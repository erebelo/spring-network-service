package com.erebelo.springnetworkservice.mapper;

import static org.mapstruct.ReportingPolicy.WARN;

import com.erebelo.springnetworkservice.domain.dto.NonSellingRelationshipDto;
import com.erebelo.springnetworkservice.domain.model.NonSellingRelationship;
import com.erebelo.springnetworkservice.domain.model.NonSellingRelationshipVertex;
import com.erebelo.springnetworkservice.domain.model.Organization;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", unmappedTargetPolicy = WARN)
public interface NonSellingRelationshipMapper {

    List<NonSellingRelationshipDto> toDtoList(List<NonSellingRelationship> nonSellingRelationships);

    NonSellingRelationshipDto toDto(NonSellingRelationship relationship);

    @Mapping(target = "orgId", source = "id")
    @Mapping(target = "orgName", source = "name")
    NonSellingRelationshipVertex organizationToNonSellingRelationshipVertex(Organization organization);

}
