package com.pesco.operator.common;

public interface KubeConstants {

    /* 标签相关，参考自 https://kubernetes.io/docs/concepts/overview/working-with-objects/common-labels/ */
    String APP_LABEL = "app.kubernetes.io/name";
    String INSTANCE_LABEL = "app.kubernetes.io/instance";
    String VERSION_LABEL = "app.kubernetes.io/version";
    String COMPONENT_LABEL = "app.kubernetes.io/component";

    String LABELS_CONTEXT_KEY = "labels";

    String OPERATOR_NAME = "bigdata-deployment-operator";

    String DEFAULT_APPLICATION_VERSION = "1.0.0";

    /* 资源名称信息 */
    String HADOOP_CONFIG_INSTANCE_NAME = "hadoop-config";

}
