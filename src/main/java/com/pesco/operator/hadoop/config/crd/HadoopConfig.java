package com.pesco.operator.hadoop.config.crd;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.*;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * @Title hadoop配置对象，统一抽象为资源形式，以方便其他服务使用
 * @Company 尚源智慧科技有限公司
 * @Author 陈翔
 * @Package com.pesco.operator.hadoop.config.crd
 * @Description
 * @Date 2022/6/7 8:29 上午
 * @Version V1.0
 */

@Group("com.pesco.operator")
@Kind("HadoopConfig")
@Plural("hadoopconfigs")
@Singular("hadoopconfig")
@ShortNames("hdc")
@Version("v1")
@RegisterForReflection
public class HadoopConfig extends CustomResource<HadoopConfigSpec, HadoopConfigStatus> implements Namespaced {
}
