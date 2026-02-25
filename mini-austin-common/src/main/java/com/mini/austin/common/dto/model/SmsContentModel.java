package com.mini.austin.common.dto.model;

import com.mini.austin.common.domain.ContentModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短信内容模型
 *
 * @author mini-austin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsContentModel implements ContentModel {

    /**
     * 短信内容
     */
    private String content;

    /**
     * 短链接（可选，拼接在内容后）
     */
    private String url;
}
