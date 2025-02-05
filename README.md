# Lightning Trade - 量化交易系统

一个基于 Spring Boot 的量化交易系统，支持美股和港股市场的实时交易和策略回测。

## 最新更新

- 重构了所有交易策略实现，采用更高效的数据结构和算法
- 优化了策略工厂，支持更灵活的策略参数配置
- 重新设计了配置文件结构，使用 YAML 格式提供更好的可读性
- 改进了市场配置，增加了股票手数(lotSize)配置
- 优化了各个策略的实现：
  - MACD 策略：使用 EMA 计算，支持自定义信号周期
  - RSI 策略：支持自定义超买超卖阈值
  - 布林带策略：优化了计算方法，使用队列存储数据
  - 双均线策略：改进了交叉判断逻辑
  - MA 交叉策略：新增 1% 的确认区间
- 删除了冗余的 application.properties 文件

## 功能特点

### 实时交易
- 支持美股和港股市场
- 多种交易策略支持：
  - 双均线策略 (DOUBLE_MA)
  - MACD策略
  - 布林带策略 (BOLL)
  - RSI策略
- 可配置的股票代码和手数
- 市场级别的启用/禁用控制

### 策略回测
- 支持选择市场和股票
- 可配置初始资金
- 自定义回测日期范围
- 详细的回测结果展示：
  - 总收益率
  - 夏普比率
  - 最大回撤
  - 胜率
  - 权益曲线

## 使用说明

1. 访问首页 (http://localhost:8080/)
   - 查看各市场配置和交易状态
   - 监控策略参数和股票信息

2. 实时交易 (http://localhost:8080/trading)
   - 查看实时交易信号
   - 监控持仓状态

3. 策略回测 (http://localhost:8080/backtest)
   - 选择市场和股票
   - 设置策略参数
   - 配置回测周期
   - 查看回测结果和权益曲线

## 技术栈

- Spring Boot
- Thymeleaf
- Bootstrap 5
- Tiger API
- Java 17 