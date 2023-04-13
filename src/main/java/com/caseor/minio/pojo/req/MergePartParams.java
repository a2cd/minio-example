package com.caseor.minio.pojo.req;

import lombok.Data;

/**
 * @author Fu Kai
 * @since 20230408
 * 合并分片请求参数
 */

@Data
public class MergePartParams {
    private String md5;
    /**
     * 文件名称
     */
    private String objectName;
    /**
     * 文件大小
     */
    private Integer objectSize;
    /**
     * 上传编号
     */
    private String uploadId;
    /**
     * 分片大小
     */
    private Integer partSize;
    /**
     * 文件类型
     */
    private String contentType;
    /**
     * 密码
     */
    private String passwd;
    /**
     * 超时时间
     */
    private Integer expire;
    /**
     * 最大下载数
     */
    private Integer maxGetCount;
}
