package com.erebelo.springnetworkservice.repository;

import com.erebelo.springnetworkservice.domain.model.Organization;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository extends MongoRepository<Organization, String> {

    Optional<Organization> findByOrgRefId(String orgRefId);

}
