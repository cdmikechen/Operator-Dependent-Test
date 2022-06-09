package com.pesco.operator.common.crd.rds;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pesco.operator.common.type.BackendType;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Title 数据库底层的存储
 * @Company 尚源智慧科技有限公司
 * @Author 陈翔
 * @Package com.pesco.operator.common.crd.rds
 * @Description
 * @Date 2022/6/9 1:03 下午
 * @Version V1.0
 */

@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true)
public class DbBackend {

    private BackendType type;

    private String namespace;

    private String name;

    private Map<String, String> properties = new LinkedHashMap<>();

    public BackendType getType() {
        return type;
    }

    public void setType(BackendType type) {
        this.type = type;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return "DbBackend{" +
                "type=" + type +
                ", namespace='" + namespace + '\'' +
                ", name='" + name + '\'' +
                ", properties=" + properties +
                '}';
    }
}
