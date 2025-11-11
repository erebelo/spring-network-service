package com.erebelo.springnetworkservice.domain.model;

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

    private String orgId;

    @Transient
    private String orgName;

}
