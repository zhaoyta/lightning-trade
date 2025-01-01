# Lightning Trade - 量化交易系统

## 项目介绍
基于 Spring Boot 的量化交易系统，集成老虎证券 API，支持美股和港股的自动化交易。系统提供了回测功能，可以在历史数据上测试交易策略的表现。

### 主要特性
- 支持美股和港股市场
- 提供多种交易策略（MA、布林带等）
- 完整的回测系统
- 实时交易执行
- 历史数据管理

## 项目架构

### 技术栈
- Spring Boot 2.7.0
- Java 11
- H2 Database
- Tiger Open API

### 核心模块
1. **配置模块** (`config`)
   - `ApiConfig`: Tiger API 客户端配置
   - `TigerApiConfig`: Tiger API 参数配置

2. **策略模块** (`strategy`)
   - 布林带策略
   - 双均线策略
   - 支持自定义策略扩展

3. **回测模块** (`backtest`)
   - 支持美股/港股回测
   - 提供回测报告
   - 交易成本计算

4. **交易执行** (`execution`)
   - 订单管理
   - 实时交易执行

## 代码结构 

## 如何运行

### 环境要求
- Java 11+
- Maven 3.6+
- VSCode + Java 插件
- Tiger API 账号

### 配置步骤

1. 配置 Tiger API
创建 `tiger_openapi_config.properties` 文件：
```properties
private_key_pk1=你的私钥
private_key_pk8=你的私钥
tiger_id=你的tiger_id
account=你的账号
license=你的license
env=环境配置
```

2. 配置 VSCode 启动参数
在 `.vscode/launch.json` 中添加：
```json
{
    "type": "java",
    "name": "LightningTradeApplication",
    "request": "launch",
    "mainClass": "com.lightningtrade.easyquant.LightningTradeApplication",
    "projectName": "lightning-trade",
    "env": {
        "tiger.config.path": "/path/to/tiger_openapi_config.properties"
    }
}
```

### 运行步骤
1. 克隆项目
2. 在 VSCode 中打开项目
3. 配置 Tiger API 参数
4. 运行 `LightningTradeApplication`

## 开发指南

### 添加新策略
1. 在 `strategy` 包下创建新的策略类
2. 实现交易逻辑
3. 在配置中启用新策略

### 回测流程
1. 配置回测参数
2. 选择要测试的策略
3. 运行回测
4. 分析回测结果

## 注意事项
- 确保 Tiger API 配置文件格式正确
- 注意交易时间（美股/港股市场时间）
- 建议先在回测环境中测试策略
- 实盘交易前请仔细检查配置参数 