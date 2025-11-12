package com.erebelo.springnetworkservice.repository;

import com.erebelo.springnetworkservice.domain.enumeration.RoleEnum;
import java.util.stream.Stream;

public interface NetworkHydrationRepository {

    Stream<String> streamReferenceIdBatchByRole(RoleEnum role);

}
