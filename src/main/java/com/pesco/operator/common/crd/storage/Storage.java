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

    private Boolean create = true;

    private String volumeClaimSize;

    public String getStorageClassName() {
        return storageClassName;
    }

    public void setStorageClassName(String storageClassName) {
        this.storageClassName = storageClassName;
    }

    public Boolean getCreate() {
        return create;
    }

    public void setCreate(Boolean create) {
        this.create = create;
    }

    public String getVolumeClaimSize() {
        return volumeClaimSize;
    }

    public void setVolumeClaimSize(String volumeClaimSize) {
        this.volumeClaimSize = volumeClaimSize;
    }

    @Override
    public String toString() {
        return "Storage{" +
                "storageClassName='" + storageClassName + '\'' +
                ", create=" + create +
                ", volumeClaimSize='" + volumeClaimSize + '\'' +
                '}';
    }
}
