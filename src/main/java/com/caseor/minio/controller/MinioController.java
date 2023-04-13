package com.caseor.minio.controller;

import com.caseor.minio.pojo.req.CreatePartParams;
import com.caseor.minio.pojo.res.*;
import io.minio.ListPartsResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.caseor.minio.pojo.ApiRes;
import com.caseor.minio.pojo.req.MergePartParams;
import com.caseor.minio.service.OssService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author Fu Kai
 * @since 20220314
 */

@Slf4j
@RestController
@RequestMapping("/minio")
@AllArgsConstructor
public class MinioController {

    private OssService ossService;

    @GetMapping("/v1/hello")
    public ApiRes<String> hello() {
        return ApiRes.success("hello");
    }

    @PostMapping("/v1/upload")
    public ApiRes<Void> upload(MultipartFile file) throws IOException {
        validateFile(file);
        ossService.upload(file.getOriginalFilename(), file.getInputStream());
        return ApiRes.success();
    }

    private void validateFile(MultipartFile file) {
        //判断文件是否为空
        if (null == file || 0 == file.getSize()) {
            throw new IllegalArgumentException("file cannot be empty");
        }
        if (file.getOriginalFilename() == null) {
            throw new IllegalArgumentException("file name cannot be null");
        }
    }

    @GetMapping("/v1/presigned-upload")
    public ApiRes<UploadResult> presignedUpload(@RequestParam String objectName) {
        return ApiRes.success(ossService.upload(objectName));
    }


    @PostMapping("/v1/presigned-upload/complete")
    public ApiRes<PresignedUploadResult> presignedUploadComplete(@RequestParam String objectName, @RequestParam String md5) {
        return ApiRes.success(ossService.presignedUploadComplete(objectName, md5));
    }

    @GetMapping("/v1/download")
    public ApiRes<String> download() {
        InputStream in = ossService.download("/avatar/yasuo.png");
        File file = new File("F:\\yasuo.png");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            int read;
            byte[] bytes = new byte[1024];
            while ((read = in.read(bytes)) != -1) {
                fos.write(bytes, 0, read);
            }
        } catch (Exception e) {
            log.error("download error: ", e.getCause());
            throw new RuntimeException(e.getCause());
        }
        return ApiRes.success("complete");
    }

    @PostMapping("/v1/parts/create")
    public ApiRes<CreatePartResult> createPartition(@RequestBody CreatePartParams params) {
        return ApiRes.success(ossService.createParts(params));
    }


    @PostMapping("/v1/parts/merge")
    public ApiRes<MergePartResult> mergePartition(@RequestBody MergePartParams params) {
        return ApiRes.success(ossService.mergeParts(params));
    }

    @PostMapping("/v1/parts/info/{uploadId}")
    public ApiRes<List<Integer>> partInfo(@PathVariable String uploadId, @RequestParam String objectName) {
        return ApiRes.success(ossService.queryPartInfo(uploadId, objectName));
    }


    @GetMapping("/v1/file-exists/{md5}")
    public ApiRes<FileExistsResult> fileExists(@PathVariable String md5) {
        return ApiRes.success(ossService.fileExists(md5));
    }


}
