package com.mini.austin.web.dao;

import com.mini.austin.web.domain.MessageTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 消息模板 DAO
 *
 * @author mini-austin
 */
@Repository
public interface MessageTemplateDao extends JpaRepository<MessageTemplate, Long> {

    /**
     * 根据删除状态查询
     */
    List<MessageTemplate> findByIsDeleted(Integer isDeleted);

    /**
     * 统计未删除的模板数量
     */
    long countByIsDeleted(Integer isDeleted);
}
