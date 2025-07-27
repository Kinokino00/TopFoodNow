package com.example.topfoodnow.service.impl;

import com.example.topfoodnow.service.GcsService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import com.google.cloud.storage.*;
import lombok.RequiredArgsConstructor;
import java.io.IOException;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class GcsServiceImpl implements GcsService {
    private final Storage storage;

    @Value("${gcs.bucket.name}")
    private String bucketName;

    public String uploadFile(MultipartFile file, String folderPath) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String fullPath = folderPath + fileName;

        BlobId blobId = BlobId.of(bucketName, fullPath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                // 設定為公開讀取權限
                .setAcl(Collections.singletonList(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER)))
                .build();

        Blob blob = storage.create(blobInfo, file.getBytes());
        return blob.getMediaLink();
    }

    public void deleteFile(String fileUrl) {
        String path = fileUrl.substring(fileUrl.indexOf(bucketName) + bucketName.length() + 1);
        BlobId blobId = BlobId.of(bucketName, path);
        storage.delete(blobId);
    }
}