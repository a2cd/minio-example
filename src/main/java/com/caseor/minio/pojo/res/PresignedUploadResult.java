package com.caseor.minio.pojo.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Fu Kai
 * @since 20230408
 * 合并分片返回结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresignedUploadResult {
    private String downloadUrl;
}
