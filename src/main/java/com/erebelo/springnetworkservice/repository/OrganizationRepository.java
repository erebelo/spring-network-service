package com.erebelo.springnetworkservice.repository;

import com.erebelo.springnetworkservice.domain.model.Organization;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository extends MongoRepository<Organization, String> {

}
