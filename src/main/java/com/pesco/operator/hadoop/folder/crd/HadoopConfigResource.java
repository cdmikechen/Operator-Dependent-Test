package com.pesco.operator.hadoop.folder.crd;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * @Title HadoopConfig配置资源
 * @Company 尚源智慧科技有限公司
 * @Author 陈翔
 * @Package com.pesco.operator.hadoop.folder.crd
 * @Description
 * @Date 2022/6/7 3:18 下午
 * @Version V1.0
 */
@RegisterForReflection
public class HadoopConfigResource {

    private String namespace;

    private String name;

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

    @Override
    public String toString() {
        return "HadoopConfigResource{" +
                "namespace='" + namespace + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
