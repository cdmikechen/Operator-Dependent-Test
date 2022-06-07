package com.pesco.operator.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.fabric8.kubernetes.client.utils.Serialization;

/**
 * @Title yaml处理工具类
 * @Company 尚源智慧科技有限公司
 * @Author 陈翔
 * @Package com.pesco.operator.common.util
 * @Description
 * @Date 2022/4/29 10:54 上午
 * @Version V1.0
 */
public class YamlUtil {

    public static String toPrettyYaml(Object pojoObject) {
        try {
            return YAML_MAPPER.writeValueAsString(pojoObject);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("解析对象的yaml失败！" + pojoObject.getClass().getName(), ex);
        }
    }

    private static final YAMLMapper YAML_MAPPER = YAMLMapper.builder(new YAMLFactory()
                    .disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID))
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
            .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
            .build();

    static {
        YAML_MAPPER.registerModules(Serialization.UNMATCHED_FIELD_TYPE_MODULE);
    }

}
