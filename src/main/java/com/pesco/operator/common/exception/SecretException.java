package com.pesco.operator.common.exception;

/**
 * @Title secret异常
 * @Company 尚源智慧科技有限公司
 * @Author 陈翔
 * @Package com.pesco.operator.common.exception
 * @Description
 * @Date 2022/5/7 4:04 下午
 * @Version V1.0
 */
public class SecretException extends RuntimeException {

    public SecretException(String message) {
        super(message);
    }

    public SecretException(String message, Throwable cause) {
        super(message, cause);
    }
}
