package com.pesco.operator.common.crd.resource;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * @Title 资源情况
 * @Company 尚源智慧科技有限公司
 * @Author 陈翔
 * @Package com.pesco.operator.common.crd.resource
 * @Description
 * @Date 2022/6/7 4:49 下午
 * @Version V1.0
 */
@RegisterForReflection
public class Resource {

    private String cpu;

    private String memory;

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    @Override
    public String toString() {
        return "Resource{" +
                "cpu='" + cpu + '\'' +
                ", memory='" + memory + '\'' +
                '}';
    }
}
