package com.gtalent.helloworld.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import com.gtalent.helloworld.domain.model.FileMetadata;
import com.gtalent.helloworld.domain.model.StoredFile;
import com.gtalent.helloworld.repository.FileMetadataRepository;
import com.gtalent.helloworld.repository.StoredFileRepository;

@Service
public class FileSystemStorageService implements StorageService {

    private final Path rootLocation;
    private final StoredFileRepository storedFileRepository;
    private final FileMetadataRepository fileMetadataRepository;

    @Autowired
    public FileSystemStorageService(StorageProperties properties,
                                    StoredFileRepository storedFileRepository,
                                    FileMetadataRepository fileMetadataRepository) {
        if (properties.getLocation().trim().length() == 0) {
            throw new StorageException("File upload location can not be Empty.");
        }
        this.rootLocation = Paths.get(properties.getLocation());
        this.storedFileRepository = storedFileRepository;
        this.fileMetadataRepository = fileMetadataRepository;
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }

    @Override
    @Transactional
    public FileMetadata store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file.");
            }

            byte[] bytes = file.getBytes();
            String hash = sha256Hex(bytes);

            String originalFilename = file.getOriginalFilename();
            String extension = (originalFilename != null && originalFilename.contains("."))
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";

            StoredFile storedFile = storedFileRepository.findByHash(hash)
                    .orElseGet(() -> {
                        String storedPath = hash + extension;
                        Path dest = rootLocation.resolve(storedPath).normalize().toAbsolutePath();
                        if (!dest.getParent().equals(rootLocation.toAbsolutePath())) {
                            throw new StorageException("Cannot store file outside current directory.");
                        }
                        try {
                            Files.write(dest, bytes);
                        } catch (IOException e) {
                            throw new StorageException("Failed to write file.", e);
                        }
                        StoredFile sf = new StoredFile();
                        sf.setHash(hash);
                        sf.setPath(storedPath);
                        return storedFileRepository.save(sf);
                    });

            FileMetadata metadata = new FileMetadata();
            metadata.setStoredFile(storedFile);
            metadata.setOriginalName(originalFilename);
            metadata.setContentType(file.getContentType());
            metadata.setFileSize(file.getSize());
            return fileMetadataRepository.save(metadata);

        } catch (IOException e) {
            throw new StorageException("Failed to store file.", e);
        }
    }

    @Override
    @Transactional
    public void deleteFile(Long metadataId) {
        FileMetadata metadata = fileMetadataRepository.findById(metadataId)
                .orElseThrow(() -> new StorageFileNotFoundException("File not found: " + metadataId));
        StoredFile storedFile = metadata.getStoredFile();
        fileMetadataRepository.delete(metadata);

        if (fileMetadataRepository.countByStoredFile(storedFile) == 0) {
            Path filePath = rootLocation.resolve(storedFile.getPath()).normalize().toAbsolutePath();
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                throw new StorageException("Could not delete physical file: " + storedFile.getPath(), e);
            }
            storedFileRepository.delete(storedFile);
        }
    }

    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    private String sha256Hex(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new StorageException("SHA-256 algorithm not available", e);
        }
    }
}
