package com.pesco.operator.ranger.crd.service;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

/**
 * @Title 初始化的service信息
 * @Company 尚源智慧科技有限公司
 * @Author 陈翔
 * @Package com.pesco.operator.ranger.crd.service
 * @Description
 * @Date 2022/6/7 5:22 下午
 * @Version V1.0
 */
@RegisterForReflection
public class InitService {

    private Boolean enable = false;

    List<Service> service;

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public List<Service> getService() {
        return service;
    }

    public void setService(List<Service> service) {
        this.service = service;
    }

    @Override
    public String toString() {
        return "InitService{" +
                "enable=" + enable +
                ", service=" + service +
                '}';
    }
}
