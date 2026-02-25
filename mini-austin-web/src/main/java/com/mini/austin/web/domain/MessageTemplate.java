package com.mini.austin.web.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 消息模板实体
 * <p>
 * 定义一个消息的模板，包括：发送渠道、内容模板、接收者类型等
 *
 * @author mini-austin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "message_template")
public class MessageTemplate implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 模板名称
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 发送渠道
     * @see com.mini.austin.common.enums.ChannelType
     */
    @Column(nullable = false)
    private Integer sendChannel;

    /**
     * 消息类型（通知/营销/验证码）
     * @see com.mini.austin.common.enums.MessageType
     */
    @Column(nullable = false)
    private Integer msgType;

    /**
     * 消息内容（JSON格式，支持占位符 ${xxx}）
     * 例如：{"title":"验证码","content":"您的验证码是${code}，5分钟内有效"}
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String msgContent;

    /**
     * 发送账号ID（用于多账号场景）
     */
    private Integer sendAccount;

    /**
     * 模板状态（0-草稿，1-启用，2-停用）
     */
    @Column(nullable = false)
    private Integer status;

    /**
     * 创建者
     */
    @Column(length = 64)
    private String creator;

    /**
     * 创建时间（秒级时间戳）
     */
    private Long createdTime;

    /**
     * 更新时间（秒级时间戳）
     */
    private Long updatedTime;

    /**
     * 是否删除（0-否，1-是）
     */
    @Column(nullable = false)
    private Integer isDeleted;
}
