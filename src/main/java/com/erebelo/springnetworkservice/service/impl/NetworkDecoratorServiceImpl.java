package com.erebelo.springnetworkservice.service.impl;

import static com.erebelo.springnetworkservice.domain.enumeration.RoleEnum.AGENCY;
import static com.erebelo.springnetworkservice.domain.enumeration.RoleEnum.AGENT;

import com.erebelo.springnetworkservice.domain.dto.NetworkDto;
import com.erebelo.springnetworkservice.domain.dto.RelationshipDto;
import com.erebelo.springnetworkservice.domain.dto.RelationshipVertexDto;
import com.erebelo.springnetworkservice.domain.enumeration.RoleEnum;
import com.erebelo.springnetworkservice.domain.enumeration.StatusEnum;
import com.erebelo.springnetworkservice.domain.model.Contract;
import com.erebelo.springnetworkservice.domain.model.NonSellingRelationship;
import com.erebelo.springnetworkservice.domain.model.NonSellingRelationshipVertex;
import com.erebelo.springnetworkservice.domain.model.Organization;
import com.erebelo.springnetworkservice.domain.model.Relationship;
import com.erebelo.springnetworkservice.domain.model.RelationshipVertex;
import com.erebelo.springnetworkservice.exception.model.NotFoundException;
import com.erebelo.springnetworkservice.mapper.NonSellingRelationshipMapper;
import com.erebelo.springnetworkservice.mapper.RelationshipMapper;
import com.erebelo.springnetworkservice.repository.ContractRepository;
import com.erebelo.springnetworkservice.repository.NonSellingRelationshipRepository;
import com.erebelo.springnetworkservice.repository.OrganizationRepository;
import com.erebelo.springnetworkservice.service.NetworkDecoratorService;
import com.erebelo.springnetworkservice.service.NetworkTraversalService;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class NetworkDecoratorServiceImpl implements NetworkDecoratorService {

    private final NetworkTraversalService networkTraversalService;
    private final ContractRepository contractRepository;
    private final OrganizationRepository organizationRepository;
    private final NonSellingRelationshipRepository nonSellingRelationshipRepository;
    private final RelationshipMapper relationshipMapper;
    private final NonSellingRelationshipMapper nonSellingRelationshipMapper;

    private static final Set<RoleEnum> NON_SELLING_RELATIONSHIP_ROLES = Set.of(AGENT, AGENCY);

    @Override
    public NetworkDto legacyNetworkDecorator(List<Relationship> relationships, String rootReferenceId,
            LocalDate relationshipDate) {
        if (CollectionUtils.isEmpty(relationships)) {
            return buildNetwork(null, null, rootReferenceId, relationshipDate);
        }

        List<Relationship> filteredRelationships = filterByRelationshipDate(relationships, relationshipDate);

        Map<String, RelationshipVertex> vertexCache = new HashMap<>();
        filteredRelationships.forEach((Relationship r) -> decorateRelationship(r, vertexCache));

        try {
            Set<String> nonSellingRelationshipReferenceIds = new LinkedHashSet<>();

            for (Relationship r : filteredRelationships) {
                if (NON_SELLING_RELATIONSHIP_ROLES.contains(r.getFrom().getRole())) {
                    nonSellingRelationshipReferenceIds.add(r.getFrom().getReferenceId());
                }
                if (NON_SELLING_RELATIONSHIP_ROLES.contains(r.getTo().getRole())) {
                    nonSellingRelationshipReferenceIds.add(r.getTo().getReferenceId());
                }
            }

            List<NonSellingRelationship> nonSellingRelationships = findAndDecorateNonSellingRelationships(vertexCache,
                    nonSellingRelationshipReferenceIds);
            List<NonSellingRelationship> filteredNonSellingRelationships = filterByRelationshipDate(
                    nonSellingRelationships, relationshipDate);

            return buildNetwork(filteredRelationships, filteredNonSellingRelationships, rootReferenceId,
                    relationshipDate);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    String.format("An error occurred while decorating network for rootReferenceId=%s. Error: %s",
                            rootReferenceId, e.getMessage()),
                    e);
        }
    }

    /*
     * Filters relationships by a given date (startDate ≤ date and (endDate is null
     * or ≥ date))
     */
    private static <T> List<T> filterByRelationshipDate(List<T> items, LocalDate date) {
        if (date == null || items == null) {
            return items;
        }

        return items.stream().filter((T item) -> {
            try {
                LocalDate startDate = (LocalDate) item.getClass().getMethod("getStartDate").invoke(item);
                LocalDate endDate = (LocalDate) item.getClass().getMethod("getEndDate").invoke(item);
                return !startDate.isAfter(date) && (endDate == null || !endDate.isBefore(date));
            } catch (Exception e) {
                throw new IllegalStateException(
                        "Unable to access startDate/endDate while filtering by relationship date for class: "
                                + item.getClass().getSimpleName(),
                        e);
            }
        }).toList();
    }

    private void decorateRelationship(Relationship relationship, Map<String, RelationshipVertex> vertexCache) {
        RelationshipVertex from = vertexCache.computeIfAbsent(relationship.getFrom().getReferenceId(),
                this::findContractByReferenceId);
        RelationshipVertex to = vertexCache.computeIfAbsent(relationship.getTo().getReferenceId(),
                this::findContractByReferenceId);

        relationship.setFrom(from);
        relationship.setTo(to);
    }

    private List<NonSellingRelationship> findAndDecorateNonSellingRelationships(
            Map<String, RelationshipVertex> vertexCache, Set<String> nonSellingRelationshipReferenceIds) {
        List<NonSellingRelationship> nonSellingRelationships = nonSellingRelationshipRepository
                .findNonSellingRelationshipsByReferenceIds(nonSellingRelationshipReferenceIds)
                .orElse(Collections.emptyList());

        if (!nonSellingRelationships.isEmpty()) {
            nonSellingRelationships.forEach((NonSellingRelationship r) -> {
                setNonSellingRelationshipVertex(r);
                setRelationshipVertex(r, vertexCache);
            });
        }

        return nonSellingRelationships;
    }

    private void setNonSellingRelationshipVertex(NonSellingRelationship nonSellingRelationship) {
        NonSellingRelationshipVertex from = findOrganizationByOrgId(nonSellingRelationship.getFrom().getOrgId());
        nonSellingRelationship.setFrom(from);
    }

    private void setRelationshipVertex(NonSellingRelationship nonSellingRelationship,
            Map<String, RelationshipVertex> vertexCache) {
        RelationshipVertex to = vertexCache.computeIfAbsent(nonSellingRelationship.getTo().getReferenceId(),
                this::findContractByReferenceId);
        nonSellingRelationship.setTo(to);
    }

    private NetworkDto buildNetwork(List<Relationship> relationships,
            List<NonSellingRelationship> nonSellingRelationships, String rootReferenceId, LocalDate relationshipDate) {
        Contract rootContract = contractRepository.findByReferenceId(rootReferenceId).orElseThrow(
                () -> new NotFoundException("No root contract found by rootReferenceId=" + rootReferenceId));

        NetworkDto network = buildBaseNetwork(relationships, nonSellingRelationships, rootReferenceId, rootContract);

        if (CollectionUtils.isEmpty(relationships) || !isValidRelationshipDateRange(network, relationshipDate)) {
            network.setStatus(StatusEnum.INACTIVE);
            network.setSellingRelationships(null);
            network.setNonSellingRelationships(null);
            return network;
        }

        Contract highestLevelContract = retrieveContractFromHighestLevelNode(network.getSellingRelationships(),
                rootReferenceId);
        enrichNetwork(network, highestLevelContract, relationshipDate);

        return network;
    }

    private NetworkDto buildBaseNetwork(List<Relationship> relationships,
            List<NonSellingRelationship> nonSellingRelationships, String rootAdId, Contract rootContract) {
        NetworkDto network = new NetworkDto();
        network.setRootReferenceId(rootAdId);
        network.setStatus(rootContract.getStatus());
        network.setStartDate(rootContract.getStartDate());
        network.setEndDate(rootContract.getEndDate());
        network.setSellingRelationships(relationshipMapper.toDtoList(relationships));
        network.setNonSellingRelationships(nonSellingRelationshipMapper.toDtoList(nonSellingRelationships));

        return network;
    }

    private void enrichNetwork(NetworkDto network, Contract highestLevelContract, LocalDate relationshipDate) {
        network.setHighestLevelReferenceId(highestLevelContract.getReferenceId());
        network.setProductType(highestLevelContract.getProductType());
        network.setBusinessChannel(highestLevelContract.getBusinessChannel());
        network.setStatus(refreshNetworkStatus(network, relationshipDate));
    }

    private Contract retrieveContractFromHighestLevelNode(List<RelationshipDto> relationships, String rootReferenceId) {
        return networkTraversalService.findHighestLevelNode(relationships, rootReferenceId)
                .map(RelationshipVertexDto::getReferenceId).flatMap(contractRepository::findByReferenceId)
                .orElseThrow(() -> new NotFoundException(
                        "No contract found from the highest-level node for network with rootReferenceId="
                                + rootReferenceId));
    }

    private boolean isValidRelationshipDateRange(NetworkDto network, LocalDate relationshipDateFilter) {
        if (relationshipDateFilter != null && network.getEndDate() != null) {
            // Check that relationshipDate is within the network startDate and endDate range
            return (!relationshipDateFilter.isBefore(network.getStartDate())
                    && !relationshipDateFilter.isAfter(network.getEndDate()));
        }
        return true;
    }

    private StatusEnum refreshNetworkStatus(NetworkDto network, LocalDate relationshipDateFilter) {
        if (relationshipDateFilter == null) {
            return network.getStatus();
        }

        if (network.getEndDate() != null) {
            if (!relationshipDateFilter.isBefore(network.getStartDate())
                    && relationshipDateFilter.isAfter(network.getEndDate())) {
                return StatusEnum.ACTIVE;
            }

            return StatusEnum.INACTIVE;
        }

        return StatusEnum.ACTIVE;
    }

    private RelationshipVertex findContractByReferenceId(String referenceId) {
        Contract contract = contractRepository.findByReferenceId(referenceId)
                .orElseThrow(() -> new NotFoundException("No contract found by referenceId=" + referenceId));
        return relationshipMapper.contractToRelationshipVertex(contract);
    }

    private NonSellingRelationshipVertex findOrganizationByOrgId(String orgId) {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new NotFoundException("No organization found by orgId=" + orgId));
        return nonSellingRelationshipMapper.organizationToNonSellingRelationshipVertex(organization);
    }
}
