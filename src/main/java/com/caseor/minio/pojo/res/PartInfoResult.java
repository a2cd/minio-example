package com.caseor.minio.pojo.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Fu Kai
 * @since 20230408
 * 查询分片结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartInfoResult {
    private Boolean exists;
    private String downloadUrl;
}
