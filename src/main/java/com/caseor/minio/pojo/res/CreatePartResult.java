package com.caseor.minio.pojo.res;

import lombok.Data;

import java.util.List;

/**
 * @author Fu Kai
 * @since 20230408
 * 创建分片返回结果
 */
@Data
public class CreatePartResult {
    /**
     * 上传编号
     */
    private String uploadId;

    /**
     * 分片信息
     */
    private List<PartItem> parts;


    @Data
    public static class PartItem {
        /**
         * 分片id
         */
        private Integer partNumber;
        /**
         * 上传地址
         */
        private String uploadUrl;
    }
}
