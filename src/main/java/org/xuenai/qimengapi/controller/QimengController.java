package org.xuenai.qimengapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xuenai.qimengapi.model.AccountInfo;
import org.xuenai.qimengapi.model.BatchQueryResult;
import org.xuenai.qimengapi.service.QimengService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * qimeng兽圈防骗查询API控制器
 * @author XueNai
 */
@RestController
@RequestMapping("/api/qimeng")
public class QimengController {

    @Autowired
    private QimengService qimengService;

    /**
     * 单个QQ查询接口
     * @param account QQ号码
     * @return 查询结果
     */
    @PostMapping("/query")
    public Map<String, Object> querySingle(@RequestParam String account) {
        Map<String, Object> result = new HashMap<>();
        try {
            AccountInfo queryResult = qimengService.querySingle(account);
            result.put("code", 200);
            result.put("message", "查询成功");
            result.put("data", queryResult);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "查询失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 批量QQ查询接口
     * @param accounts 多个QQ号码，以逗号分隔
     * @return 批量查询结果
     */
    @PostMapping("/batchQuery")
    public Map<String, Object> queryBatch(@RequestParam String accounts) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 直接将逗号分隔的账号传递给service，service层会处理
            List<BatchQueryResult> queryResult = qimengService.queryBatch(accounts);
            result.put("code", 200);
            result.put("message", "批量查询成功");
            result.put("data", queryResult);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "批量查询失败: " + e.getMessage());
        }
        return result;
    }
}