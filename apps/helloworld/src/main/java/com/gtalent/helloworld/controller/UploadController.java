package com.gtalent.helloworld.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gtalent.helloworld.domain.model.FileMetadata;
import com.gtalent.helloworld.service.StorageService;

@RestController
@RequestMapping("v1")
public class UploadController {

    @Autowired
    private StorageService storageService;

    @PostMapping("/upload")
    public ResponseEntity<FileMetadata> upload(@RequestParam("file") MultipartFile file) {
        FileMetadata metadata = storageService.store(file);
        return ResponseEntity.ok(metadata);
    }
}
