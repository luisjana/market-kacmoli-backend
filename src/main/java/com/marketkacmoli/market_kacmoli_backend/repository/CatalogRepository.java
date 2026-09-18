package com.marketkacmoli.market_kacmoli_backend.repository;

import com.marketkacmoli.market_kacmoli_backend.model.Catalog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogRepository extends JpaRepository<Catalog, Long> {
}