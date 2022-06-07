package com.pesco.operator.hadoop.folder.crd;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Arrays;

/**
 * @Title HadoopConfigFolder的状态
 * @Company 尚源智慧科技有限公司
 * @Author 陈翔
 * @Package com.pesco.operator.hadoop.folder
 * @Description
 * @Date 2022/6/7 10:21 上午
 * @Version V1.0
 */
@RegisterForReflection
public class HadoopConfigFolderStatus {

    /**
     * 资源的名称
     */
    private String resourceName;

    /**
     * 文件列表
     */
    private String[] fileList;

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

    public String[] getFileList() {
        return fileList;
    }

    public void setFileList(String[] fileList) {
        this.fileList = fileList;
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
        return "HadoopConfigFolderStatus{" +
                "resourceName='" + resourceName + '\'' +
                ", fileList=" + Arrays.toString(fileList) +
                ", success=" + success +
                ", errorMsg='" + errorMsg + '\'' +
                ", stackTrace=" + Arrays.toString(stackTrace) +
                '}';
    }
}
