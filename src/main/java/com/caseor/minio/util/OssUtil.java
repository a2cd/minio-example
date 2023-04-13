package com.caseor.minio.util;

/**
 * @author
 * @since 2023-03-23
 */

public class OssUtil {

    public static String getExt(String objectName) {
        String ext = "";
        int extIndex = objectName.lastIndexOf(".");
        if (extIndex != -1) {
            ext = objectName.substring(extIndex);
        }
        return ext;
    }

}
