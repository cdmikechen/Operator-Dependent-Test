package com.pesco.operator.common.crd.storage;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * @Title 存储相关的通用对象
 * @Company 尚源智慧科技有限公司
 * @Author 陈翔
 * @Package com.pesco.operator.common.crd.storage
 * @Description
 * @Date 2022/6/7 4:57 下午
 * @Version V1.0
 */

@RegisterForReflection
public class Storage {

    private String storageClassName;

    private String accessModes = "ReadWriteOnce";

    private Boolean expansion = false;

    private String volumeClaimSize;

    public String getStorageClassName() {
        return storageClassName;
    }

    public void setStorageClassName(String storageClassName) {
        this.storageClassName = storageClassName;
    }

    public String getVolumeClaimSize() {
        return volumeClaimSize;
    }

    public void setVolumeClaimSize(String volumeClaimSize) {
        this.volumeClaimSize = volumeClaimSize;
    }

    public String getAccessModes() {
        return accessModes;
    }

    public void setAccessModes(String accessModes) {
        this.accessModes = accessModes;
    }

    public Boolean getExpansion() {
        return expansion;
    }

    public void setExpansion(Boolean expansion) {
        this.expansion = expansion;
    }

    @Override
    public String toString() {
        return "Storage{" +
                "storageClassName='" + storageClassName + '\'' +
                ", accessModes='" + accessModes + '\'' +
                ", expansion=" + expansion +
                ", volumeClaimSize='" + volumeClaimSize + '\'' +
                '}';
    }
}
