package com.caseor.minio.pojo;

import lombok.Getter;
import lombok.ToString;


/**
 * @author Fu Kai
 * @since 20220411
 * Default code is success if HttpCode not set
 * Default msg is current datetime is msg not set
 */
@Getter
@ToString
public final class ApiRes<T> {

  private final boolean success;

  private final int code;

  private final T data;

  private final String msg;

  private ApiRes(boolean success, int code, T data, String msg) {
    this.success = success;
    this.code = code;
    this.data = data;
    this.msg = msg;
  }

  public static <T> ApiRes<T> of(ResCode resCode) {
    boolean success = ResCode.SUCCESS.equals(resCode);
    return new ApiRes<>(success, resCode.getCode(), null, resCode.getMsg());
  }

  public static <T> ApiRes<T> of(ResCode resCode, String msg) {
    boolean success = ResCode.SUCCESS.equals(resCode);
    return new ApiRes<>(success, resCode.getCode(), null, msg);
  }

  public static <T> ApiRes<T> of(ResCode resCode, T data) {
    boolean success = ResCode.SUCCESS.equals(resCode);
    return new ApiRes<>(success, resCode.getCode(), data, resCode.getMsg());
  }

  public static <T> ApiRes<T> of(ResCode resCode, T data, String msg) {
    boolean success = ResCode.SUCCESS.equals(resCode);
    return new ApiRes<>(success, resCode.getCode(), data, msg);
  }

  public static <T> ApiRes<T> success() {
    return new ApiRes<>(true, ResCode.SUCCESS.getCode(), null, ResCode.SUCCESS.getMsg());
  }

  public static <T> ApiRes<T> success(T data) {
    return new ApiRes<>(true, ResCode.SUCCESS.getCode(), data, ResCode.SUCCESS.getMsg());
  }

  public static <T> ApiRes<T> success(String msg) {
    return new ApiRes<>(true, ResCode.SUCCESS.getCode(), null, msg);
  }

  public static <T> ApiRes<T> success(T data, String msg) {
    return new ApiRes<>(true, ResCode.SUCCESS.getCode(), data, msg);
  }

  public static <T> ApiRes<T> failure() {
    return new ApiRes<>(false, ResCode.INVALID_REQUEST.getCode(), null, ResCode.INVALID_REQUEST.getMsg());
  }

  public static <T> ApiRes<T> failure(T data) {
    return new ApiRes<>(false, ResCode.INVALID_REQUEST.getCode(), data, ResCode.INVALID_REQUEST.getMsg());
  }

  public static <T> ApiRes<T> failure(String msg) {
    return new ApiRes<>(false, ResCode.INVALID_REQUEST.getCode(), null, msg);
  }

  public static <T> ApiRes<T> failure(T data, String msg) {
    return new ApiRes<>(false, ResCode.INVALID_REQUEST.getCode(), data, msg);
  }

}
