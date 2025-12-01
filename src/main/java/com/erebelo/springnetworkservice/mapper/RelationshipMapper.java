package com.erebelo.springnetworkservice.mapper;

import static org.mapstruct.ReportingPolicy.WARN;

import com.erebelo.springnetworkservice.domain.dto.RelationshipDto;
import com.erebelo.springnetworkservice.domain.model.Contract;
import com.erebelo.springnetworkservice.domain.model.Relationship;
import com.erebelo.springnetworkservice.domain.model.RelationshipVertex;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", unmappedTargetPolicy = WARN)
public interface RelationshipMapper {

    List<RelationshipDto> toDtoList(List<Relationship> relationships);

    RelationshipDto toDto(Relationship relationship);

    RelationshipVertex contractToRelationshipVertex(Contract contract);

}
