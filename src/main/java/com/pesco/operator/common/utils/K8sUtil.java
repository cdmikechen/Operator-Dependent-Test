package com.pesco.operator.common.utils;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static com.pesco.operator.common.KubeConstants.*;

public class K8sUtil {

    public static ObjectMeta createMetadata(String namespace, String name, Map<String, String> labels) {
        var builder = new ObjectMetaBuilder()
                .withName(name)
                .withLabels(labels);
        if (StringUtils.isNotBlank(namespace)) builder.withNamespace(namespace);
        return builder.build();
    }

    /**
     * 获取 context 传输的标签变量
     */
    public static Map<String, String> getContextLabels(Context context) {
        return (Map<String, String>) context.managedDependentResourceContext()
                .getMandatory(LABELS_CONTEXT_KEY, Map.class);
    }

    /**
     * configmap名称
     */
    public static String hadoopConfigmap(String name) {
        return String.format("%s-configmap", name);
    }

}
