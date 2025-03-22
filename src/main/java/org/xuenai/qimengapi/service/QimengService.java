package org.xuenai.qimengapi.service;

import org.springframework.stereotype.Service;
import org.xuenai.qimengapi.model.AccountInfo;
import org.xuenai.qimengapi.model.BatchQueryResult;
import org.xuenai.qimengapi.utils.QimengQueryUtil;

import java.util.List;

/**
 * qimeng兽圈防骗查询服务类
 * @author XueNai
 */
@Service
public class QimengService {

    /**
     * 单个QQ号查询
     * @param account QQ号码
     * @return 账号信息
     */
    public AccountInfo querySingle(String account) {
        return QimengQueryUtil.query(account);
    }
    
    /**
     * 批量QQ号查询
     * @param accounts 多个QQ号码，以换行符分隔
     * @return 批量查询结果
     */
    public List<BatchQueryResult> queryBatch(String accounts) {
        return QimengQueryUtil.batchQuery(accounts);
    }
} 