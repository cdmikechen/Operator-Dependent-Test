package com.pesco.operator.common;

/**
 * @Title k8s的部分常量
 * @Company 尚源智慧科技有限公司
 * @Author 陈翔
 * @Package com.pesco.operator.common
 * @Description
 * @Date 2022/4/29 11:34 上午
 * @Version V1.0
 */
public interface KubeConstants {

    /* 标签相关，参考自 https://kubernetes.io/docs/concepts/overview/working-with-objects/common-labels/ */
    String APP_LABEL = "app.kubernetes.io/name";
    String INSTANCE_LABEL = "app.kubernetes.io/instance";
    String VERSION_LABEL = "app.kubernetes.io/version";
    String COMPONMENT_LABEL = "app.kubernetes.io/component";
    String PART_OF_LABEL = "app.kubernetes.io/app.kubernetes.io/part-of";
    String MANAGED_BY_LABEL = "app.kubernetes.io/managed-by";
    String CREATED_BY_LABEL = "app.kubernetes.io/created-by";

    String LABELS_CONTEXT_KEY = "labels";
    String CONFIG_MAP_HASH_ANNOTATION = "quarkus-config-hash";

    String OPERATOR_NAME = "bigdata-deployment-operator";
    String QUARKUS_INSTANCE_NAME = "quarkus";

    String DEFAULT_APPLICATION_VERSION = "1.0.0";

    /* 资源名称信息 */
    String HADOOP_CONFIG_INSTANCE_NAME = "hadoop-config";

    String QUARKUS_APPLICATION_PROPERTIES = "application.properties";
    String QUARKUS_DEFAULT_CLUSTER_ROLE = "quarkus-configmap-role";

    String ClusterIP = "ClusterIP";
}
