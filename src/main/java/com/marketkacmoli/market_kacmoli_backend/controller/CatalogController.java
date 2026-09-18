package com.marketkacmoli.market_kacmoli_backend.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.marketkacmoli.market_kacmoli_backend.model.Catalog;
import com.marketkacmoli.market_kacmoli_backend.model.CatalogImage;
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

    // Publik - kthen te gjitha kataloget (secili me listen e fotove te tij)
    @GetMapping
    public ResponseEntity<List<Catalog>> getAllCatalogs() {
        return ResponseEntity.ok(catalogRepository.findAll());
    }

    // Vetem admin - krijon nje katalog te ri me disa foto njeheresh
    @PostMapping
    public ResponseEntity<?> uploadCatalog(
            @RequestParam("title") String title,
            @RequestParam("files") MultipartFile[] files
    ) {
        try {
            Catalog catalog = new Catalog(title, LocalDateTime.now());

            int position = 0;
            for (MultipartFile file : files) {
                Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
                String url = (String) uploadResult.get("secure_url");
                String publicId = (String) uploadResult.get("public_id");

                CatalogImage image = new CatalogImage(url, publicId, position, catalog);
                catalog.getImages().add(image);
                position++;
            }

            catalogRepository.save(catalog);
            return ResponseEntity.ok(catalog);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // Vetem admin - edito titullin, dhe/ose shto foto te reja ne fund te katalogut
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCatalog(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam(value = "files", required = false) MultipartFile[] files
    ) {
        Optional<Catalog> found = catalogRepository.findById(id);

        if (found.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Catalog catalog = found.get();
        catalog.setTitle(title);

        try {
            if (files != null && files.length > 0) {
                int position = catalog.getImages().size();

                for (MultipartFile file : files) {
                    Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
                    String url = (String) uploadResult.get("secure_url");
                    String publicId = (String) uploadResult.get("public_id");

                    CatalogImage image = new CatalogImage(url, publicId, position, catalog);
                    catalog.getImages().add(image);
                    position++;
                }
            }

            catalog.setUpdatedAt(LocalDateTime.now());
            catalogRepository.save(catalog);

            return ResponseEntity.ok(catalog);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // Vetem admin - fshin nje katalog te tere (te gjitha fotot e tij nga Cloudinary + DB)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCatalog(@PathVariable Long id) {
        Optional<Catalog> found = catalogRepository.findById(id);

        if (found.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Catalog catalog = found.get();

        for (CatalogImage image : catalog.getImages()) {
            try {
                cloudinary.uploader().destroy(image.getPublicId(), ObjectUtils.emptyMap());
            } catch (Exception e) {
                // vazhdo edhe nese Cloudinary deshton per nje foto
            }
        }

        catalogRepository.delete(catalog);
        return ResponseEntity.ok().body(Map.of("deleted", true));
    }

    // Vetem admin - fshin nje foto te vetme nga nje katalog
    @DeleteMapping("/{catalogId}/images/{imageId}")
    public ResponseEntity<?> deleteCatalogImage(
            @PathVariable Long catalogId,
            @PathVariable Long imageId
    ) {
        Optional<Catalog> found = catalogRepository.findById(catalogId);

        if (found.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Catalog catalog = found.get();

        CatalogImage toRemove = catalog.getImages().stream()
                .filter(img -> img.getId().equals(imageId))
                .findFirst()
                .orElse(null);

        if (toRemove == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            cloudinary.uploader().destroy(toRemove.getPublicId(), ObjectUtils.emptyMap());
        } catch (Exception e) {
            // vazhdo gjithsesi
        }

        catalog.getImages().remove(toRemove);
        catalogRepository.save(catalog);

        return ResponseEntity.ok(catalog);
    }
}