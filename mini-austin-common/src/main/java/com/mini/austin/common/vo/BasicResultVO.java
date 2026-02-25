package com.mini.austin.common.vo;

import com.mini.austin.common.enums.RespStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用响应对象
 *
 * @author mini-austin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasicResultVO<T> {

    /**
     * 状态码
     */
    private String status;

    /**
     * 响应信息
     */
    private String msg;

    /**
     * 响应数据
     */
    private T data;

    public BasicResultVO(RespStatusEnum respStatusEnum) {
        this(respStatusEnum, null);
    }

    public BasicResultVO(RespStatusEnum respStatusEnum, T data) {
        this.status = respStatusEnum.getCode();
        this.msg = respStatusEnum.getMsg();
        this.data = data;
    }

    /**
     * 成功
     */
    public static <T> BasicResultVO<T> success() {
        return new BasicResultVO<>(RespStatusEnum.SUCCESS);
    }

    public static <T> BasicResultVO<T> success(T data) {
        return new BasicResultVO<>(RespStatusEnum.SUCCESS, data);
    }

    /**
     * 失败
     */
    public static <T> BasicResultVO<T> fail() {
        return new BasicResultVO<>(RespStatusEnum.FAIL);
    }

    public static <T> BasicResultVO<T> fail(String msg) {
        return BasicResultVO.<T>builder()
                .status(RespStatusEnum.FAIL.getCode())
                .msg(msg)
                .build();
    }

    public static <T> BasicResultVO<T> fail(RespStatusEnum respStatusEnum) {
        return new BasicResultVO<>(respStatusEnum);
    }
}
