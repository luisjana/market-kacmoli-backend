package com.marketkacmoli.market_kacmoli_backend.repository;

import com.marketkacmoli.market_kacmoli_backend.model.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
}