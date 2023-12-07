package com.BaGulBaGul.BaGulBaGul.global.upload.service;

import com.BaGulBaGul.BaGulBaGul.global.upload.Resource;
import com.BaGulBaGul.BaGulBaGul.global.upload.S3TempResource;
import com.BaGulBaGul.BaGulBaGul.global.upload.constant.StorageVendor;
import com.BaGulBaGul.BaGulBaGul.global.upload.exception.ResourceNotFoundException;
import com.BaGulBaGul.BaGulBaGul.global.upload.repository.ResourceRepository;
import com.BaGulBaGul.BaGulBaGul.global.upload.repository.S3TempResourceRepository;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class S3ResourceService extends ResourceService {

    private final AmazonS3 amazonS3;
    private final ResourceRepository resourceRepository;
    private final S3TempResourceRepository s3TempResourceRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Override
    @Transactional
    public Long uploadResource(String path, MultipartFile multipartFile) throws IOException {
        String key = createKey(path, multipartFile.getOriginalFilename());
        //먼저 db에 업로드
        Resource resource = resourceRepository.save(
                Resource.builder()
                        .key(key)
                        .storageVendor(StorageVendor.S3)
                        .uploadTime(LocalDateTime.now())
                        .build()
        );
        s3TempResourceRepository.save(
                S3TempResource.builder()
                        .resource(resource)
                        .uploadTime(LocalDateTime.now())
                        .build()
        );
        resourceRepository.flush();

        //s3 업로드
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());
        amazonS3.putObject(bucketName, key, multipartFile.getInputStream(), metadata);

        return resource.getId();
    }

    @Override
    public void deleteResource(Long resourceId) {
        Resource resource = resourceRepository.findById(resourceId).orElseThrow(() -> new ResourceNotFoundException());
        String key = resource.getKey();
        resourceRepository.delete(resource);
        amazonS3.deleteObject(bucketName, key);
    }

    @Override
    @Async
    public void deleteResourcesAsync(List<Long> resourceIds) {
        if(resourceIds == null)
            return;
        List<Resource> resources = resourceRepository.findAllById(resourceIds);
        List<String> keys = resources.stream().map(Resource::getKey).collect(Collectors.toList());
        resourceRepository.deleteAllByIdInBatch(resourceIds);
        for(String key : keys) {
            amazonS3.deleteObject(bucketName, key);
        }
    }

    @Override
    public String getResourceUrlFromId(Long resourceId) {
        Resource resource = resourceRepository.findById(resourceId).orElseThrow(() -> new ResourceNotFoundException());
        String key = resource.getKey();
        return getUrlFromKey(key);
    }

    @Override
    public List<String> getResourceUrlsFromIds(List<Long> resourceIds) {
        //한번에 받아옴
        resourceRepository.findAllByIds(resourceIds);
        //순서 유지하면서 url로 변환
        List<String> urls = new ArrayList<>();
        for(Long id:resourceIds) {
            Resource resource = resourceRepository.findById(id).orElse(null);
            if(resource == null)
                urls.add(null);
            else
                urls.add(getUrlFromKey(resource.getKey()));
        }
        return urls;
    }

    @Override
    public void cancelTempResource(Long resourceId) {
        s3TempResourceRepository.deleteById(resourceId);
    }

    @Override
    public void cancelTempResources(List<Long> resourceIds) {
        s3TempResourceRepository.deleteAllByIdInBatch(resourceIds);
    }

    private String getUrlFromKey(String key) {
        return amazonS3.getUrl(bucketName, key).toString();
    }
}