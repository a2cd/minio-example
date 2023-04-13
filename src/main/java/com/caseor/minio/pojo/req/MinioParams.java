package com.caseor.minio.pojo.req;

import com.google.common.collect.Multimap;
import io.minio.messages.Part;
import lombok.Builder;
import lombok.Data;

/**
 * @author Fu Kai
 * @since 20230408
 */

@Data
@Builder
public class MinioParams {
    private String objectName;

    private String bucketName;

    private String region;

    private Multimap<String, String> headers;

    private Multimap<String, String> extraQueryParams;

    private String uploadId;

    private Integer maxParts;

    private Part[] parts;

    private Integer partNumberMarker;

}
