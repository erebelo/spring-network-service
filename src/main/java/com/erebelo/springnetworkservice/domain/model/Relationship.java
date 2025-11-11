package com.erebelo.springnetworkservice.domain.model;

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
@Document(collection = "relationships")
public class Relationship extends BaseEntity {

    @Id
    private String id;

    private RelationshipVertex from;
    private RelationshipVertex to;
    private StatusEnum status;
    private LocalDate startDate;
    private LocalDate endDate;

}
