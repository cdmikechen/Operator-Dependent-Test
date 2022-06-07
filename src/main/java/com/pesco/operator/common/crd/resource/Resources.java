package com.pesco.operator.common.crd.resource;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * @Title Resources资源相关对象
 * @Company 尚源智慧科技有限公司
 * @Author 陈翔
 * @Package com.pesco.operator.common.crd
 * @Description
 * @Date 2022/6/7 4:48 下午
 * @Version V1.0
 */
@RegisterForReflection
public class Resources {

    private Resource requests;

    private Resource limits;

    public Resource getRequests() {
        return requests;
    }

    public void setRequests(Resource requests) {
        this.requests = requests;
    }

    public Resource getLimits() {
        return limits;
    }

    public void setLimits(Resource limits) {
        this.limits = limits;
    }

    @Override
    public String toString() {
        return "Resources{" +
                "requests=" + requests +
                ", limits=" + limits +
                '}';
    }
}
