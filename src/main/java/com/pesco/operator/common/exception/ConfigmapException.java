package com.pesco.operator.common.exception;

/**
 * @Title configmap异常
 * @Company 尚源智慧科技有限公司
 * @Author 陈翔
 * @Package com.pesco.operator.common.exception
 * @Description
 * @Date 2022/5/7 4:04 下午
 * @Version V1.0
 */
public class ConfigmapException extends RuntimeException {

    public ConfigmapException(String message) {
        super(message);
    }

    public ConfigmapException(String message, Throwable cause) {
        super(message, cause);
    }
}
