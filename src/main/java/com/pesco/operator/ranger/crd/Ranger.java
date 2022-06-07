package com.pesco.operator.ranger.crd;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.*;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * @Title Ranger部署服务
 * @Company 尚源智慧科技有限公司
 * @Author 陈翔
 * @Package com.pesco.operator.ranger.crd
 * @Description
 * @Date 2022/6/7 4:32 下午
 * @Version V1.0
 */

@Group("com.pesco.operator")
@Kind("Ranger")
@Plural("rangers")
@Singular("ranger")
@ShortNames("ranger")
@Version("v1")
@RegisterForReflection
public class Ranger extends CustomResource<RangerSpec, RangerStatus> implements Namespaced {

}
