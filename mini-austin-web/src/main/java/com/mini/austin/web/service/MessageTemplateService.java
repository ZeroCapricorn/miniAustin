package com.mini.austin.web.service;

import com.mini.austin.web.domain.MessageTemplate;

import java.util.List;

/**
 * 消息模板服务接口
 *
 * @author mini-austin
 */
public interface MessageTemplateService {

    /**
     * 查询所有未删除的模板
     */
    List<MessageTemplate> list();

    /**
     * 根据ID查询
     */
    MessageTemplate getById(Long id);

    /**
     * 保存或更新
     */
    MessageTemplate saveOrUpdate(MessageTemplate template);

    /**
     * 逻辑删除
     */
    void deleteById(Long id);
}
