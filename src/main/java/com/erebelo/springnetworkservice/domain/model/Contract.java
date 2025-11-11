package com.erebelo.springnetworkservice.domain.model;

import com.erebelo.springnetworkservice.domain.enumeration.BusinessChannelEnum;
import com.erebelo.springnetworkservice.domain.enumeration.ProductTypeEnum;
import com.erebelo.springnetworkservice.domain.enumeration.RoleEnum;
import com.erebelo.springnetworkservice.domain.enumeration.StatusEnum;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = "contracts")
public class Contract extends BaseEntity {

    @Id
    private String id;

    private String referenceId;
    private String profileId;
    private RoleEnum role;
    private BusinessChannelEnum businessChannel;
    private ProductTypeEnum productType;
    private StatusEnum status;
    private LocalDate startDate;
    private LocalDate endDate;

}
