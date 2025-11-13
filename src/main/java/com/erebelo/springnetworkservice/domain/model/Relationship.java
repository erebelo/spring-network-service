package com.erebelo.springnetworkservice.domain.model;

import com.erebelo.springnetworkservice.domain.enumeration.StatusEnum;
import jakarta.validation.Valid;
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
@Document(collection = "relationships")
public class Relationship extends BaseEntity {

    @Id
    private String id;

    @Valid
    private RelationshipVertex from;

    @Valid
    private RelationshipVertex to;

    @NotNull(message = "status is mandatory")
    private StatusEnum status;

    @NotNull(message = "startDate is mandatory")
    private LocalDate startDate;

    private LocalDate endDate;

}
