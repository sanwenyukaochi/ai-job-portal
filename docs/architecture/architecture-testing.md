# ArchUnit 架构测试规范

- 状态：Confirmed
- 确认日期：2026-09-22
- 工具：ArchUnit `1.4.2`、Taikai `1.60.0`
- 适用范围：所有 Java 业务模块和部署单元

## 目的

架构约束必须由测试自动执行，不能只依赖文档和代码评审。Taikai 提供通用 Java、JUnit 和 Spring 规则；项目特有的六边形依赖规则使用其底层 ArchUnit API 补充。

项目不使用 Spring Modulith 或 `ModularityTest`。Gradle 在编译期实施模块级物理隔离，ArchUnit + Taikai 验证模块内部及跨模块代码依赖规则，避免维护第二套模块模型。

参考资料：

- [Taikai 官方文档](https://enofex.github.io/taikai/documentation/)
- [Taikai GitHub](https://github.com/enofex/taikai)
- [ArchUnit 用户指南](https://www.archunit.org/userguide/html/000_Index.html)

## Gradle 依赖

版本统一登记在 `gradle/libs.versions.toml`。拥有架构测试的模块使用：

```kotlin
dependencies {
    testImplementation(libs.archunit.junit5)
    testImplementation(libs.taikai)
}
```

依赖只允许出现在测试配置中。实际测试位置在对应能力完成第 4 阶段、确认包和部署边界后确定；当前不创建虚假的空测试模块。

## 必须执行的通用规则

每个部署单元的 `ArchitectureTests` 至少启用以下规则：

```java
package com.portal.job.<context>;

import static com.tngtech.archunit.core.domain.JavaModifier.FINAL;
import static com.tngtech.archunit.core.domain.JavaModifier.PRIVATE;
import static com.tngtech.archunit.core.domain.JavaModifier.STATIC;

import com.enofex.taikai.Taikai;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

class ArchitectureTests {

    private static final String BASE_PACKAGE = "com.portal.job.<context>";

    @Test
    void shouldFulfillArchitectureConstraints() {
        Taikai.builder()
                .namespace(BASE_PACKAGE)
                .failOnEmpty(true)
                .java(java -> java
                        .noUsageOfDeprecatedAPIs()
                        .methodsShouldNotDeclareGenericExceptions()
                        .utilityClassesShouldBeFinalAndHavePrivateConstructor()
                        .imports(imports -> imports.shouldHaveNoCycles())
                        .naming(naming -> naming
                                .fieldsShouldNotMatch(".*(List|Set|Map)$")
                                .constantsShouldFollowConventions()
                                .interfacesShouldNotHavePrefixI()))
                .logging(logging -> logging
                        .loggersShouldFollowConventions(
                                Logger.class, "LOG", List.of(PRIVATE, STATIC, FINAL)))
                .test(test -> test.junit(junit -> junit
                        .classesShouldBePackagePrivate(".*Test(s)")
                        .classesShouldNotBeAnnotatedWithDisabled()
                        .methodsShouldNotBeAnnotatedWithDisabled()))
                .spring(spring -> spring
                        .noAutowiredFields()
                        .controllers(controllers -> controllers
                                .shouldBeAnnotatedWithRestController()
                                .namesShouldEndWithController()
                                .shouldNotDependOnOtherControllers()
                                .shouldBePackagePrivate())
                        .transactional(transactional -> transactional
                                .methodsShouldBePublic()
                                .shouldNotBeUsedInControllers()))
                .build()
                .checkAll();
    }
}
```

`<context>` 是第 4 阶段确认后的包名，不允许直接提交占位值。

## 必须补充的六边形规则

通用 Taikai 规则不能替代项目边界规则。每个部署单元还必须用 ArchUnit/Taikai 自定义规则验证：

1. Domain Core 不依赖 Application、Adapter、Container、Spring、Jakarta Persistence、Kafka、Feign 或 Jackson。
2. Application Service 只依赖 Domain Core 和经确认的 JDK/通用原语，不依赖任何 Adapter、Container 或 Spring Web/Data/Messaging。
3. Inbound Adapter 可以依赖 Application Service，不得依赖 Outbound Adapter。
4. Outbound Adapter 可以依赖 Application Service 和 Domain Core，不得依赖 Inbound Adapter 或其他 Outbound Adapter。
5. Container 可以依赖所有本部署单元模块，但其他层不得依赖 Container。
6. 不同限界上下文之间不得通过 Java/Gradle 依赖共享领域实体、持久化实体、Repository 或内部 DTO。
7. REST Controller 只能调用输入端口，不得直接依赖 JPA Repository、HTTP Client、Kafka Producer 或数据库 Entity。

规则应按已确认的包和模块边界编写，失败消息必须明确指出违反的依赖方向。

## 不采用的示例规则

不直接采用 Taikai 示例中的以下规则：

- `services().shouldBeAnnotatedWithService()`：Application Service 必须保持 Spring 无关，由 Container 装配。
- 对所有 `*Repository` 使用 Spring Repository 规则：应用层输出端口也可能以 `Repository` 命名，但它不是 Spring Data Repository。
- 在边界确认前固定 Spring Boot Application 所在包：实际根包由第 4 阶段确认。

这不是放宽约束，而是避免用框架命名规则破坏六边形依赖方向。

## 执行和门禁

- 架构测试属于普通 `test` 任务，`./gradlew check` 必须执行它们。
- 使用 `checkAll()` 一次报告全部违规，不使用 fail-fast 隐藏后续问题。
- 使用 `failOnEmpty(true)`，避免包名错误或无匹配类时测试假通过。
- 禁止使用 `@Disabled` 绕过失败；规则变更必须回到对应 spec 的第 4 阶段讨论。
- 第 5 阶段必须演示至少一个故意违反边界的 Red 结果，随后删除违规样例，再进入功能实现。
