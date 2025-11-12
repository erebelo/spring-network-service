package com.erebelo.springnetworkservice.service.impl;

import com.erebelo.springnetworkservice.domain.dto.RelationshipDto;
import com.erebelo.springnetworkservice.domain.dto.RelationshipVertexDto;
import com.erebelo.springnetworkservice.domain.model.Relationship;
import com.erebelo.springnetworkservice.service.NetworkTraversalService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NetworkTraversalServiceImpl implements NetworkTraversalService {

    @Override
    public List<Relationship> deriveNetworkFromRootReferenceId(String rootReferenceId) {
        return List.of();
    }

    @Override
    public Optional<RelationshipVertexDto> findHighestLevelNode(List<RelationshipDto> relationships,
            String rootReferenceId) {
        return Optional.empty();
    }
}
