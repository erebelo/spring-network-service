package com.erebelo.springnetworkservice.service.impl;

import com.erebelo.springnetworkservice.config.NetworkProperties;
import com.erebelo.springnetworkservice.domain.dto.NetworkDto;
import com.erebelo.springnetworkservice.domain.model.Contract;
import com.erebelo.springnetworkservice.domain.model.Relationship;
import com.erebelo.springnetworkservice.exception.model.BadRequestException;
import com.erebelo.springnetworkservice.exception.model.NotFoundException;
import com.erebelo.springnetworkservice.repository.ContractRepository;
import com.erebelo.springnetworkservice.service.NetworkDecoratorService;
import com.erebelo.springnetworkservice.service.NetworkService;
import com.erebelo.springnetworkservice.service.NetworkTraversalService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NetworkServiceImpl implements NetworkService {

    private final NetworkProperties networkProperties;
    private final NetworkTraversalService networkTraversalService;
    private final NetworkDecoratorService networkDecoratorService;
    private final ContractRepository contractRepository;

    @Override
    public NetworkDto findByRootReferenceId(String rootReferenceId, LocalDate relationshipDate) {
        if (rootReferenceId == null || rootReferenceId.isBlank()) {
            throw new BadRequestException("'rootReferenceId' is invalid");
        }

        Contract contract = contractRepository.findByReferenceId(rootReferenceId)
                .orElseThrow(() -> new NotFoundException("No contract found by rootReferenceId=" + rootReferenceId));

        if (!networkProperties.getRootCriteria().contains(contract.getRole())) {
            throw new BadRequestException(
                    String.format("Provided referenceId=%s is not a root contract", rootReferenceId));
        }

        List<Relationship> relationships = networkTraversalService
                .deriveNetworkFromRootReferenceId(contract.getReferenceId());
        NetworkDto network = networkDecoratorService.legacyNetworkDecorator(relationships, rootReferenceId,
                relationshipDate);

        int sellingRelationshipSize = network.getSellingRelationships() != null
                ? network.getSellingRelationships().size()
                : 0;
        int nonSellingRelationshipSize = network.getNonSellingRelationships() != null
                ? network.getNonSellingRelationships().size()
                : 0;
        log.info(
                "Network details: rootReferenceId={}, highestLevelReferenceId={}, sellingRelationships={}, nonSellingRelationships={}",
                network.getRootReferenceId(), network.getHighestLevelReferenceId(), sellingRelationshipSize,
                nonSellingRelationshipSize);

        return network;
    }
}
