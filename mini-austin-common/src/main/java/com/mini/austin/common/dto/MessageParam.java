package com.mini.austin.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 发送请求参数
 *
 * @author mini-austin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageParam {

    /**
     * 接收者（多个用逗号隔开）
     * 例如：手机号、邮箱、userId
     */
    private String receiver;

    /**
     * 占位符变量
     * 例如：{"name": "张三", "code": "123456"}
     */
    private Map<String, String> variables;

    /**
     * 扩展参数（预留）
     */
    private Map<String, String> extra;
}
