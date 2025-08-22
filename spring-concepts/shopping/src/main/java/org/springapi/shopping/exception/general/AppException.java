package org.springapi.shopping.exception.general;

import org.springframework.http.HttpStatus;

public class AppException extends RuntimeException {

  private final String title;
  private final HttpStatus status;

  public AppException(String message, String title, HttpStatus status) {
    super(message);
    this.title = title;
    this.status = status;
  }

  public String getTitle() {
    return title;
  }

  public HttpStatus getStatus() {
    return status;
  }

}
