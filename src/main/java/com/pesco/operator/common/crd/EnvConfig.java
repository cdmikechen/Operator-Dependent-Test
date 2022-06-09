package com.pesco.operator.common.crd;

import io.quarkus.runtime.annotations.RegisterForReflection;

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
