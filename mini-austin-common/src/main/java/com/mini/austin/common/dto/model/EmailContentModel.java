package com.mini.austin.common.dto.model;

import com.mini.austin.common.domain.ContentModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 邮件内容模型
 *
 * @author mini-austin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailContentModel implements ContentModel {

    /**
     * 邮件标题
     */
    private String title;

    /**
     * 邮件正文（支持HTML）
     */
    private String content;

    /**
     * 附件链接（可选）
     */
    private String url;
}
