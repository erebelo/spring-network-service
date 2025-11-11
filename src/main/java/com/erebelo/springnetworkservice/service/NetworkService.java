package com.erebelo.springnetworkservice.service;

import com.erebelo.springnetworkservice.domain.dto.NetworkDto;
import java.time.LocalDate;

public interface NetworkService {

    NetworkDto findByRootReferenceId(String rootReferenceId, LocalDate relationshipDate);

}
