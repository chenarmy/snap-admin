# SnapAdmin - Spring Boot 数据库管理面板

为你的 [Spring Boot®](https://spring.io/projects/spring-boot) 应用程序快速生成一个功能强大的数据库/CRUD 管理仪表板。

SnapAdmin 会扫描你的实体类，自动为数据库架构构建带有 CRUD 操作（以及更多功能）的 Web UI。不需要修改现有代码（好吧，你只需要添加 **1 行代码**）！

## 功能特性

* 带分页和排序的对象列表
* 创建/编辑对象
* 操作日志：通过 Web UI 执行的所有写操作历史记录
* 高级搜索和过滤
* 基于注解的自定义配置
* 数据导出（CSV、XLSX、JSONL）
* SQL 控制台：运行、保存自定义 SQL 查询并导出结果

## ORM 支持

SnapAdmin 支持 **MyBatis-Plus** ORM 框架：

* **MyBatis-Plus**：使用 MyBatis-Plus 注解（`@TableName`、`@TableId`、`@TableField` 等）

## 安装

### 1. Maven 依赖

SnapAdmin 通过 Maven 分发。在 `pom.xml` 中添加以下依赖：

```xml
<dependency>
    <groupId>tech.ailef</groupId>
    <artifactId>snap-admin</artifactId>
    <version>0.2.3</version>
</dependency>

<!-- MyBatis-Plus 支持（必需） -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    <version>3.5.7</version>
</dependency>
```

### 2. 配置 application.properties

在你的 `application.properties` 文件中添加以下配置：

```properties
# 启用 SnapAdmin（默认禁用）
snapadmin.enabled=true

# URL 路径前缀：http://localhost:8080/${baseUrl}/
snapadmin.baseUrl=admin

# 包含实体类的包路径（多个包用逗号分隔）
snapadmin.modelsPackage=your.models.package

# ORM 类型：使用 MYBATIS_PLUS
snapadmin.ormType=MYBATIS_PLUS

# 数据库配置（示例使用 H2 内存数据库）
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# MyBatis-Plus 配置
mybatis-plus.mapper.scan=your.mapper.package
mybatis-plus.global-config.db-config.id-type=auto
mybatis-plus.configuration.map-underscore-to-camel-case=true

# 可选参数

# 是否启用 SQL 控制台（默认 true）
# snapadmin.sqlConsoleEnabled=false

# 测试模式（运行测试时需要）
# snapadmin.testMode=false
```

### 3. 启用自动配置

在你的 Spring Boot 应用主类上添加注解：

```java
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import tech.ailef.snapadmin.external.SnapAdminAutoConfiguration;

@ImportAutoConfiguration(SnapAdminAutoConfiguration.class)
@SpringBootApplication
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

### 4. 实体类注解

#### 使用 MyBatis-Plus

```java
import com.baomidou.mybatisplus.annotation.*;

@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    @TableField("user_name")
    private String userName;
    
    @TableField("description")
    private String description; // 使用 @TableField 替代 @Lob
    
    // getters and setters
}
```

#### MyBatis-Plus Mapper 接口

使用 MyBatis-Plus 时，需要为每个实体创建 Mapper 接口：

```java
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 无需编写方法，BaseMapper 提供了基本的 CRUD 操作
}
```

### 5. 启动应用

启动你的 Spring Boot 应用后，访问：`http://localhost:8080/${snapadmin.baseUrl}`

默认地址：`http://localhost:8080/admin`

## 支持的注解

### MyBatis-Plus 注解
* 核心：`@TableName`、`@TableId`、`@TableField`
* ID 类型：`IdType.AUTO`、`IdType.INPUT`、`IdType.ASSIGN_ID`、`IdType.ASSIGN_UUID`
* 验证：所有 Jakarta Validation 注解（`jakarta.validation.constraints.*`）

## 支持的字段类型

以下是实体类中支持的字段类型。不支持类型的字段会被忽略，但功能可能受限。

* 数值：Double、Float、Integer、Short、Byte、BigDecimal、BigInteger
* 布尔：Boolean
* 字符串：String、UUID
* 日期：Date、LocalDate、LocalDateTime、OffsetDateTime、Instant
* 二进制：byte[]
* 枚举：Enum

## 内部实体类

SnapAdmin 内部使用以下实体类（使用 MyBatis-Plus）：

* `UserSetting` - 用户设置
* `UserAction` - 用户操作日志
* `ConsoleQuery` - 控制台查询

这些实体位于 `tech.ailef.snapadmin.internal.model` 包中，使用 MyBatis-Plus 注解。

## 项目结构

```
snap-admin/
├── src/main/java/tech/ailef/snapadmin/
│   ├── external/           # 外部 API 和核心功能
│   │   ├── SnapAdmin.java           # 主类，扫描实体并构建 Schema
│   │   ├── SnapAdminProperties.java  # 配置属性
│   │   ├── dbmapping/              # 数据库映射核心
│   │   │   ├── OrmType.java        # ORM 类型枚举（MYBATIS_PLUS）
│   │   │   ├── DbObjectSchema.java  # 数据库对象 Schema
│   │   │   ├── AnnotationUtils.java # 注解工具类（支持 MP）
│   │   │   └── ...
│   │   └── ...
│   └── internal/          # 内部实现
│       ├── model/         # 内部实体（使用 MyBatis-Plus 注解）
│       ├── mapper/        # MyBatis-Plus Mapper 接口
│       ├── service/       # 服务层
│       └── config/        # 配置类
└── src/main/resources/
    └── application.properties  # 配置文件
```

## 文档

* [最新 Javadoc](https://javadoc.io/doc/tech.ailef/snap-admin/)
* [参考指南](https://snapadmin.dev/docs/)

## 问题反馈

如果你发现问题或 Bug，请提交 Issue。提交时请包含尽可能多的信息：

* 提供相关实体类的代码（如果可能/相关）
* 提供完整的错误堆栈跟踪
* 说明你是否在 `application.properties` 中使用了特定配置或注解
* 如果问题在启动时发生，启用 `DEBUG` 级别日志并报告 `grep SnapAdmin` 返回的信息
* **说明你使用的 ORM 类型**（MYBATIS_PLUS）

## 许可证

MIT License - 详见 LICENSE 文件

## 版本历史

* **0.2.2** - 添加 MyBatis-Plus 支持
* **0.2.1** - Bug 修复和性能优化
* **0.2.0** - 项目从 "Spring Boot Database Admin" 重命名为 "SnapAdmin"

---

> 项目最近从 'Spring Boot Database Admin' 重命名为 'SnapAdmin'。
> 如果你之前在使用 'Spring Boot Database Admin'，请确保更新你的 `pom.xml` 和其他引用。
