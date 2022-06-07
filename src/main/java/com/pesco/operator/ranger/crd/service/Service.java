package com.pesco.operator.ranger.crd.service;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Map;

/**
 * @Title 初始化的service服务
 * @Company 尚源智慧科技有限公司
 * @Author 陈翔
 * @Package com.pesco.operator.ranger.crd.service
 * @Description
 * @Date 2022/6/7 5:20 下午
 * @Version V1.0
 */
@RegisterForReflection
public class Service {

    private String name;

    private String displayName;

    private String description;

    private Boolean isEnabled = true;

    private String type;

    private Map<String, String> configs;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getEnabled() {
        return isEnabled;
    }

    public void setEnabled(Boolean enabled) {
        isEnabled = enabled;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getConfigs() {
        return configs;
    }

    public void setConfigs(Map<String, String> configs) {
        this.configs = configs;
    }

    @Override
    public String toString() {
        return "Service{" +
                "name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", description='" + description + '\'' +
                ", isEnabled=" + isEnabled +
                ", type='" + type + '\'' +
                ", configs=" + configs +
                '}';
    }
}
