package com.db.mdm.gestionale.be.service;

import org.springframework.web.multipart.MultipartFile;

public interface SupabaseS3Service {
    String uploadFile(MultipartFile file, String path) throws Exception;
    void deleteFile(String path);
    String getPublicUrl(String path);
    byte[] downloadFile(String path) throws Exception;
    String getTotalStorageUsagePretty();
	String uploadBytes(byte[] data, String path, String contentType) throws Exception;
}
