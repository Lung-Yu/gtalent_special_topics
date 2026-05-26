package com.gtalent.helloworld.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gtalent.helloworld.service.StorageService;

@RestController
@RequestMapping("v1")
public class UploadController {
    
    @Autowired
    private StorageService storageService;

    @PostMapping("/upload")
    public String upload(MultipartFile file) {
        // 這裡可以實現文件上傳的邏輯，例如保存文件到服務器或處理文件內容

        storageService.store(file);

        return "File uploaded successfully: " + file.getOriginalFilename() + " (" + file.getSize() + " bytes)";
    }

}
