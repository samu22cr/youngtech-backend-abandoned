package com.youngtechcr.www.exceptions.custom;

import com.youngtechcr.www.exceptions.ErrorCode;
import com.youngtechcr.www.exceptions.ICustomRuntimeException;
import org.springframework.http.HttpStatus;

public class ParameterValueAndRequestBodyMismatchException extends RuntimeException implements ICustomRuntimeException {

    private String title = "Bad request parameters exception";
    private HttpStatus status = HttpStatus.BAD_REQUEST;
    private ErrorCode errorCode = ErrorCode.getCode(4);

    public ParameterValueAndRequestBodyMismatchException(String detail) {
        super(detail);
    }

    @Override
    public String getDefaultTitle() {
        return this.title;
    }

    @Override
    public ErrorCode getCustomErrorCode() {
        return this.errorCode;
    }

    @Override
    public HttpStatus getDefaultHttpStatus() {
        return this.status;
    }

    @Override
    public String getDetail() {
        return this.getMessage();
    }
}
