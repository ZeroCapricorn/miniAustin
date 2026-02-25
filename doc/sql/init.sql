-- ====================================
-- Mini Austin 数据库初始化脚本
-- ====================================

CREATE DATABASE IF NOT EXISTS mini_austin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE mini_austin;

-- 消息模板表
CREATE TABLE IF NOT EXISTS message_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    name VARCHAR(100) NOT NULL COMMENT '模板名称',
    send_channel INT NOT NULL COMMENT '发送渠道（30-短信, 40-邮件）',
    msg_type INT NOT NULL COMMENT '消息类型（10-通知, 20-营销, 30-验证码）',
    msg_content TEXT NOT NULL COMMENT '消息内容JSON',
    send_account INT DEFAULT NULL COMMENT '发送账号ID',
    status INT NOT NULL DEFAULT 0 COMMENT '状态（0-草稿, 1-启用, 2-停用）',
    creator VARCHAR(64) DEFAULT NULL COMMENT '创建者',
    created_time BIGINT DEFAULT NULL COMMENT '创建时间（秒级时间戳）',
    updated_time BIGINT DEFAULT NULL COMMENT '更新时间（秒级时间戳）',
    is_deleted INT NOT NULL DEFAULT 0 COMMENT '是否删除（0-否, 1-是）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息模板表';

-- 插入测试数据
INSERT INTO message_template (name, send_channel, msg_type, msg_content, status, creator, created_time, updated_time, is_deleted)
VALUES
('验证码短信模板', 30, 30, '{"content":"您的验证码是${code}，5分钟内有效，请勿泄露给他人。"}', 1, 'admin', UNIX_TIMESTAMP(), UNIX_TIMESTAMP(), 0),
('系统通知邮件模板', 40, 10, '{"title":"系统通知","content":"尊敬的${name}，您有一条新的系统通知：${message}"}', 1, 'admin', UNIX_TIMESTAMP(), UNIX_TIMESTAMP(), 0),
('营销活动邮件模板', 40, 20, '{"title":"${title}","content":"亲爱的${name}，${content}。点击链接了解详情：${url}"}', 1, 'admin', UNIX_TIMESTAMP(), UNIX_TIMESTAMP(), 0);
