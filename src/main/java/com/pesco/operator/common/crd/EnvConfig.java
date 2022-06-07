package com.pesco.operator.common.crd;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * @Title 环境变量对象
 * @Company 尚源智慧科技有限公司
 * @Author 陈翔
 * @Package com.pesco.operator.common.crd
 * @Description
 * @Date 2022/4/29 10:14 上午
 * @Version V1.0
 */
@RegisterForReflection
public class EnvConfig {

    private String name;

    private String value;

    public EnvConfig(String name, String value) {
        this.value = value;
        this.name = name;
    }

    public EnvConfig() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "EnvConfig{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
