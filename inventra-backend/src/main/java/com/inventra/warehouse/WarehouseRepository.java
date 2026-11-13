package com.inventra.warehouse;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    boolean existsByCode(String code);

    Optional<Warehouse> findByCode(String code);
}
