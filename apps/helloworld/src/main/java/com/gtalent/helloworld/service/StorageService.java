package com.gtalent.helloworld.service;

import com.gtalent.helloworld.domain.model.FileMetadata;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    void init();

    FileMetadata store(MultipartFile file);

    Resource loadAsResource(String filename);

    void deleteAll();

    void deleteFile(Long metadataId);
}
