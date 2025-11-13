package com.erebelo.springnetworkservice.domain.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class NonSellingRelationshipVertex {

    @NotBlank(message = "orgId is mandatory")
    private String orgId;

    @Transient
    private String orgName;

}
