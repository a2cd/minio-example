package com.caseor.minio.service;

import com.caseor.minio.pojo.req.CreatePartParams;
import com.caseor.minio.pojo.req.MergePartParams;
import com.caseor.minio.pojo.res.*;
import io.minio.ListPartsResponse;

import java.io.InputStream;
import java.util.List;

/**
 * @author Fu Kai
 * @since 20220408
 */

public interface OssService {
    void createBucket(String bucketName);

    void deleteBucket(String bucketName);

    /**
     * objectName为对象的全路径
     */
    void upload(String objectName, InputStream stream);

    UploadResult upload(String objectName);

    InputStream download(String objectName);

    void deleteObject(String objectName);

    /**
     * 分片上传 创建分片
     */
    CreatePartResult createParts(CreatePartParams params);

    /**
     * 分片上传 合并分片
     */
    MergePartResult mergeParts(MergePartParams params);

    List<Integer> queryPartInfo(String uploadId, String objectName);

    CreatePartResult calcUnuploadedParts(CreatePartParams params);

    FileExistsResult fileExists(String md5);

    PresignedUploadResult presignedUploadComplete(String objectName, String md5);
}
