package com.erebelo.springnetworkservice.domain.model;

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
@Document(collection = "organizations")
public class Organization extends BaseEntity {

    @Id
    private String id;

    @NotBlank(message = "name is mandatory")
    private String name;

    @NotNull(message = "status is mandatory")
    private StatusEnum status;

    @NotNull(message = "startDate is mandatory")
    private LocalDate startDate;

    private LocalDate endDate;

}
