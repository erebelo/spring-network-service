package com.erebelo.springnetworkservice.repository;

import com.erebelo.springnetworkservice.domain.model.Relationship;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RelationshipRepository extends MongoRepository<Relationship, String> {

    @Query("{ 'from.referenceId': ?0 }")
    List<Relationship> findByFromReferenceId(String referenceId);

    @Query("{ 'to.referenceId': ?0 }")
    List<Relationship> findByToReferenceId(String referenceId);

}
