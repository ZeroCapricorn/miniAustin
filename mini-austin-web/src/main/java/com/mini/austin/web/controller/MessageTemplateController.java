package com.mini.austin.web.controller;

import com.mini.austin.common.vo.BasicResultVO;
import com.mini.austin.web.domain.MessageTemplate;
import com.mini.austin.web.service.MessageTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息模板 Controller
 *
 * @author mini-austin
 */
@RestController
@RequestMapping("/messageTemplate")
public class MessageTemplateController {

    @Autowired
    private MessageTemplateService messageTemplateService;

    /**
     * 查询所有模板
     */
    @GetMapping("/list")
    public BasicResultVO<List<MessageTemplate>> list() {
        return BasicResultVO.success(messageTemplateService.list());
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/{id}")
    public BasicResultVO<MessageTemplate> getById(@PathVariable Long id) {
        return BasicResultVO.success(messageTemplateService.getById(id));
    }

    /**
     * 保存或更新
     */
    @PostMapping("/save")
    public BasicResultVO<MessageTemplate> save(@RequestBody MessageTemplate template) {
        return BasicResultVO.success(messageTemplateService.saveOrUpdate(template));
    }

    /**
     * 删除（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public BasicResultVO<Void> delete(@PathVariable Long id) {
        messageTemplateService.deleteById(id);
        return BasicResultVO.success();
    }
}
