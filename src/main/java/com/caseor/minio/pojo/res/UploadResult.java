package com.caseor.minio.pojo.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author
 * @since 2023-04-12
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadResult {
    private String presignedUploadUrl;
}
