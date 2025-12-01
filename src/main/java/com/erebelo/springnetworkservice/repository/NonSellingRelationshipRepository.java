package com.erebelo.springnetworkservice.repository;

import com.erebelo.springnetworkservice.domain.model.NonSellingRelationship;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NonSellingRelationshipRepository extends MongoRepository<NonSellingRelationship, String> {

    Optional<List<NonSellingRelationship>> findNonSellingRelationshipsByReferenceIds(Set<String> referenceIds);

}
