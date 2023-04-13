package com.caseor.minio.service;

import cn.hutool.core.util.StrUtil;
import com.caseor.minio.pojo.req.CreatePartParams;
import com.caseor.minio.pojo.res.*;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Part;
import lombok.extern.slf4j.Slf4j;
import com.caseor.minio.config.CustomizedMinioClient;
import com.caseor.minio.config.MinioProperties;
import com.caseor.minio.pojo.req.MergePartParams;
import com.caseor.minio.pojo.req.MinioParams;
import com.caseor.minio.util.OssUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Fu Kai
 * @since 20230322
 */
@Slf4j
@Service
public class OssServiceImpl implements OssService {

    @Resource
    private CustomizedMinioClient customizedMinioClient;

    @Resource
    private MinioProperties minioProperties;

    /**
     * 模拟数据库存储文件md5与下载路径
     */
    private static final Map<String, String> FILE_MAP = new ConcurrentHashMap<>();

    @Override
    public CreatePartResult createParts(CreatePartParams params) {
        log.info("创建分片: {}", params);
        // 获取uploadId
        MinioParams minioParams = MinioParams.builder()
            .bucketName(minioProperties.getBucket())
            .objectName(params.getObjectName())
            .build();
        CreateMultipartUploadResponse registerUploadResponse = registerMultipartUpload(minioParams);

        CreatePartResult result = new CreatePartResult();
        result.setParts(new ArrayList<>());
        result.setUploadId(registerUploadResponse.result().uploadId());

        // 根据前端的分片数量生成分片的上传地址
        Map<String, String> queryParams = new HashMap<>(4);
        queryParams.put("uploadId", result.getUploadId());
        // 一定要注意partNumber从1开始, 从0开始会丢失0这个分片
        for (int i = 1; i <= params.getPartCount(); i++) {
            queryParams.put("partNumber", String.valueOf(i));
            String presignedObjectUrl = getPresignedObjectUrl(minioParams, queryParams);
            // 如果为minio做了Nginx代理, 则使用Nginx代理地址
            if (!StrUtil.isBlank(minioProperties.getEndpointProxy())) {
                presignedObjectUrl = presignedObjectUrl.replace(minioProperties.getEndpoint(), minioProperties.getEndpointProxy());
            }
            CreatePartResult.PartItem item = new CreatePartResult.PartItem();
            item.setPartNumber(i);
            item.setUploadUrl(presignedObjectUrl);
            result.getParts().add(item);
        }
        log.info("uploadId: {}", result.getUploadId());
        log.info("objectName: {}", params.getObjectName());
        log.info("partCount: {}", params.getPartCount());
        return result;
    }

    /**
     * 向minio注册分片上传
     */
    private CreateMultipartUploadResponse registerMultipartUpload(MinioParams params) {
        try {
            return customizedMinioClient.createMultipartUpload(params.getBucketName(), params.getRegion(), params.getObjectName(), params.getHeaders(), params.getExtraQueryParams());
        } catch (Exception e) {
            log.error("获取上传编号失败", e);
            throw new RuntimeException(e.getCause());
        }
    }

