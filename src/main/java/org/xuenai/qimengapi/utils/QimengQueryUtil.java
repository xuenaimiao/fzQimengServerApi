package org.xuenai.qimengapi.utils;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.htmlunit.BrowserVersion;
import org.htmlunit.WebClient;
import org.htmlunit.WebRequest;
import org.htmlunit.html.HtmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xuenai.qimengapi.model.AccountInfo;
import org.xuenai.qimengapi.model.BatchQueryResult;

/**
 * qimeng兽圈防骗查询工具类
 * @author XueNai
 */
@Slf4j
public class QimengQueryUtil {

    private static final String QIMENG_URL = "https://fz.qimeng.fun/";

    /**
     * 获取WebClient实例
     */
    private static WebClient getWebClient() {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setTimeout(60 * 1000);
        return webClient;
    }

    /**
     * 通用请求方法，兼容GET和POST请求
     */
    public static String executeRequest(String url, Map<String, String> headers, String method, String postData, Integer timeout) {
        WebClient webClient = getWebClient();
        HtmlPage htmlPage = null;

        try {
            WebRequest request = new WebRequest(new URL(url), "POST".equalsIgnoreCase(method) ? org.htmlunit.HttpMethod.POST : org.htmlunit.HttpMethod.GET);

            // 设置请求头
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    request.setAdditionalHeader(entry.getKey(), entry.getValue());
                }
            }

            // 如果是POST请求，设置请求体
            if ("POST".equalsIgnoreCase(method) && postData != null) {
                request.setRequestBody(postData);
            }

            // 发送请求并等待响应
            try {
                htmlPage = webClient.getPage(request);;
            } catch (Exception e) {
                // JS错误不影响结果
//                log.error("执行请求时出错: ", e);
            }

