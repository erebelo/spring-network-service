package com.erebelo.springnetworkservice.controller;

import static com.erebelo.springnetworkservice.constant.BusinessConstant.NETWORKS_GRAPH_PATH;
import static com.erebelo.springnetworkservice.constant.BusinessConstant.NETWORKS_PATH;

import com.erebelo.springnetworkservice.domain.dto.NetworkDto;
import com.erebelo.springnetworkservice.domain.dto.NonSellingRelationshipDto;
import com.erebelo.springnetworkservice.domain.dto.NonSellingRelationshipVertexDto;
import com.erebelo.springnetworkservice.domain.dto.RelationshipDto;
import com.erebelo.springnetworkservice.domain.dto.RelationshipVertexDto;
import com.erebelo.springnetworkservice.util.ObjectMapperUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * (LOCALLY ONLY) Network UI API that generates graph data (vertices and edges)
 * for Cytoscape-based visualization
 */
@Slf4j
@Profile("local")
@RestController
@RequiredArgsConstructor
@RequestMapping(NETWORKS_PATH)
public class NetworkUIController {

    private final NetworkController controller;

    private static final String NETWORK_JSON_KEY = "networks";
    private static final String VERTICES_KEY = "vertices";
    private static final String EDGES_KEY = "edges";
    private static final String NET_KEY = "NET:";
    private static final String VTX_KEY = "_VTX:";
    private static final String EDG_KEY = "_EDG:";
    private static final String LABEL_KEY = "label";
    private static final String NAME_KEY = "name";
    private static final String FROM_KEY = "from";
    private static final String TO_KEY = "to";
    private static final String SELLING_VTX_LABEL = "Selling Vertex";
    private static final String NON_SELLING_VTX_LABEL = "Non-Selling Vertex";
    private static final String SELLING_REL_LABEL = "Selling Relationship";
    private static final String NON_SELLING_REL_LABEL = "Non-Selling Relationship";

    @GetMapping(value = NETWORKS_GRAPH_PATH + "/{rootReferenceId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public Map<String, Object> getNetworkGraph(@PathVariable("rootReferenceId") String rootReferenceId,
            @RequestParam(value = "relationshipDate", required = false) LocalDate relationshipDate) {
        log.info("POST {}/{}?relationshipDate={}", NETWORKS_PATH + NETWORKS_GRAPH_PATH, rootReferenceId,
                relationshipDate != null ? relationshipDate : "");

        NetworkDto network = controller.getNetworkByRootReferenceId(rootReferenceId, relationshipDate).getBody();
        Map<String, Object> graphData = new HashMap<>();

        if (network != null) {
            deriveNetwork(graphData, network);
        }

        return graphData;
    }

    private void deriveNetwork(Map<String, Object> graphData, NetworkDto network) {
        Map<String, Map<String, String>> vertices = new HashMap<>();
        Map<String, Map<String, String>> edges = new HashMap<>();

        deriveSellingRelationships(vertices, edges, network.getRootReferenceId(), network.getSellingRelationships());
        deriveNonSellingRelationships(vertices, edges, network.getRootReferenceId(),
                network.getNonSellingRelationships());

        graphData.put(NETWORK_JSON_KEY, network);
        graphData.put(VERTICES_KEY, vertices);
        graphData.put(EDGES_KEY, edges);
    }

    private void deriveSellingRelationships(Map<String, Map<String, String>> vertices,
            Map<String, Map<String, String>> edges, String networkId, List<RelationshipDto> sellingRelationships) {
        if (sellingRelationships != null && !sellingRelationships.isEmpty()) {
            sellingRelationships.forEach(rel -> {
                RelationshipVertexDto from = rel.getFrom();
                RelationshipVertexDto to = rel.getTo();

                if (from == null || to == null) {
                    return;
                }

                String fromVertexId = NET_KEY + networkId + VTX_KEY + from.getReferenceId();
                String toVertexId = NET_KEY + networkId + VTX_KEY + to.getReferenceId();

                Map<String, String> relationshipProps = new LinkedHashMap<>();
                relationshipProps.put(LABEL_KEY, SELLING_REL_LABEL);
                relationshipProps.put(FROM_KEY, fromVertexId);
                relationshipProps.put(TO_KEY, toVertexId);
                relationshipProps.putAll(toStringMap(rel, "from", "to"));

                edges.putIfAbsent(NET_KEY + networkId + EDG_KEY + rel.getId(), relationshipProps);

                decorateVertex(vertices, from, fromVertexId, SELLING_VTX_LABEL, from.getRole().getValue());
                decorateVertex(vertices, to, toVertexId, SELLING_VTX_LABEL, to.getRole().getValue());
            });
        }
    }

    private void deriveNonSellingRelationships(Map<String, Map<String, String>> vertices,
            Map<String, Map<String, String>> edges, String networkId,
            List<NonSellingRelationshipDto> nonSellingRelationships) {
        if (nonSellingRelationships != null && !nonSellingRelationships.isEmpty()) {
            nonSellingRelationships.forEach(rel -> {
                NonSellingRelationshipVertexDto from = rel.getFrom();
                RelationshipVertexDto to = rel.getTo();

                if (from == null || to == null) {
                    return;
                }

                String fromVertexId = NET_KEY + networkId + VTX_KEY + from.getOrgId();
                String toVertexId = NET_KEY + networkId + VTX_KEY + to.getReferenceId();

                Map<String, String> relationshipProps = new LinkedHashMap<>();
                relationshipProps.put(LABEL_KEY, NON_SELLING_REL_LABEL);
                relationshipProps.put(FROM_KEY, fromVertexId);
                relationshipProps.put(TO_KEY, toVertexId);
                relationshipProps.putAll(toStringMap(rel, "from", "to"));

                edges.putIfAbsent(NET_KEY + networkId + EDG_KEY + rel.getId(), relationshipProps);

                decorateVertex(vertices, from, fromVertexId, NON_SELLING_VTX_LABEL, from.getOrgName());
                decorateVertex(vertices, to, toVertexId, NON_SELLING_VTX_LABEL, to.getRole().getValue());
            });
        }
    }

    private void decorateVertex(Map<String, Map<String, String>> vertices, Object vertex, String id, String label,
            String name) {
        Map<String, String> entityPropertiesMap = new LinkedHashMap<>();
        entityPropertiesMap.put(LABEL_KEY, label);
        entityPropertiesMap.put(NAME_KEY, name);
        entityPropertiesMap.putAll(toStringMap(vertex));
        vertices.putIfAbsent(id, entityPropertiesMap);
    }

    private Map<String, String> toStringMap(Object obj, String... excludedAttributes) {
        Map<String, Object> rawMap = ObjectMapperUtil.objectMapper.convertValue(obj,
                new TypeReference<LinkedHashMap<String, Object>>() {
                });
        Set<String> excludedSet = excludedAttributes != null
                ? new HashSet<>(Arrays.asList(excludedAttributes))
                : Collections.emptySet();

        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : rawMap.entrySet()) {
            if (entry.getValue() != null && !excludedSet.contains(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue().toString());
            }
        }
        return result;
    }
}
