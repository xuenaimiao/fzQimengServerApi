package org.xuenai.qimengapi.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 批量查询结果项
 * @author XueNai
 */
@Data
public class BatchQueryResult {
    private String account;    // QQ账号
    private String status;     // 状态: 安全/云黑/避雷
    private String message;    // 消息（用于错误情况）
} 