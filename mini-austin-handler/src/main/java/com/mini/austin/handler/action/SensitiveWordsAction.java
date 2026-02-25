package com.mini.austin.handler.action;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mini.austin.common.domain.ContentModel;
import com.mini.austin.common.domain.TaskInfo;
import com.mini.austin.common.dto.model.EmailContentModel;
import com.mini.austin.common.dto.model.SmsContentModel;
import com.mini.austin.common.pipeline.BusinessProcess;
import com.mini.austin.common.pipeline.ProcessContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 敏感词过滤 Action
 * <p>
 * ★★★ Phase 4 功能：过滤消息内容中的敏感词 ★★★
 * <p>
 * 场景：
 * - 防止发送包含违规内容的消息（政治敏感、色情、诈骗等）
 * - 保护平台合规性，避免法律风险
 * <p>
 * 实现原理：
 * - 使用 DFA（确定有限自动机）算法构建敏感词匹配树
 * - 时间复杂度 O(n)，n 为文本长度，高效处理大量敏感词
 * <p>
 * 面试亮点：
 * - DFA 算法原理与优势（对比暴力匹配）
 * - 敏感词的加载与热更新设计
 *
 * @author mini-austin
 */
@Slf4j
@Component
public class SensitiveWordsAction implements BusinessProcess<TaskInfo> {

    /**
     * 是否启用敏感词过滤
     */
    @Value("${mini-austin.sensitive-words.enabled:true}")
    private boolean enabled;

    /**
     * 敏感词替换字符
     */
    @Value("${mini-austin.sensitive-words.replacement:*}")
    private String replacement;

    /**
     * DFA 敏感词树根节点
     */
    private Map<Character, Object> sensitiveWordMap = new HashMap<>();

    /**
     * 敏感词标记位（表示一个敏感词结束）
     */
    private static final String IS_END = "isEnd";

    /**
     * 初始化敏感词库
     */
    @PostConstruct
    public void init() {
        loadSensitiveWords();
        log.info("敏感词库初始化完成，共加载 {} 个敏感词", countSensitiveWords());
    }

    @Override
    public void process(ProcessContext<TaskInfo> context) {
        if (!enabled) {
            return;
        }

        TaskInfo taskInfo = context.getProcessModel();
        ContentModel contentModel = taskInfo.getContentModel();

        if (contentModel == null) {
            return;
        }

        // 根据不同渠道过滤内容
        if (contentModel instanceof SmsContentModel) {
            SmsContentModel sms = (SmsContentModel) contentModel;
            String filtered = filterSensitiveWords(sms.getContent());
            sms.setContent(filtered);
        } else if (contentModel instanceof EmailContentModel) {
            EmailContentModel email = (EmailContentModel) contentModel;
            String filteredTitle = filterSensitiveWords(email.getTitle());
            String filteredContent = filterSensitiveWords(email.getContent());
            email.setTitle(filteredTitle);
            email.setContent(filteredContent);
        }
    }

    /**
     * 加载敏感词库（从 classpath 下的文件读取）
     */
    private void loadSensitiveWords() {
        Set<String> words = new HashSet<>();

        // 默认敏感词（实际项目中应从文件或数据库加载）
        words.addAll(Arrays.asList(
                "违禁词", "敏感内容", "诈骗", "赌博", "色情",
                "传销", "非法集资", "洗钱", "枪支", "毒品"
        ));

        // 尝试从配置文件加载
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("sensitive-words.txt")) {
            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (StrUtil.isNotBlank(line) && !line.startsWith("#")) {
                        words.add(line);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("加载敏感词文件失败，使用默认敏感词库: {}", e.getMessage());
        }

        // 构建 DFA 树
        buildDFATree(words);
    }

    /**
     * 构建 DFA 敏感词树
     * <p>
     * 例如：敏感词 "赌博"、"赌场"
     * 构建的树结构：
     * 根 -> 赌 -> 博(end)
     *         -> 场(end)
     */
    @SuppressWarnings("unchecked")
    private void buildDFATree(Set<String> words) {
        for (String word : words) {
            if (StrUtil.isBlank(word)) {
                continue;
            }

            Map<Character, Object> currentMap = sensitiveWordMap;

            for (int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                Object obj = currentMap.get(c);

                if (obj == null) {
                    // 新建节点
                    Map<Character, Object> newMap = new HashMap<>();
                    newMap.put(IS_END.charAt(0), false);
                    currentMap.put(c, newMap);
                    currentMap = newMap;
                } else {
                    currentMap = (Map<Character, Object>) obj;
                }

                // 最后一个字符标记为结束
                if (i == word.length() - 1) {
                    currentMap.put(IS_END.charAt(0), true);
                }
            }
        }
    }

    /**
     * 过滤敏感词
     *
     * @param text 原始文本
     * @return 过滤后的文本（敏感词被替换为 *）
     */
    @SuppressWarnings("unchecked")
    public String filterSensitiveWords(String text) {
        if (StrUtil.isBlank(text) || CollUtil.isEmpty(sensitiveWordMap)) {
            return text;
        }

        StringBuilder result = new StringBuilder();
        int length = text.length();
        int i = 0;

        while (i < length) {
            char c = text.charAt(i);
            Map<Character, Object> currentMap = sensitiveWordMap;

            // 如果当前字符不在敏感词树中，直接加入结果
            if (!currentMap.containsKey(c)) {
                result.append(c);
                i++;
                continue;
            }

            // 尝试匹配敏感词
            int matchLength = 0;
            int j = i;
            boolean found = false;

            while (j < length) {
                char ch = text.charAt(j);
                Object obj = currentMap.get(ch);

                if (obj == null) {
                    break;
                }

                currentMap = (Map<Character, Object>) obj;
                matchLength++;

                // 检查是否匹配到完整敏感词
                Object isEnd = currentMap.get(IS_END.charAt(0));
                if (Boolean.TRUE.equals(isEnd)) {
                    found = true;
                    // 继续尝试更长的匹配（贪婪模式）
                }

                j++;
            }

            if (found && matchLength > 0) {
                // 用替换字符替换敏感词
                for (int k = 0; k < matchLength; k++) {
                    result.append(replacement);
                }
                i += matchLength;
                log.debug("敏感词被过滤: {}", text.substring(i - matchLength, i));
            } else {
                result.append(c);
                i++;
            }
        }

        return result.toString();
    }

    /**
     * 统计敏感词数量（用于日志）
     */
    private int countSensitiveWords() {
        return countWords(sensitiveWordMap);
    }

    @SuppressWarnings("unchecked")
    private int countWords(Map<Character, Object> map) {
        int count = 0;
        for (Map.Entry<Character, Object> entry : map.entrySet()) {
            if (entry.getKey().equals(IS_END.charAt(0))) {
                if (Boolean.TRUE.equals(entry.getValue())) {
                    count++;
                }
            } else if (entry.getValue() instanceof Map) {
                count += countWords((Map<Character, Object>) entry.getValue());
            }
        }
        return count;
    }
}
