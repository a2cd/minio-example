package com.caseor.minio.pojo;

/**
 * @author Fu Kai
 * @since 20211019
 * Custom response code
 * 200, 400, 404, 500 for system exception
 * 50xxx for token exception
 * 51xxx for user exception
 * 52xxx for role exception
 * 53xxx for permission exception(menu and api)
 * 54xxx for config
 */

public enum ResCode {
  /**
   * The request is successful. It is generally used for get and post requests
   */
  SUCCESS(200, "SUCCESS"),
  /**
   * Invalid request. The request sent by the user has errors, and the server does not create or modify data
   */
  INVALID_REQUEST(400, "INVALID_REQUEST"),
  /**
   * Not found
   */
  NOT_FOUND(404, "NOT_FOUND"),
  /**
   * Request failed, server internal error
   */
  INTERNAL_SERVER_ERROR(500, "INTERNAL_SERVER_ERROR"),
  /*
   * Refresh jwt
   */
  JWT_REFRESH(50000, "JWT_REFRESH"),
  /*
   * Illegal jwt
   */
  JWT_ILLEGAL(50010, "JWT_ILLEGAL"),
  /**
   * Jwt has expired
   */
  JWT_EXPIRED(50020, "JWT_EXPIRED"),
  /**
   * The user are blocked to log in
   */
  USER_BLOCKED(51000, "USER_BLOCKED"),
  /**
   * Unauthorized
   */
  USER_UNAUTHORIZED(51010, "USER_UNAUTHORIZED"),
  /**
   * Unauthenticated
   */
  USER_UNAUTHENTICATED(51020, "USER_UNAUTHENTICATED"),
  /**
   * The user not exists
   */
  USER_NOT_FOUNT(51404, "USER_NOT_FOUNT"),
  /**
   * User role changes
   */
  ROLE_CHANGED(52000, "ROLE_CHANGED"),
  CONFIG_NOT_FOUNT(54000, "CONFIG_NOT_FOUNT");

  private final int code;
  private final String msg;

  ResCode(Integer code, String msg) {
    this.code = code;
    this.msg = msg;
  }

  public Integer getCode() {
    return code;
  }
  public String getMsg() {
    return msg;
  }

}