    /**
     * 获取预上传地址
     */
    private String getPresignedObjectUrl(MinioParams params, Map<String, String> queryParams) {
        try {
            return customizedMinioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket(params.getBucketName())
                    .object(params.getObjectName())
                    .expiry(1, TimeUnit.DAYS)
                    .extraQueryParams(queryParams)
                    .build());
        } catch (Exception e) {
            log.error("查询分片失败", e);
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    public MergePartResult mergeParts(MergePartParams params) {
        log.info("合并分片: {}", params);
        MinioParams minioParams = MinioParams.builder()
            .bucketName(minioProperties.getBucket())
            .uploadId(params.getUploadId())
            .objectName(params.getObjectName())
            // 查询最大分片数, 默认1000
            // .maxParts(params.getPartSize())
            // 指定List的起始位置, 只有Part Number数目大于该参数的Part会被列出
            .partNumberMarker(0)
            .build();
        // 查询分片
        ListPartsResponse listPartsRes = listParts(minioParams);
        // 合并
        minioParams.setParts(listPartsRes.result().partList().toArray(new Part[]{}));
        doMergePartition(minioParams);
        log.info("合并分片完成");

        String downloadUrl = minioProperties.getEndpoint();
        if (!StrUtil.isBlank(minioProperties.getEndpointProxy())) {
            downloadUrl = minioProperties.getEndpointProxy();
        }
        downloadUrl = downloadUrl + "/" + minioProperties.getBucket() + "/" + params.getObjectName();

        // 模拟数据库存储md5
        FILE_MAP.put(params.getMd5(), downloadUrl);

        MergePartResult result = new MergePartResult();
        result.setDownloadUrl(downloadUrl);
        return result;
    }

    private ListPartsResponse listParts(MinioParams params) {
        try {
            return customizedMinioClient.listParts(params.getBucketName(), params.getRegion(), params.getObjectName(), params.getMaxParts(), params.getPartNumberMarker(), params.getUploadId(), params.getHeaders(), params.getExtraQueryParams());
        } catch (Exception e) {
            log.error("查询分片异常", e);
            throw new RuntimeException(e.getCause());
        }
    }

    private ObjectWriteResponse doMergePartition(MinioParams params) {
        try {
            return customizedMinioClient.completeMultipartUpload(params.getBucketName(), params.getRegion(), params.getObjectName(), params.getUploadId(), params.getParts(), params.getHeaders(), params.getExtraQueryParams());
        } catch (Exception e) {
            log.error("合并分片异常", e);
            throw new RuntimeException(e.getCause());
        }
    }

    /**
     * 创建bucket
     */
    public void createBucket(String bucketName) {
        try {
            customizedMinioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            log.error("createBucket error: ", e.getCause());
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    public void upload(String objectName, InputStream stream) {
        validateBucket(minioProperties.getBucket());
        String ext = OssUtil.getExt(objectName);
        try {
            customizedMinioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(objectName)
                    .stream(stream, stream.available(), -1)
                    .contentType(ext)
                    .build()
            );
        } catch (Exception e) {
            log.error("upload error: ", e.getCause());
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    public UploadResult upload(String objectName) {
        // TODO 检验objectName是否存在

        MinioParams params = MinioParams.builder()
            .bucketName(minioProperties.getBucket())
            .objectName(objectName)
            .build();

        String presignedUploadUrl = getPresignedObjectUrl(params, null);
        return new UploadResult(presignedUploadUrl);
    }

    private boolean isBucketExist(String bucketName) {
        boolean isBucketExists = false;
        try {
            isBucketExists = customizedMinioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception ignored) {
        }
        return isBucketExists;
    }

    private void validateBucket(String bucketName) {
        if (!isBucketExist(bucketName)) {
            throw new NoSuchElementException("bucket not found");
        }
    }

    @Override
    public void deleteBucket(String bucketName) {
        try {
            customizedMinioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            log.error("delete bucket error: ", e.getCause());
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    public InputStream download(String objectName) {
        try {
            return customizedMinioClient.getObject(GetObjectArgs.builder()
                .bucket(minioProperties.getBucket())
                .object(objectName)
                .build());
        } catch (Exception e) {
            log.error("download file error: ", e.getCause());
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    public void deleteObject(String objectName) {
        try {
            customizedMinioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(minioProperties.getBucket())
                .object(objectName)
                .build());
        } catch (Exception e) {
            log.error("delete file error: ", e.getCause());
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    public List<Integer> queryPartInfo(String uploadId, String objectName) {
        MinioParams minioParams = MinioParams.builder()
            .bucketName(minioProperties.getBucket())
            .uploadId(uploadId)
            .objectName(objectName)
            // 指定List的起始位置, 只有Part Number数目大于该参数的Part会被列出
            .partNumberMarker(0)
            .build();
        // bug partNumber为0的Part信息没办法返回
        ListPartsResponse listPartsResponse = listParts(minioParams);
        List<Integer> uploadedParts = new ArrayList<>();
        for (Part part : listPartsResponse.result().partList()) {
            uploadedParts.add(part.partNumber());
        }
        return uploadedParts;
    }

    @Override
    public CreatePartResult calcUnuploadedParts(CreatePartParams params) {
        List<Integer> uploadedParts = queryPartInfo(params.getUploadId(), params.getObjectName());
        Set<Integer> set = new HashSet<>(uploadedParts);

        MinioParams minioParams = MinioParams.builder()
            .bucketName(minioProperties.getBucket())
            .objectName(params.getObjectName())
            .build();

        CreatePartResult result = new CreatePartResult();
        result.setParts(new ArrayList<>());
        result.setUploadId(params.getUploadId());

        // 根据前端的分片数量生成分片的上传地址
        Map<String, String> queryParams = new HashMap<>(4);
        queryParams.put("uploadId", result.getUploadId());
        for (int i = 1; i <= params.getPartCount(); i++) {
            if (set.contains(i)) {
                continue;
            }
            queryParams.put("partNumber", String.valueOf(i));
            String presignedObjectUrl = getPresignedObjectUrl(minioParams, queryParams);
            // 如果为minio做了Nginx代理, 则使用Nginx代理地址
            if (!StrUtil.isBlank(minioProperties.getEndpointProxy())) {
                presignedObjectUrl = presignedObjectUrl.replace(minioProperties.getEndpoint(), minioProperties.getEndpointProxy());
            }
            CreatePartResult.PartItem item = new CreatePartResult.PartItem();
            item.setPartNumber(i);
            item.setUploadUrl(presignedObjectUrl);
            result.getParts().add(item);
        }
        return result;
    }

    @Override
    public FileExistsResult fileExists(String md5) {
        String downloadUrl = FILE_MAP.get(md5);
        if (downloadUrl == null) {
            return new FileExistsResult(false, null);
        }
        return new FileExistsResult(true, downloadUrl);
    }

    @Override
    public PresignedUploadResult presignedUploadComplete(String objectName, String md5) {
        log.info("objectName: '{}' upload complete", objectName);
        String downloadUrl = minioProperties.getEndpoint();
        if (!StrUtil.isBlank(minioProperties.getEndpointProxy())) {
            downloadUrl = minioProperties.getEndpointProxy();
        }
        downloadUrl = downloadUrl + "/" + minioProperties.getBucket() + "/" + objectName;
        FILE_MAP.put(md5, downloadUrl);
        return new PresignedUploadResult(downloadUrl);
    }
}
