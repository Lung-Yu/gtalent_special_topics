package com.gtalent.helloworld.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("v1")
public class UploadController {
    

    @PostMapping("/upload")
    public String upload(MultipartFile file) {
        // 這裡可以實現文件上傳的邏輯，例如保存文件到服務器或處理文件內容



        return "File uploaded successfully: " + file.getOriginalFilename() + " (" + file.getSize() + " bytes)";
    }

}
