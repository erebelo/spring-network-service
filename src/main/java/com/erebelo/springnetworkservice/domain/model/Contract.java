package com.erebelo.springnetworkservice.domain.model;

import com.erebelo.springnetworkservice.domain.enumeration.BusinessChannelEnum;
import com.erebelo.springnetworkservice.domain.enumeration.ProductTypeEnum;
import com.erebelo.springnetworkservice.domain.enumeration.RoleEnum;
import com.erebelo.springnetworkservice.domain.enumeration.StatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank(message = "referenceId is mandatory")
    private String referenceId;

    @NotBlank(message = "profileId is mandatory")
    private String profileId;

    @NotNull(message = "role is mandatory")
    private RoleEnum role;

    @NotNull(message = "businessChannel is mandatory")
    private BusinessChannelEnum businessChannel;

    @NotNull(message = "productType is mandatory")
    private ProductTypeEnum productType;

    @NotNull(message = "status is mandatory")
    private StatusEnum status;

    @NotNull(message = "startDate is mandatory")
    private LocalDate startDate;

    private LocalDate endDate;

}
