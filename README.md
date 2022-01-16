# Operation Log

> 参考 [mzt-biz-log](https://github.com/mouzt/mzt-biz-log) 实现的一款基于 spring aop 操作日志记录工具 支持自定义方法处理

<p align="center">
<img src="https://maven-badges.herokuapp.com/maven-central/cn.hangsman.operationlog/operation-log-core/badge.svg" />
<a target="_blank" href="https://github.com/hangsman/operation-log/blob/master/LICENSE">
    <img src="https://img.shields.io/apm/l/vim-mode.svg?color=yellow" />
</a>
<img src="https://img.shields.io/badge/JDK-1.8+-green" />
</p>

## 快速开始

### 添加依赖

```xml

<dependency>
    <groupId>cn.hangsman.operationlog</groupId>
    <artifactId>operation-log-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 启用注解

```java

@SpringBootApplication
@EnableOperationLog
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 添加 @OperationLog 注解

```java

@Service
public class OrderServiceImpl implements OrderService {
    @Override
    @OperationLog(
            category = "创建订单",
            fail = "订单创建失败：{#_errorMsg}",
            content = "创建了一个订单 订单id： {#order.orderID}"
    )
    public Order createOrder(Order order) {
        return new Order();
    }
}
```

## 其他用法

### 日志记录

实现 `OperationLogRecorder`

```java
public interface OperationLogRecorder {
    void record(OperationLog operationLog);
}
```

### 自定义方法

实现 `SpelFunction` 来创建一个spel表达式方法

```java

@Service
public class JsonSpelFunction implements SpelFunction {
    @Override
    public Object apply(Object value) {
        return JsonUtil.toJson(value);
    }

    @Override
    public String functionName() {
        return "json";
    }
}
```

在模板中通过 `$方法名称(变量)` 来使用

```java

@Service
public class OrderServiceImpl implements OrderService {
    @Override
    @OperationLog(
            category = "创建订单",
            fail = "订单创建失败：{#_errorMsg}",
            content = "创建了一个订单 订单id： {#order.orderID}",
            detail = "{$json(#_ret)}"
    )
    public Order createOrder(Order order) {
        return new Order();
    }
}
```

### 前置处理

格式为 `变量名={spel函数}`

之后在其他模板中可以使用 `#变量名` 获取函数返回值

```java

@Service
public class UserServiceImpl implements UserService {
    @Override
    @OperationLog(
            before = {"oldName={$oldNameFun(#user.id)}"},
            content = "将用户名从 {#oldName} 修改为 {#user.username}")
    public User updateName(User user) {

    }
}
```

## 支持

如果您喜欢该项目，请给项目点亮⭐️，感谢！
