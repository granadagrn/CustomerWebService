package com.demo.springbootoracle.repository;

import com.demo.springbootoracle.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    //boolean existsByAddressName(String addressName);
}

