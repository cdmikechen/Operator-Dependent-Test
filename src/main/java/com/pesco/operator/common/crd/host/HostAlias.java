package com.pesco.operator.common.crd.host;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

/**
 * @Title hostAliases资源对象
 * @Company 尚源智慧科技有限公司
 * @Author 陈翔
 * @Package com.pesco.operator.common.crd.host
 * @Description
 * @Date 2022/6/7 4:53 下午
 * @Version V1.0
 */

@RegisterForReflection
public class HostAlias {

    private String ip;

    private List<String> hostnames;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public List<String> getHostnames() {
        return hostnames;
    }

    public void setHostnames(List<String> hostnames) {
        this.hostnames = hostnames;
    }

    @Override
    public String toString() {
        return "HostAlias{" +
                "ip='" + ip + '\'' +
                ", hostnames=" + hostnames +
                '}';
    }
}
