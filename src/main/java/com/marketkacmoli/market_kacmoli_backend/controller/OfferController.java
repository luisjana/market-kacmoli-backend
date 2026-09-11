package com.marketkacmoli.market_kacmoli_backend.controller;

import com.marketkacmoli.market_kacmoli_backend.model.Offer;
import com.marketkacmoli.market_kacmoli_backend.repository.OfferRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offers")
//@CrossOrigin(origins = "*")
public class OfferController {

    private final OfferRepository offerRepository;

    public OfferController(OfferRepository offerRepository) {
        this.offerRepository = offerRepository;
    }

    @GetMapping
    public List<Offer> getAllOffers() {
        return offerRepository.findAll();
    }

    @PostMapping
    public Offer createOffer(@RequestBody Offer offer) {
        return offerRepository.save(offer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Offer> updateOffer(
            @PathVariable Long id,
            @RequestBody Offer offerDetails
    ) {
        return offerRepository.findById(id)
                .map(offer -> {

                    offer.setName(offerDetails.getName());
                    offer.setOldPrice(offerDetails.getOldPrice());
                    offer.setNewPrice(offerDetails.getNewPrice());
                    offer.setDiscount(offerDetails.getDiscount());
                    offer.setImage(offerDetails.getImage());

                    Offer updatedOffer = offerRepository.save(offer);

                    return ResponseEntity.ok(updatedOffer);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffer(@PathVariable Long id) {

        if (!offerRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        offerRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}