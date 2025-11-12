package com.erebelo.springnetworkservice.repository.impl;

import com.erebelo.springnetworkservice.domain.enumeration.RoleEnum;
import com.erebelo.springnetworkservice.domain.model.Contract;
import com.erebelo.springnetworkservice.repository.NetworkHydrationRepository;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NetworkHydrationRepositoryImpl implements NetworkHydrationRepository {

    private final MongoTemplate mongoTemplate;

    @Value("${network.hydration.query.batch-size:5000}")
    private Integer queryBatchSize;

    /**
     * Returns a stream of contract referenceIds for the given role.
     * <p>
     * This uses a MongoDB cursor under the hood (memory efficient for large
     * datasets).
     * </p>
     */
    @Override
    public Stream<String> streamReferenceIdBatchByRole(RoleEnum role) {
        Query query = new Query(Criteria.where("role").is(role)).cursorBatchSize(queryBatchSize);

        // Only include referenceId to minimize data transfer
        query.fields().include("referenceId");

        return mongoTemplate.stream(query, Contract.class).map(Contract::getReferenceId);
    }
}
