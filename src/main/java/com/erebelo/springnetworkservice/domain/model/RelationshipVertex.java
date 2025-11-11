package com.erebelo.springnetworkservice.domain.model;

import com.erebelo.springnetworkservice.domain.enumeration.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class RelationshipVertex {

    private String referenceId;

    @Transient
    private String profileId;

    @Transient
    private RoleEnum role;

}
