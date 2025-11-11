package com.erebelo.springnetworkservice.domain.dto;

import com.erebelo.springnetworkservice.domain.enumeration.RoleEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RelationshipVertexDto {

    private String referenceId;
    private String profileId;
    private RoleEnum role;

}
