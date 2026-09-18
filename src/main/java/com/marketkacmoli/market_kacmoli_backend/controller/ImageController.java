package com.marketkacmoli.market_kacmoli_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
//@CrossOrigin(origins = "*")
public class ImageController {

    private static final String UPLOAD_DIR = "uploads/";

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(
            @RequestParam("file") MultipartFile file
    ) {

        try {

            File uploadDirectory = new File(UPLOAD_DIR);

            if (!uploadDirectory.exists()) {
                uploadDirectory.mkdirs();
            }

            String originalFileName = file.getOriginalFilename();

            String extension = "";

            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(
                        originalFileName.lastIndexOf(".")
                );
            }

            String fileName =
                    UUID.randomUUID().toString() + extension;

            Path filePath = Paths.get(
                    UPLOAD_DIR + fileName
            );

            Files.write(
                    filePath,
                    file.getBytes()
            );

            return ResponseEntity.ok(
                    "/uploads/" + fileName
            );

        } catch (IOException e) {

            return ResponseEntity
                    .internalServerError()
                    .body("Gabim gjate upload-it te fotos");
        }
    }
}