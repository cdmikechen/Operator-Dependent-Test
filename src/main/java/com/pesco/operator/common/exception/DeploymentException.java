package com.pesco.operator.common.exception;

/**
 * @Title deployment异常
 * @Company 尚源智慧科技有限公司
 * @Author 陈翔
 * @Package com.pesco.operator.common.exception
 * @Description
 * @Date 2022/6/9 11:12 上午
 * @Version V1.0
 */
public class DeploymentException extends RuntimeException {

    public DeploymentException(String message) {
        super(message);
    }

    public DeploymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
