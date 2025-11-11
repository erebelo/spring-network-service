package com.erebelo.springnetworkservice.domain.dto;

import com.erebelo.springnetworkservice.domain.enumeration.BusinessChannelEnum;
import com.erebelo.springnetworkservice.domain.enumeration.ProductTypeEnum;
import com.erebelo.springnetworkservice.domain.enumeration.StatusEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NetworkDto {

    private String rootReferenceId;

    @JsonIgnore
    @ToString.Exclude
    private String highestLevelReferenceId;

    private List<RelationshipDto> sellingRelationships;
    private List<NonSellingRelationshipDto> nonSellingRelationships;
    private ProductTypeEnum productType;
    private BusinessChannelEnum businessChannel;
    private StatusEnum status;
    private LocalDate startDate;
    private LocalDate endDate;

}
