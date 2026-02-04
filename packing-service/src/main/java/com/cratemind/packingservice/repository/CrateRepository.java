package com.cratemind.packingservice.repository;

import com.cratemind.packingservice.entity.Crates;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CrateRepository extends JpaRepository<Crates, UUID> {
}