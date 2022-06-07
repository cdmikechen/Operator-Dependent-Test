package com.pesco.operator.hadoop.config.crd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pesco.operator.common.type.ConfigType;
import com.pesco.operator.common.crd.EnvConfig;
import com.pesco.operator.common.type.ResourceType;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

/**
 * @Title HadoopConfig的详情信息
 * @Company 尚源智慧科技有限公司
 * @Author 陈翔
 * @Package com.pesco.operator.hadoop.config
 * @Description
 * @Date 2022/6/7 8:46 上午
 * @Version V1.0
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@RegisterForReflection
public class HadoopConfigSpec {

    /**
     * 配置文件的类型
     */
    private ConfigType type;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 配置内容
     */
    private String content;

    /**
     * 可覆盖的配置信息
     */
    private List<EnvConfig> overwrites;

    public ConfigType getType() {
        return type;
    }

    public void setType(ConfigType type) {
        this.type = type;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<EnvConfig> getOverwrites() {
        return overwrites;
    }

    public void setOverwrites(List<EnvConfig> overwrites) {
        this.overwrites = overwrites;
    }

    @Override
    public String toString() {
        return "HadoopConfigSpec{" +
                "type=" + type +
                ", fileName='" + fileName + '\'' +
                ", content='" + content + '\'' +
                ", overwrites=" + overwrites +
                '}';
    }
}
