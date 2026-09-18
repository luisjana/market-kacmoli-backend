package com.marketkacmoli.market_kacmoli_backend.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.marketkacmoli.market_kacmoli_backend.model.Catalog;
import com.marketkacmoli.market_kacmoli_backend.repository.CatalogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    @Autowired
    private CatalogRepository catalogRepository;

    @Autowired
    private Cloudinary cloudinary;

    // Publik - e thërret faqja kryesore
    @GetMapping
    public ResponseEntity<?> getCatalog() {
        List<Catalog> all = catalogRepository.findAll();
        if (all.isEmpty()) {
            return ResponseEntity.ok().body(Map.of("exists", false));
        }
        Catalog catalog = all.get(0);
        return ResponseEntity.ok(catalog);
    }

    // Vetëm admin - upload/ndryshim katalogu
    @PostMapping
    public ResponseEntity<?> uploadCatalog(@RequestParam("file") MultipartFile file) {
        try {
            // Fshi katalogun e vjetër nëse ekziston
            List<Catalog> existing = catalogRepository.findAll();
            if (!existing.isEmpty()) {
                Catalog old = existing.get(0);
                cloudinary.uploader().destroy(old.getPublicId(), ObjectUtils.emptyMap());
                catalogRepository.delete(old);
            }

            // Ngarko të riun
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            String url = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            Catalog catalog = new Catalog(url, publicId, LocalDateTime.now());
            catalogRepository.save(catalog);

            return ResponseEntity.ok(catalog);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // Vetëm admin - fshirje katalogu
    @DeleteMapping
    public ResponseEntity<?> deleteCatalog() {
        List<Catalog> existing = catalogRepository.findAll();
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Catalog old = existing.get(0);
        try {
            cloudinary.uploader().destroy(old.getPublicId(), ObjectUtils.emptyMap());
        } catch (Exception e) {
            // vazhdo edhe nëse Cloudinary dështon te fshirja, largo nga DB gjithsesi
        }
        catalogRepository.delete(old);
        return ResponseEntity.ok().body(Map.of("deleted", true));
    }
}