# 问题与诊断总结

## 修复的问题

### 1. SwaggerConfig.java 中的 OAS_30 符号错误
- **问题**：`DocumentationType.OAS_30` 不存在
- **解决方案**：将其替换为 `DocumentationType.SWAGGER_2`，这是 Springfox 2.x 版本支持的文档类型
- **文件路径**：`src/main/java/com/example/vuehouduan/config/SwaggerConfig.java:19`

### 2. JwtInterceptor.java 中静态调用非静态方法的错误
- **问题**：直接调用 `JwtUtil.parseToken(token)` 是静态调用非静态方法
- **解决方案**：
  - 在 `JwtInterceptor` 类中添加 `@Autowired` 注解注入 `JwtUtil` 实例
  - 将静态调用改为实例方法调用：`jwtUtil.parseToken(token)`
- **文件路径**：`src/main/java/com/example/vuehouduan/interceptor/JwtInterceptor.java:42`

### 3. OrderServiceImpl.java 中的 QueryWrapper 类型推断错误
- **问题**：无法推断 `QueryWrapper<>` 的类型参数
- **解决方案**：
  - 显式指定类型参数：`QueryWrapper<Order> w = new QueryWrapper<Order>();`
  - 修复了创建 QueryWrapper 的方式，不再使用包装构造器
- **文件路径**：`src/main/java/com/example/vuehouduan/service/impl/OrderServiceImpl.java:263`

## 诊断过程

1. **代码分析**：查看了出错的文件，理解错误原因
2. **错误定位**：使用 `GetDiagnostics` 工具获取详细的错误信息
3. **修复验证**：通过 `mvn compile` 命令验证代码是否能够正常编译

## 验证结果

- **编译状态**：成功编译，无错误
- **警告**：存在一些关于未使用导入和类型安全的警告，但不影响程序运行
- **功能状态**：所有后端功能应该能够正常运行

## 技术说明

1. **Swagger 配置**：Springfox 2.x 版本使用 `DocumentationType.SWAGGER_2`，而 `OAS_30` 是 OpenAPI 3.0 的文档类型，在当前版本中不支持

2. **依赖注入**：`JwtUtil` 是一个 Spring 组件，应该通过依赖注入获取实例，而不是直接静态调用其方法

3. **泛型类型推断**：在创建 `QueryWrapper` 时，需要显式指定类型参数，以帮助编译器正确推断类型

## 剩余警告

1. **未使用的导入**：多个文件中存在未使用的导入语句
2. **类型安全警告**：`OrderServiceImpl.java` 中有一个关于未经检查的类型转换的警告
3. **非空注解警告**：一些方法参数缺少非空注解

这些警告不会影响程序的运行，但建议在后续开发中进行清理，以提高代码质量。