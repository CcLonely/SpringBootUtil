package com.example.bootutil.vo;


import java.io.Serializable;


public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 成功标志
     * （2020-12-28 11:09 gz.zhang 恢复success属性， 前端页面需要。
     * Result职责不单一，httpapi和rpcapi都在用，先维持现状。）
     */
    private boolean success = true;

    /**
     * 返回处理消息
     */
    private String message = "操作成功！";


    /**
     * 时间戳
     */
    private long timestamp = System.currentTimeMillis();

    public Result() {
    }

    public static <T> Result<T> successWithMsg(String message){
        Result<T> result=new Result<>();
        result.message = message;
        return result;
    }

    public static <T> Result<T> err( String msg) {
        Result<T> result=new Result<>();
        result.success = false;
        result.message = msg;
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}