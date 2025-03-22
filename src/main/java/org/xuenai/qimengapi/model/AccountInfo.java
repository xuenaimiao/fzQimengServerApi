package org.xuenai.qimengapi.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 账号信息数据类
 * @author XueNai
 */
@Data
public class AccountInfo {
    private String account;            // 查询账号
    private String status;             // 账号状态: 安全/云黑/避雷
    private String warning;            // 警告信息
    private String teacher;            // 登记老师
    private String level;              // 云黑等级/避雷等级
    private String blackTime;          // 上黑时间/避雷时间
    private String reason;             // 云黑原因/避雷原因
    private String trustLevel;         // 可信度
    private String registerTime;       // 注册时间
    private String lastLoginTime;      // 上次登录时间
    private String rating;             // 评级
    private String phoneAuth;          // 手机认证
    private String realNameAuth;       // 实名认证
    private String wechatAuth;         // 微信认证
    private String alipayAuth;         // 支付宝认证
    private String groupCount;         // 所在群数
    private String monthlyActivity;    // 本月活跃
    private String totalActivity;      // 总计活跃
    private String firstActive;        // 首次活跃
    private String lastActive;         // 最后活跃
    private String templateCount;      // 打标模板数量
    private String score;              // 账号包裹积分
    private String otherTags;          // 其他标签
} 