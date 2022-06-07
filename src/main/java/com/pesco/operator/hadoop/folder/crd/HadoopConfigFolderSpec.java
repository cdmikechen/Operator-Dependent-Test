package com.pesco.operator.hadoop.folder.crd;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

/**
 * @Title HadoopConfigFolder描述形式
 * @Company 尚源智慧科技有限公司
 * @Author 陈翔
 * @Package com.pesco.operator.hadoop.folder
 * @Description
 * @Date 2022/6/7 9:20 上午
 * @Version V1.0
 */
@RegisterForReflection
public class HadoopConfigFolderSpec {

    private List<HadoopConfigResource> config;

    public List<HadoopConfigResource> getConfig() {
        return config;
    }

    public void setConfig(List<HadoopConfigResource> config) {
        this.config = config;
    }
}
