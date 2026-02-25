package com.mini.austin.common.domain;

import java.io.Serializable;

/**
 * 消息内容模型 - 标记接口
 * <p>
 * 不同渠道（邮件/短信/Push）的内容结构不同，
 * 各渠道实现自己的 ContentModel
 *
 * @author mini-austin
 */
public interface ContentModel extends Serializable {
}
