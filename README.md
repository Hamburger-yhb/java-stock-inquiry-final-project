# java-stock-inquiry-final-project
A Java Swing stock query application developed for my final course project.

# 股票查询系统

一个使用 Java Swing 开发的桌面股票查询工具，
通过 iTick API 获取股票资料和行情。

## 已实现功能

- 根据市场和股票代码查询股票
- 展示名称、昨收价、当前价、涨跌额和涨跌幅
- 手动刷新行情
- 配置和测试 API Token

## 技术栈

- Java
- Swing
- Maven
- Jackson
- iTick API

## 项目结构

- client：HTTP 接口客户端
- service：股票查询和行情服务
- model：数据模型
- ui：桌面界面
- util：工具类

## 使用说明

1. 使用支持 Maven 的 Java 开发环境打开项目。
2. 加载 pom.xml 中的依赖。
3. 运行 harry.stockinqbasic.Main。
4. 在设置中配置自己的 iTick API Token。

## 当前状态

本项目处于学习和改进阶段。
行情字段、并发更新及错误反馈等问题正在完善。
