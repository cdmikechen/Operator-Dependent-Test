package com.pesco.operator.common.exception;

/**
 * @Title serviceaccount异常
 * @Company 尚源智慧科技有限公司
 * @Author 陈翔
 * @Package com.pesco.operator.common.exception
 * @Description
 * @Date 2022/5/7 11:37 上午
 * @Version V1.0
 */
public class ServiceAccountException extends RuntimeException {

    public ServiceAccountException(String message) {
        super(message);
    }
}
