package com.gtalent.helloworld.service;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

public interface StorageService {

    void init();

    void store(MultipartFile file);

    Resource loadAsResource(String filename);

    void deleteAll();

}