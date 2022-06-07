package com.pesco.operator.ranger.crd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Arrays;

/**
 * @Title ranger服务状态
 * @Company 尚源智慧科技有限公司
 * @Author 陈翔
 * @Package com.pesco.operator.ranger.crd
 * @Description
 * @Date 2022/6/7 4:32 下午
 * @Version V1.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@RegisterForReflection
public class RangerStatus {

    private Integer activeReplicas = 0;

    private String deploymentName;

    /**
     * 这里的configmap是指生成的ranger-admin-site.xml的名称，而不是引用的名称
     */
    private String configMapName;

    private String serviceName;

    private String errorMsg;

    private String[] stackTrace;

    public Integer getActiveReplicas() {
        return activeReplicas;
    }

    public void setActiveReplicas(Integer activeReplicas) {
        this.activeReplicas = activeReplicas;
    }

    public String getDeploymentName() {
        return deploymentName;
    }

    public void setDeploymentName(String deploymentName) {
        this.deploymentName = deploymentName;
    }

    public String getConfigMapName() {
        return configMapName;
    }

    public void setConfigMapName(String configMapName) {
        this.configMapName = configMapName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
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
        return "RangerStatus{" +
                "activeReplicas=" + activeReplicas +
                ", deploymentName='" + deploymentName + '\'' +
                ", configMapName='" + configMapName + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", errorMsg='" + errorMsg + '\'' +
                ", stackTrace=" + Arrays.toString(stackTrace) +
                '}';
    }
}
