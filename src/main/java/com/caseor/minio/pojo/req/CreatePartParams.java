package com.caseor.minio.pojo.req;

import lombok.Data;

/**
 * @author Fu Kai
 * @since 20230408
 * 创建分片请求参数
 */

@Data
public class CreatePartParams {
  /**
   * 对象名
   */
  private String objectName;
  /**
   * 分片数量
   */
  private Integer partCount;

  private String uploadId;
}
