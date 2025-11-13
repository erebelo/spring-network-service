package com.erebelo.springnetworkservice.service.impl;

import static com.erebelo.springnetworkservice.domain.enumeration.RoleEnum.BENEFICIARY;
import static com.erebelo.springnetworkservice.domain.enumeration.RoleEnum.DEPENDENT;
import static com.erebelo.springnetworkservice.domain.enumeration.RoleEnum.POLICY_HOLDER;
import static com.erebelo.springnetworkservice.domain.enumeration.RoleEnum.SUB_AGENT;

import com.erebelo.springnetworkservice.domain.dto.RelationshipDto;
import com.erebelo.springnetworkservice.domain.dto.RelationshipVertexDto;
import com.erebelo.springnetworkservice.domain.enumeration.RoleEnum;
import com.erebelo.springnetworkservice.domain.model.Relationship;
import com.erebelo.springnetworkservice.repository.RelationshipRepository;
import com.erebelo.springnetworkservice.service.NetworkTraversalService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NetworkTraversalServiceImpl implements NetworkTraversalService {

    private final RelationshipRepository repository;

    /**
     * Set of roles ineligible for high-level node.
     */
    private static final Set<RoleEnum> NON_HIGH_LEVEL_ROLES = Set.of(SUB_AGENT, DEPENDENT, BENEFICIARY, POLICY_HOLDER);

    /**
     * Builds the full relationship network starting from a given root referenceId,
     * using the Depth-First Search (DFS) algorithm to traverse and collect
     * connected nodes.
     */
    @Override
    public List<Relationship> deriveNetworkFromRootReferenceId(String rootReferenceId) {
        List<Relationship> network = new ArrayList<>();
        HashSet<String> visited = new HashSet<>();

        validateReferenceIdIsRootNode(rootReferenceId);

        deriveNetworkDFS(rootReferenceId, network, visited);

        return network;
    }

    private void deriveNetworkDFS(String fromReferenceId, List<Relationship> network, Set<String> visited) {
        if (visited.add(fromReferenceId)) {
            // Skip recursive call if this node was already visited (cycle detected)
            return;
        }

        repository.findByFromReferenceId(fromReferenceId).forEach(rel -> {
            network.add(rel);
            deriveNetworkDFS(rel.getTo().getReferenceId(), network, visited);
        });
    }

    private void validateReferenceIdIsRootNode(String referenceId) {
        if (repository.findByToReferenceId(referenceId).isEmpty()) {
            throw new IllegalStateException(String.format("Provided referenceId=%s is not the root", referenceId));
        }
    }

    /**
     * Finds the highest-level node in the network hierarchy starting from a given
     * root referenceId, using the Breadth-First Search (BFS) algorithm to traverse
     * connected nodes.
     * 
     * <pre>
     * Example input relationships:
     * 	   R1: { fromVertex: { referenceId=CLIENT_ID }, toVertex: { referenceId=REGIONAL_MANAGER_ID } }
     * 	   R2: { fromVertex: { referenceId=CLIENT_ID } toVertex: { referenceId=POLICY_HOLDER } }
     * 	   R3: { fromVertex: { referenceId=REGIONAL_MANAGER } toVertex: { referenceId=AGENCY } }
     * 	   R4: { fromVertex: { referenceId=POLICY_HOLDER } toVertex: { referenceId=REGIONAL_MANAGER } } [cycle detected]
     *
     * After building vertexConnections:
     *     CLIENT_ID = [ { referenceId=REGIONAL_MANAGER_ID }, { referenceId=POLICY_HOLDER } ]
     *     REGIONAL_MANAGER_ID = [ { referenceId=AGENCY } ]
     *     POLICY_HOLDER = [ { referenceId=REGIONAL_MANAGER_ID } ] [cycle detected]
     * </pre>
     *
     * Note: During traversal, cycles are detected and skipped to prevent infinite
     * loops.
     */
    @Override
    public Optional<RelationshipVertexDto> findHighestLevelNode(List<RelationshipDto> relationships,
            String rootReferenceId) {
        Queue<Pair<String, Integer>> queue = new LinkedList<>();
        queue.offer(Pair.of(rootReferenceId, 0));

        Set<String> visited = new HashSet<>(Set.of(rootReferenceId));
        RelationshipVertexDto highestLevelNode = null;
        Map<String, List<RelationshipVertexDto>> vertexConnections = new HashMap<>();

        for (RelationshipDto r : relationships) {
            String fromReferenceId = r.getFrom().getReferenceId();
            vertexConnections.computeIfAbsent(fromReferenceId, k -> new ArrayList<>()).add(r.getTo());

            if (rootReferenceId.equals(fromReferenceId) && highestLevelNode == null) {
                highestLevelNode = r.getFrom();
            }
        }

        return Optional.ofNullable(findHighestLevelNodeBFS(queue, visited, highestLevelNode, vertexConnections));
    }

    private RelationshipVertexDto findHighestLevelNodeBFS(Queue<Pair<String, Integer>> queue, Set<String> visited,
            RelationshipVertexDto highestLevelNode, Map<String, List<RelationshipVertexDto>> vertexConnections) {
        int deepest = -1;

        while (!queue.isEmpty()) {
            Pair<String, Integer> current = queue.poll(); // Removes and returns the front element (or null if empty)
            String currentReferenceId = current.getLeft();
            int depth = current.getRight();

            List<RelationshipVertexDto> neighbors = vertexConnections.getOrDefault(currentReferenceId, List.of());

            for (RelationshipVertexDto neighbor : neighbors) {
                if (visited.add(neighbor.getReferenceId())) {
                    int nextDepth = depth + 1;
                    queue.offer(Pair.of(neighbor.getReferenceId(), nextDepth)); // Adds an element to the back of the
                                                                                // queue

                    if (isEligibleHighLevelNode(neighbor.getRole(), nextDepth, deepest)) {
                        // Eligible high-level node found
                        highestLevelNode = neighbor;
                        deepest = nextDepth;
                    }
                }
            }
        }

        return highestLevelNode;
    }

    private boolean isEligibleHighLevelNode(RoleEnum role, int depth, int deepest) {
        return !NON_HIGH_LEVEL_ROLES.contains(role) && depth > deepest;
    }
}
