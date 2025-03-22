# qimeng兽圈防骗QQ查询API

> **声明：本项目非qimeng兽圈官方项目，仅为第三方爬虫接口服务，与qimeng官方无关。**

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Java](https://img.shields.io/badge/Java-17%2B-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-brightgreen)

qimeng兽圈防骗QQ查询API是一个用于查询QQ账号云黑/避雷信息的接口服务，基于Java爬虫技术开发，提供了简单易用的RESTful API，支持单个QQ查询和批量QQ查询功能。

## 📑 目录

- [功能特点](#功能特点)
- [快速开始](#快速开始)
- [接口说明](#接口说明)
  - [单个QQ查询](#1-单个qq查询)
  - [批量QQ查询](#2-批量qq查询)
- [状态码说明](#状态码说明)
- [部署说明](#部署说明)
- [技术栈](#技术栈)
- [常见问题](#常见问题)
- [贡献指南](#贡献指南)
- [许可证](#许可证)

## ✨ 功能特点

- 🔍 **单个QQ号查询**：查询指定QQ号的详细信息，包括账号状态、认证情况等
- 📋 **批量QQ号查询**：一次查询多个QQ号的状态信息，高效处理大量数据
- 📊 **结构化JSON响应**：返回标准格式的JSON数据，方便各类应用集成
- ⚡ **低延迟高效率**：优化的网络请求和解析逻辑，快速响应查询请求
- 🛡️ **稳定可靠**：自动处理各种异常情况，提供可靠的服务体验

## 🚀 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+

### 安装步骤

1. 克隆仓库
```bash
git clone https://github.com/yourusername/qimengApi.git
cd qimengApi
```

2. 编译打包
```bash
mvn clean package
```

3. 运行服务
```bash
java -jar target/qimengApi-0.0.1-SNAPSHOT.jar
```

4. 测试API
```bash
# 单个查询测试
curl -X POST "http://localhost:8124/api/qimeng/query" -d "account=123456789"

# 批量查询测试
curl -X POST "http://localhost:8124/api/qimeng/batchQuery" -d "accounts=123456789,987654321"
```

## 📖 接口说明

### 1. 单个QQ查询

**请求方式**: POST

**接口地址**: `/api/qimeng/query`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
| ------ | ---- | ---- | ---- |
| account | String | 是 | 需要查询的QQ号码 |

**响应参数**:

| 参数名 | 类型 | 说明 |
| ------ | ---- | ---- |
| code | Integer | 状态码，200表示成功 |
| message | String | 响应消息 |
| data | Object | 查询结果数据 |

**data字段说明**:

| 字段名 | 类型 | 说明 |
| ------ | ---- | ---- |
| account | String | 查询的QQ号 |
| status | String | 账号状态：云黑/避雷/安全 |
| warning | String | 警告信息 |
| teacher | String | 登记老师 |
| level | String | 云黑等级/避雷等级 |
| blackTime | String | 上黑时间/避雷时间 |
| reason | String | 云黑原因/避雷原因 |
| trustLevel | String | 可信度 |
| registerTime | String | 注册时间 |
| lastLoginTime | String | 上次登录时间 |
| rating | String | 评级 |
| phoneAuth | String | 手机认证状态 |
| realNameAuth | String | 实名认证状态 |
| wechatAuth | String | 微信认证状态 |
| alipayAuth | String | 支付宝认证状态 |
| groupCount | String | 所在群数 |
| monthlyActivity | String | 本月活跃 |
| totalActivity | String | 总计活跃 |
| firstActive | String | 首次活跃时间 |
| lastActive | String | 最后活跃时间 |
| templateCount | String | 打标模板数量 |
| score | String | 账号包裹积分 |
| otherTags | String | 其他标签 |

**请求示例**:

```bash
curl -X POST "http://localhost:8124/api/qimeng/query" -d "account=3045170045"
```

**响应示例**:

```json
{
    "code": 200,
    "data": {
        "account": "3045170045",
        "status": "云黑",
        "warning": "请立即终止交易!",
        "teacher": "Smart风控系统",
        "level": "4",
        "blackTime": "2024-10-10 19:48:24",
        "reason": "追加封杀：恶意以号养号 SMART: 文件存储异常. 前科: 拍卖逃单",
        "trustLevel": "完全不可信 完全不可信",
        "registerTime": "距今225天 [▲评级:B▲]",
        "lastLoginTime": "2024-08-08 15:24:04",
        "rating": null,
        "phoneAuth": "◈已认证手机◈",
        "realNameAuth": "◈已实名认证◈",
        "wechatAuth": "✘未绑定微信✘",
        "alipayAuth": "◈已绑定支付宝◈",
        "groupCount": "[ 2 [▽评级:E▽] ]",
        "monthlyActivity": "[ 0 [▽评级:E▽] ]",
        "totalActivity": "[ 18776 [▣评级:𝘼▣] ]",
        "firstActive": "[ 2023年8月8日 距今: 589天 ]",
        "lastActive": "[ ]",
        "templateCount": "[ 48个 ]",
        "score": "[ 1分 ]",
        "otherTags": "[云打标买断]"
    },
    "message": "查询成功"
}
```

### 2. 批量QQ查询

**请求方式**: POST

**接口地址**: `/api/qimeng/batchQuery`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
| ------ | ---- | ---- | ---- |
| accounts | String | 是 | 需要查询的QQ号码列表，以逗号分隔 |

**响应参数**:

| 参数名 | 类型 | 说明                   |
| ------ | ---- |----------------------|
| code | Integer | 状态码，200表示成功          |
| message | String | 响应消息                 |
| data | Array | 查询结果数据数组(仅返回云黑/避雷数据) |

**data数组中每个对象的字段说明**:

| 字段名 | 类型 | 说明 |
| ------ | ---- | ---- |
| account | String | 查询的QQ号 |
| status | String | 账号状态：云黑/避雷/安全 |
| message | String | 消息（错误情况下使用） |

**请求示例**:

```bash
curl -X POST "http://localhost:8124/api/qimeng/batchQuery" -d "accounts=3045170045,123456789,3252436977"
```

**响应示例**:

```json
{
  "code": 200,
  "data": [
    {
      "account": "3045170045",
      "status": "云黑",
      "message": null
    },
    {
      "account": "3252436977",
      "status": "避雷",
      "message": null
    }
  ],
  "message": "批量查询成功"
}
```

## 🔢 状态码说明

| 状态码 | 说明 |
| ------ | ---- |
| 200 | 请求成功 |
| 500 | 服务器内部错误 |

## 🔧 部署说明

1. 确保已安装Java 17或更高版本
2. 克隆项目到本地
3. 打包项目：`mvn clean package`
4. 运行jar包：`java -jar target/qimengApi-0.0.1-SNAPSHOT.jar`
5. 服务默认启动在8124端口，如需修改端口请在`application.properties`文件中修改

### Docker部署（可选）

```bash
# 构建Docker镜像
docker build -t qimeng-api .

# 运行容器
docker run -d -p 8124:8124 --name qimeng-api-container qimeng-api
```

## 💻 技术栈

- **Spring Boot** 3.4.4 - 主框架
- **HtmlUnit** 4.2.0 - 网页模拟与爬取
- **Jsoup** 1.17.2 - HTML解析
- **Lombok** 1.18.30 - 代码简化工具

## ❓ 常见问题

**Q: 查询结果与官网不一致怎么办？**  
A: 本项目通过爬虫获取数据，可能存在延迟。如遇重要决策，请以官方查询结果为准。

**Q: 如何提高查询速度？**  
A: 可以调整`QimengQueryUtil`中的超时参数，但降低超时可能导致查询失败率提高。

**Q: 如何处理批量查询时的大量请求？**  
A: 建议控制单次批量查询的数量，避免过多请求导致IP被限制。


## 📄 许可证

本项目采用MIT许可证，详情见LICENSE文件。 