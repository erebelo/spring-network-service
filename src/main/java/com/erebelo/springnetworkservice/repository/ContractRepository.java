package com.erebelo.springnetworkservice.repository;

import com.erebelo.springnetworkservice.domain.model.Contract;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractRepository extends MongoRepository<Contract, String> {

    Optional<Contract> findByReferenceId(String referenceId);

}
