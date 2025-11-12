package com.erebelo.springnetworkservice.service;

import com.erebelo.springnetworkservice.domain.dto.RelationshipDto;
import com.erebelo.springnetworkservice.domain.dto.RelationshipVertexDto;
import com.erebelo.springnetworkservice.domain.model.Relationship;
import java.util.List;
import java.util.Optional;

public interface NetworkTraversalService {

    List<Relationship> deriveNetworkFromRootReferenceId(String rootReferenceId);

    Optional<RelationshipVertexDto> findHighestLevelNode(List<RelationshipDto> relationships, String rootReferenceId);

}
