package com.pesco.operator.hadoop.config.crd;

import java.util.Arrays;

public class HadoopConfigStatus {

    /**
     * 资源的名称
     */
    private String resourceName;

    /**
     * 实际的文件内容
     */
    private String realContent;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 异常信息
     */
    private String errorMsg;

    /**
     * 错误堆栈
     */
    private String[] stackTrace;

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getRealContent() {
        return realContent;
    }

    public void setRealContent(String realContent) {
        this.realContent = realContent;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String[] getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String[] stackTrace) {
        this.stackTrace = stackTrace;
    }

    @Override
    public String toString() {
        return "HadoopConfigStatus{" +
                "resourceName='" + resourceName + '\'' +
                ", realContent='" + realContent + '\'' +
                ", success=" + success +
                ", errorMsg='" + errorMsg + '\'' +
                ", stackTrace=" + Arrays.toString(stackTrace) +
                '}';
    }
}
