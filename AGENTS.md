# GalleryPicker 项目规范

## 项目结构
- `PhotoPicker/`：图库选择器库模块，核心业务代码放在 `src/main/java/com/photo/picker/`。
- `app/`：示例应用模块，仅用于演示和本地验证。
- `gradle/`、`*.gradle.kts`：Gradle 构建配置；除非任务明确要求，不修改构建配置。
- 根目录图片、APK、README 等为发布与说明资产；除非任务明确要求，不移动、不删除。

## 命名与语言
- 代码标识符、文件名、目录名使用英文或 ASCII 字符。
- 面向用户的文案、业务日志、代码注释使用简体中文。
- 修改包含中文的文件时使用 UTF-8 无 BOM。

## 修改原则
- 优先最小改动，只修改与当前请求直接相关的代码。
- 不顺手重构、不格式化无关代码、不清理既有死代码。
- 涉及媒体类型、文件路径、URI、权限等逻辑时，按实际数据字段判断，避免用全局配置替代单项数据。

## 验证
- 常规库模块验证：`./gradlew.bat :PhotoPicker:compileDebugKotlin`
- 涉及示例应用或集成行为时：`./gradlew.bat :app:assembleDebug`
- 修改构建脚本后：`./gradlew.bat tasks`
