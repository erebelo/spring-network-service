package com.erebelo.springnetworkservice.domain.dto;

import com.erebelo.springnetworkservice.domain.enumeration.StatusEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
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
public class OrganizationDto {

    private String id;
    private String orgRefId;
    private String name;
    private StatusEnum status;
    private LocalDate startDate;
    private LocalDate endDate;

}
