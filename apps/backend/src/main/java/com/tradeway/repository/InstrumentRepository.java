package com.tradeway.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tradeway.model.Instrument;

public interface InstrumentRepository extends JpaRepository<Instrument, Long> {

    Optional<Instrument> findBySymbolIgnoreCase(String symbol);
}
