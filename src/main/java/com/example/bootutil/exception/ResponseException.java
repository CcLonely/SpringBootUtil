package com.example.bootutil.exception;

public class ResponseException extends RuntimeException {

    private static final long serialVersionUID = -973741250742004561L;

    public ResponseException() {
        super();
    }

    public ResponseException(String message) {
        super(message);
    }

    //http://kgd1120.iteye.com/blog/1293633 java字符串格式化：String.format()方法的使用
    public ResponseException(String format, Object... arguments) {
        super(String.format(format, arguments));
    }

}
