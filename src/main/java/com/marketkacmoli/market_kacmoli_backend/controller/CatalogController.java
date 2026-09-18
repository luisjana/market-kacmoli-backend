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

    // Publik - kthen te gjitha kataloget
    @GetMapping
    public ResponseEntity<List<Catalog>> getAllCatalogs() {
        List<Catalog> all = catalogRepository.findAll();
        return ResponseEntity.ok(all);
    }

    // Vetem admin - shton nje katalog te ri (nuk fshin te vjetrit)
    @PostMapping
    public ResponseEntity<?> uploadCatalog(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title
    ) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            String url = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            Catalog catalog = new Catalog(title, url, publicId, LocalDateTime.now());
            catalogRepository.save(catalog);

            return ResponseEntity.ok(catalog);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    // Vetem admin - edito nje katalog ekzistues (titull dhe/ose foto)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCatalog(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        Optional<Catalog> found = catalogRepository.findById(id);

        if (found.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Catalog catalog = found.get();
        catalog.setTitle(title);

        try {
            if (file != null && !file.isEmpty()) {
                // fshi foton e vjeter nga Cloudinary
                cloudinary.uploader().destroy(catalog.getPublicId(), ObjectUtils.emptyMap());

                // ngarko foton e re
                Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
                catalog.setImageUrl((String) uploadResult.get("secure_url"));
                catalog.setPublicId((String) uploadResult.get("public_id"));
            }

            catalog.setUpdatedAt(LocalDateTime.now());
            catalogRepository.save(catalog);

            return ResponseEntity.ok(catalog);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // Vetem admin - fshin nje katalog specifik sipas id
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCatalog(@PathVariable Long id) {
        Optional<Catalog> found = catalogRepository.findById(id);

        if (found.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Catalog catalog = found.get();

        try {
            cloudinary.uploader().destroy(catalog.getPublicId(), ObjectUtils.emptyMap());
        } catch (Exception e) {
            // vazhdo edhe nese Cloudinary deshton
        }

        catalogRepository.delete(catalog);
        return ResponseEntity.ok().body(Map.of("deleted", true));
    }
}