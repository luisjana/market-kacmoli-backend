package com.marketkacmoli.market_kacmoli_backend.repository;

import com.marketkacmoli.market_kacmoli_backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}