            webClient.waitForBackgroundJavaScript(timeout * 1000);

        } catch (Exception e) {
            log.error("执行请求时出错: ", e);
            return "";
        }

        // 返回响应内容
        return htmlPage != null ? htmlPage.asXml() : "";
    }

    /**
     * 单个QQ查询方法
     * @param account QQ号码
     * @return 查询结果对象
     */
    public static AccountInfo query(String account) {
        Map<String, String> headers = getRequestHeaders();
        String postData = "cxtype=DanYi&user=" + account;
        String response = executeRequest(QIMENG_URL, headers, "POST", postData, 30);

        // 解析查询结果
        return parseHtml(response);
    }

    /**
     * 批量QQ查询方法
     * @param accounts 多个QQ号码，以换行符分隔
     * @return 批量查询结果列表
     */
    public static List<BatchQueryResult> batchQuery(String accounts) {
        Map<String, String> headers = getRequestHeaders();
        
        // 将多行账号转换为批量查询格式，使用|作为分隔符
        String formattedAccounts = accounts.replace("\n", "|").replace(",", "|");
        String postData = "cxtype=PiLiang&user=" + formattedAccounts;

        // 发送POST请求
        String response = executeRequest(QIMENG_URL, headers, "POST", postData, 30);

        // 解析批量查询结果
        return parseBatchResponse(response);
    }

    /**
     * 获取请求头
     */
    private static Map<String, String> getRequestHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        headers.put("content-type", "application/x-www-form-urlencoded");
        headers.put("origin", QIMENG_URL);
        headers.put("referer", QIMENG_URL);
        headers.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");
        return headers;
    }

    /**
     * 解析HTML内容为AccountInfo对象
     */
    private static AccountInfo parseHtml(String html) {
        try {
            Document doc = Jsoup.parse(html);
            AccountInfo accountInfo = new AccountInfo();

            // 通过颜色和结构判断账号状态
            Element redHeader = doc.select("h4.h5 font[color=#FF0033], h4.h5 font[color='#FF0033']").first();
            Element warningText = doc.select("p.card-text font[color=red]:contains(警告)").first();
            Element blueWarningText = doc.select("p.card-text font[color=blue]:contains(账号为避雷/前科)").first();
            Element orangeHeader = doc.select("h4.h5 font[color=#FF6600], h4.h5 font[color='#FF6600']").first();

            if (redHeader != null && warningText != null) {
                // 云黑账号
                parseBlacklistedAccount(doc, accountInfo);
            } else if (blueWarningText != null || 
                    (orangeHeader != null && doc.select("p.card-text font[color=blue]").size() > 0 &&
                            doc.select("p.card-text:contains(避雷原因)").size() > 0)) {
                // 避雷账号
                parseCautionAccount(doc, accountInfo);
            } else if (doc.select("p.card-text font[color=green]:contains(账号暂无云黑)").size() > 0) {
                // 安全账号
                parseSafeAccount(doc, accountInfo);
            } else if (doc.select("div:contains(此 账 号 目 前 提 交 为 避雷 / 前科 账 号), div:contains(此账号目前提交为避雷/前科账号)").size() > 0) {
                // 旧版避雷账号
                parseCautionAccount(doc, accountInfo);
            } else {
                // 尝试旧版格式
                if (doc.select("span.BiaoTi:contains(请停止任何交易！)").size() > 0) {
                    parseBlacklistedAccount(doc, accountInfo);
                } else if (doc.select("font:contains(账号暂无云黑)").size() > 0) {
                    parseSafeAccount(doc, accountInfo);
                } else if (doc.select("span.BiaoTi:contains(目 前 提 交 为 避雷 / 前科 账 号)").size() > 0) {
                    parseCautionAccount(doc, accountInfo);
                } else {
                    // 最后尝试通过颜色识别
                    if (doc.select("font[color='#FF0033'], font[color='red']").size() > 0
                            && doc.select("p.card-text:contains(完全不可信)").size() > 0) {
                        parseBlacklistedAccount(doc, accountInfo);
                    } else if (doc.select("font[color='blue'], font[color='#0000FF']").size() > 0
                            && doc.select("p.card-text:contains(避雷原因)").size() > 0) {
                        parseCautionAccount(doc, accountInfo);
                    } else if (doc.select("font[color='#6699FF']").size() > 0) {
                        parseSafeAccount(doc, accountInfo);
                    } else {
                        log.error("未知的页面状态！");
                        accountInfo.setStatus("未知");
                    }
                }
            }

            return accountInfo;
        } catch (Exception e) {
            log.error("解析查询结果时出错", e);
            AccountInfo errorInfo = new AccountInfo();
            errorInfo.setStatus("错误");
            errorInfo.setWarning("解析查询结果时出错：" + e.getMessage());
            return errorInfo;
        }
    }

    /**
     * 解析安全账号
     */
    private static void parseSafeAccount(Document doc, AccountInfo accountInfo) {
        accountInfo.setStatus("账号暂无云黑");

        // 提取查询账号
        Element accountElement = doc.select("p.card-text").first();
        if (accountElement != null) {
            accountInfo.setAccount(accountElement.text().trim());
        }

        // 提取评级
        Element ratingElement = doc.select("p.card-text:contains([◈评级:)").first();
        if (ratingElement != null) {
            accountInfo.setRating(ratingElement.text().trim());
        }

        // 提取其他字段
        extractCommonFields(doc, accountInfo);
    }

    /**
     * 解析云黑账号
     */
    private static void parseBlacklistedAccount(Document doc, AccountInfo accountInfo) {
        accountInfo.setStatus("云黑");

        // 提取查询账号
        Element accountElement = doc.select("p.card-text").first();
        if (accountElement != null) {
            accountInfo.setAccount(accountElement.text().trim());
        }

        // 提取警告信息
        String warning = doc.select("p.card-text font[color=red], p.card-text:has(font[color=red])").text();
        if (!warning.isEmpty()) {
            accountInfo.setWarning(warning.contains("警告") ? "请立即终止交易!" : "请停止任何交易!");
        } else {
            accountInfo.setWarning("请停止任何交易!");
        }

        // 提取登记老师
        Elements teacherElements = doc.select("p.card-text:contains(登记老师) font[color=#FF6600], p.card-text:contains(登记老师:) font[color=#FF6600]");
        if (!teacherElements.isEmpty()) {
            accountInfo.setTeacher(teacherElements.text());
        }

        // 提取云黑等级
        Elements levelElements = doc.select("p.card-text:contains(云黑等级) font[color=#FF6600], p.card-text:contains(云黑等级:) font[color=#FF6600]");
        if (!levelElements.isEmpty()) {
            accountInfo.setLevel(levelElements.text());
        }

        // 提取上黑时间
        Elements timeElements = doc.select("p.card-text:contains(上黑时间) font[color=#FF6600], p.card-text:contains(上黑时间:) font[color=#FF6600]");
        if (!timeElements.isEmpty()) {
            accountInfo.setBlackTime(timeElements.text());
        }

        // 提取云黑原因
        Elements reasonElements = doc.select("p.card-text:contains(云黑原因) font[color=#FF6600], p.card-text:contains(云黑原因:) font[color=#FF6600]");
        if (!reasonElements.isEmpty()) {
            accountInfo.setReason(reasonElements.text());
        }

        // 提取可信度
        Elements trustElements = doc.select("p.card-text:contains(完全不可信), p.card-text font[color=red]:contains(完全不可信)");
        if (!trustElements.isEmpty()) {
            accountInfo.setTrustLevel(trustElements.text());
        }

        // 提取其他字段
        extractCommonFields(doc, accountInfo);
    }

    /**
     * 解析避雷账号
     */
    private static void parseCautionAccount(Document doc, AccountInfo accountInfo) {
        accountInfo.setStatus("避雷");

        // 提取查询账号
        Element accountElement = doc.select("p.card-text").first();
        if (accountElement != null) {
            accountInfo.setAccount(accountElement.text().trim());
        }

        // 提取避雷信息
        String cautionText = doc.select("p.card-text font[color=blue]:contains(账号为避雷)").text();
        if (!cautionText.isEmpty()) {
            accountInfo.setWarning(cautionText);
        } else {
            accountInfo.setWarning("此账号目前提交为避雷/前科账号");
        }

        // 提取登记老师
        Elements teacherElements = doc.select("p.card-text:contains(登记老师) font[color=blue], p.card-text:contains(登记老师:) font[color=blue]");
        if (!teacherElements.isEmpty()) {
            accountInfo.setTeacher(teacherElements.text());
        }

        // 提取避雷时间
        Elements timeElements = doc.select("p.card-text:contains(避雷时间) font[color=blue], p.card-text:contains(避雷时间:) font[color=blue]");
        if (!timeElements.isEmpty()) {
            accountInfo.setBlackTime(timeElements.text());
        }

        // 提取避雷原因
        Elements reasonElements = doc.select("p.card-text:contains(避雷原因) font[color=blue], p.card-text:contains(避雷原因:) font[color=blue]");
        if (!reasonElements.isEmpty()) {
            accountInfo.setReason(reasonElements.text());
        }

        // 提取评级
        Elements ratingElements = doc.select("p.card-text:contains([▼评级:)");
        if (!ratingElements.isEmpty()) {
            accountInfo.setRating(ratingElements.text().trim());
        }

        // 提取其他字段
        extractCommonFields(doc, accountInfo);
    }

    /**
     * 提取账号共有字段
     */
    private static void extractCommonFields(Document doc, AccountInfo accountInfo) {
        // 提取注册时间
        String registerTime = doc.select("h4.h6:contains(注册账号时间) + p.card-text").text();
        if (!registerTime.isEmpty()) {
            accountInfo.setRegisterTime(registerTime);
        }

        // 提取上次登录时间
        String lastLoginTime = doc.select("h4.h6:contains(上次登录时间) + p.card-text").text();
        if (!lastLoginTime.isEmpty()) {
            accountInfo.setLastLoginTime(lastLoginTime);
        }

        // 打标模板数量
        String templateCount = doc.select("h4.h6:contains(打标模板数量) + p.card-text").text();
        if (!templateCount.isEmpty()) {
            accountInfo.setTemplateCount(templateCount);
        }

        // 账号包裹积分
        String score = doc.select("h4.h6:contains(账号包裹积分) + p.card-text").text();
        if (!score.isEmpty()) {
            accountInfo.setScore(score);
        }

        // 提取认证状态
        String phoneAuth = doc.select("h4.h6:contains(手机认证) + p.card-text").text();
        if (!phoneAuth.isEmpty()) {
            accountInfo.setPhoneAuth(phoneAuth);
        }

        String realNameAuth = doc.select("h4.h6:contains(实名认证) + p.card-text").text();
        if (!realNameAuth.isEmpty()) {
            accountInfo.setRealNameAuth(realNameAuth);
        }

        String wechatAuth = doc.select("h4.h6:contains(微信号认证) + p.card-text").text();
        if (!wechatAuth.isEmpty()) {
            accountInfo.setWechatAuth(wechatAuth);
        }

        String alipayAuth = doc.select("h4.h6:contains(支付宝认证) + p.card-text").text();
        if (!alipayAuth.isEmpty()) {
            accountInfo.setAlipayAuth(alipayAuth);
        }

        // 提取活跃度相关信息
        String monthlyActivity = doc.select("h4.h6:contains(本月活跃) + p.card-text").text();
        if (!monthlyActivity.isEmpty()) {
            accountInfo.setMonthlyActivity(monthlyActivity);
        }

        String totalActivity = doc.select("h4.h6:contains(总计活跃) + p.card-text").text();
        if (!totalActivity.isEmpty()) {
            accountInfo.setTotalActivity(totalActivity);
        }

        String firstActive = doc.select("h4.h6:contains(首次活跃时间) + p.card-text").text();
        if (!firstActive.isEmpty()) {
            accountInfo.setFirstActive(firstActive);
        }

        String lastActive = doc.select("h4.h6:contains(最后活跃时间) + p.card-text").text();
        if (!lastActive.isEmpty()) {
            accountInfo.setLastActive(lastActive);
        }

        String groupCount = doc.select("h4.h6:contains(所在群数) + p.card-text").text();
        if (!groupCount.isEmpty()) {
            accountInfo.setGroupCount(groupCount);
        }

        // 提取其他标签
        Elements otherTags = doc.select("h4.h6:contains(其它标签) + p.card-text font");
        if (otherTags.size() > 0) {
            StringBuilder tags = new StringBuilder();
            for (Element tag : otherTags) {
                String tagText = tag.text();
                if (!tagText.isEmpty()) {
                    tags.append(tagText).append(" ");
                }
            }
            if (tags.length() > 0) {
                accountInfo.setOtherTags(tags.toString().trim());
            }
        }
    }

    /**
     * 解析批量查询结果
     */
    private static List<BatchQueryResult> parseBatchResponse(String html) {
        List<BatchQueryResult> resultList = new ArrayList<>();

        try {
            Document doc = Jsoup.parse(html);

            // 检查新格式结果1
            Elements resultElements = doc.select("script:contains(qiehuan('PiLiang')) + br");
            if (!resultElements.isEmpty()) {
                Element resultContainer = resultElements.first();
                if (resultContainer != null) {
                    String fullText = resultContainer.parent().html();
                    int startIdx = fullText.indexOf("<script>qiehuan('PiLiang')</script><br>");
                    
                    if (startIdx > -1) {
                        startIdx += "<script>qiehuan('PiLiang')</script><br>".length();
                        String resultsSection = fullText.substring(startIdx);
                        String[] lines = resultsSection.split("<br>");
                        
                        for (String line : lines) {
                            String cleanLine = Jsoup.parse(line).text().trim();
                            if (!cleanLine.isEmpty()) {
                                BatchQueryResult result = new BatchQueryResult();
                                
                                if (cleanLine.contains("未记录")) {
                                    // 安全账号
                                    String account = cleanLine.replace("√", "").replace("未记录", "").trim();
                                    result.setAccount(account);
                                    result.setStatus("安全");
                                } else if (cleanLine.contains("已上黑")) {
                                    // 命中云黑
                                    String account = cleanLine.replace("×", "").replace("已上黑", "").trim();
                                    result.setAccount(account);
                                    result.setStatus("云黑");
                                } else if (cleanLine.contains("已避雷")) {
                                    // 避雷账号
                                    String account = cleanLine.replace("×", "").replace("已避雷", "").trim();
                                    result.setAccount(account);
                                    result.setStatus("避雷");
                                }
                                
                                if (result.getAccount() != null) {
                                    resultList.add(result);
                                }
                            }
                        }
                    }
                }
            } else {
                // 检查新格式结果2
                Elements redElements = doc.select("font[color=red]:contains(是云黑)");
                Elements blueElements = doc.select("font[color=blue]:contains(是避雷)");
                Elements greenElements = doc.select("font[color=green]:contains(未记录)");

                // 处理云黑信息
                for (Element element : redElements) {
                    String text = element.text().trim();
                    if (text.contains("是云黑")) {
                        BatchQueryResult result = new BatchQueryResult();
                        String account = text.replace("×", "").replace("是云黑.", "").trim();
                        result.setAccount(account);
                        result.setStatus("云黑");
                        resultList.add(result);
                    }
                }

                // 处理避雷信息
                for (Element element : blueElements) {
                    String text = element.text().trim();
                    if (text.contains("是避雷")) {
                        BatchQueryResult result = new BatchQueryResult();
                        String account = text.replace("×", "").replace("是避雷.", "").trim();
                        result.setAccount(account);
                        result.setStatus("避雷");
                        resultList.add(result);
                    }
                }
                
                // 处理安全账号
                for (Element element : greenElements) {
                    String text = element.text().trim();
                    if (text.contains("未记录")) {
                        BatchQueryResult result = new BatchQueryResult();
                        String account = text.replace("√", "").replace("未记录.", "").trim();
                        result.setAccount(account);
                        result.setStatus("安全");
                        resultList.add(result);
                    }
                }

                // 如果新格式2也没有结果，尝试旧版解析方法
                if (resultList.isEmpty()) {
                    return parseOldBatchResponse(html);
                }
            }
            
            return resultList;
        } catch (Exception e) {
            log.error("解析批量查询结果时出错", e);
            BatchQueryResult errorResult = new BatchQueryResult();
            errorResult.setStatus("错误");
            errorResult.setMessage("解析批量查询结果时出错: " + e.getMessage());
            resultList.add(errorResult);
            return resultList;
        }
    }

    /**
     * 解析旧版批量查询结果
     */
    private static List<BatchQueryResult> parseOldBatchResponse(String html) {
        List<BatchQueryResult> resultList = new ArrayList<>();

        try {
            Document doc = Jsoup.parse(html);
            String resultText = doc.select("center > font").text();

            if (resultText.isEmpty()) {
                BatchQueryResult noResult = new BatchQueryResult();
                noResult.setStatus("无结果");
                noResult.setMessage("未找到查询结果");
                resultList.add(noResult);
                return resultList;
            }

            String[] results = resultText.split(". ");
            for (String result : results) {
                BatchQueryResult batchResult = new BatchQueryResult();
                
                if (result.contains("是云黑")) {
                    String account = result.replace("是云黑", "").replace("×", "").trim();
                    batchResult.setAccount(account);
                    batchResult.setStatus("云黑");
                } else if (result.contains("是避雷")) {
                    String account = result.replace("是避雷", "").replace("×", "").trim();
                    batchResult.setAccount(account);
                    batchResult.setStatus("避雷");
                } else if (result.contains("未记录")) {
                    String account = result.replace("未记录", "").replace("√", "").trim();
                    batchResult.setAccount(account);
                    batchResult.setStatus("安全");
                }
                
                if (batchResult.getAccount() != null) {
                    resultList.add(batchResult);
                }
            }
            
            return resultList;
        } catch (Exception e) {
            log.error("解析旧版批量查询结果时出错", e);
            BatchQueryResult errorResult = new BatchQueryResult();
            errorResult.setStatus("错误");
            errorResult.setMessage("解析旧版批量查询结果时出错: " + e.getMessage());
            resultList.add(errorResult);
            return resultList;
        }
    }

    public static void main(String[] args) {
        // 示例账号
        String account = "3045170045";

        // 执行单个查询
        AccountInfo result = query(account);
        System.out.println(result);

        // 执行批量查询
        List<BatchQueryResult> batchResult = batchQuery("123456789\n3045170045\n3252436977");
        System.out.println(batchResult);
    }
}
