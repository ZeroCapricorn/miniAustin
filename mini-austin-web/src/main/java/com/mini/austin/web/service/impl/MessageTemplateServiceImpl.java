package com.mini.austin.web.service.impl;

import com.mini.austin.web.dao.MessageTemplateDao;
import com.mini.austin.web.domain.MessageTemplate;
import com.mini.austin.web.service.MessageTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 消息模板服务实现
 *
 * @author mini-austin
 */
@Service
public class MessageTemplateServiceImpl implements MessageTemplateService {

    private static final Integer NOT_DELETED = 0;
    private static final Integer DELETED = 1;

    @Autowired
    private MessageTemplateDao messageTemplateDao;

    @Override
    public List<MessageTemplate> list() {
        return messageTemplateDao.findByIsDeleted(NOT_DELETED);
    }

    @Override
    public MessageTemplate getById(Long id) {
        return messageTemplateDao.findById(id).orElse(null);
    }

    @Override
    public MessageTemplate saveOrUpdate(MessageTemplate template) {
        long now = System.currentTimeMillis() / 1000;

        if (Objects.isNull(template.getId())) {
            // 新增
            template.setStatus(0);
            template.setIsDeleted(NOT_DELETED);
            template.setCreatedTime(now);
        }
        template.setUpdatedTime(now);

        return messageTemplateDao.save(template);
    }

    @Override
    public void deleteById(Long id) {
        MessageTemplate template = messageTemplateDao.findById(id).orElse(null);
        if (template != null) {
            template.setIsDeleted(DELETED);
            template.setUpdatedTime(System.currentTimeMillis() / 1000);
            messageTemplateDao.save(template);
        }
    }
}
