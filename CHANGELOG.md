# Changelog

All notable changes to this project will be documented in this file.

## [1.0.0] - 2025-07-24

### Added
- 基础表格展示：多行多列数据、边框、内边距、对齐方式
- 双向滚动：纵向虚拟化 + 横向 Scroller
- 声明式 DSL：`Table{}` 入口 + `column()` / `columnRenderer()` 便捷函数
- 4 种预置主题：DEFAULT、STRIPE、BORDERED、MINIMAL
- 单元格自定义渲染：`cellRenderer` ViewBuilder，支持嵌入任意组件
- 排序功能：三态循环（无 → 升序 → 降序）+ `comparator` 数值排序
- 行选择：多选高亮 + `selectionChanged` 回调
- 行交互：`rowClick` 点击事件 + `rowLongClick` 长按事件
- 吸顶表头：`stickyHeader` 属性，表头固定不随列表滚动
- 虚拟化滚动：基于 List 组件的按需渲染，轻松应对大数据量
- 横向滚动：`horizontalScroll` + `minTableWidth`，列宽溢出时横向滚动浏览
- 完整的样式配置：分隔线、边框、表头、斑马纹、选中行等 20+ 可配置属性
- 5 平台支持：Android、iOS、macOS、JS/H5、OHOS
- 完整 KDoc API 文档
- 9 个演示 Section 的 Demo 页面
