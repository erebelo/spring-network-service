package com.erebelo.springnetworkservice.service;

import com.erebelo.springnetworkservice.domain.dto.NetworkDto;
import com.erebelo.springnetworkservice.domain.model.Relationship;
import java.time.LocalDate;
import java.util.List;

public interface NetworkDecoratorService {

    NetworkDto legacyNetworkDecorator(List<Relationship> relationships, String rootReferenceId,
            LocalDate relationshipDate);

}
