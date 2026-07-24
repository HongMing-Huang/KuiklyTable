# KuiklyTable

> 基于 KuiklyUI 跨端框架构建的数据驱动表格组件库，提供简洁的 DSL 语法，支持 Android、iOS、macOS、H5、鸿蒙五端运行。

## ✨ 功能特性

| 类别 | 功能 | 说明 |
|------|------|------|
| 基础结构 | `Table` | 数据驱动表格主组件，DSL 入口 |
| | `column()` | 文本列定义，快速创建纯文本列 |
| | `columnRenderer()` | 自定义渲染列定义，支持嵌入任意组件 |
| 主题与样式 | DEFAULT | 默认主题，仅底部分隔线 |
| | STRIPE | 斑马纹主题，奇偶行交替背景色 |
| | BORDERED | 全边框主题，每个单元格都有完整边框 |
| | MINIMAL | 极简主题，无边框无分隔线 |
| 交互功能 | 排序 | 三态循环排序（无 → 升序 → 降序），支持自定义 comparator |
| | 行选择 | 多选高亮，`selectionChanged` 回调 |
| | 横向滚动 | 列宽溢出时横向滚动浏览 |
| | 吸顶表头 | 表头固定不随列表滚动 |
| | 行点击/长按 | `rowClick` / `rowLongClick` 事件回调 |
| 性能优化 | 虚拟化滚动 | 基于 List 组件的按需渲染，轻松应对大数据量 |
| 自定义渲染 | cellRenderer | 单元格内嵌入文本、图片、按钮、进度条等任意组件 |
| 企业级功能 | 树形表格 | TreeTable，多层级数据展示 |
| | 冻结列 | FrozenColumnTable，固定列不随横向滚动 |
| | 分页 | PaginatedTable，页码导航 |
| | 搜索过滤 | FilterableTable，关键词搜索 |
| | 展开折叠 | ExpandableTable，行点击展开详情 |
| | 滑动操作 | SwipeableTable，行尾操作按钮 |
| | 内联编辑 | EditableTable，单元格直接编辑 |
| | 批量操作 | BatchActionTable，选中后批量操作 |
| | 合并表头 | GroupedHeaderTable，多行表头分组 |
| | 无限滚动 | InfiniteTable，滚动自动加载 |
| | 空状态 | TableWithEmptyState，空数据占位视图 |

## 🚀 快速接入

### 1. 添加 Maven 仓库

在项目的 `settings.gradle.kts` 中添加：

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://mirrors.tencent.com/repository/maven-tencent/")
        }
    }
}
```

### 2. 添加依赖

在模块的 `build.gradle.kts` 中添加：

```kotlin
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.tencent.kuikly.table:kuikly-table:1.0.0-2.1.21")
            }
        }
    }
}
```

## 📖 基础用法

### 基础表格

最简单的表格使用方式，只需定义数据类、列配置和数据源：

```kotlin
data class Student(
    val name: String,
    val age: Int,
    val score: Int,
    val grade: String,
)

val students = listOf(
    Student("张三", 18, 92, "高三"),
    Student("李四", 17, 85, "高二"),
    Student("王五", 16, 78, "高一"),
    Student("赵六", 18, 95, "高三"),
)

View {
    attr { height(300f) }
    Table<Student> {
        attr {
            columns = listOf(
                column("姓名", flex = 2f) { it.name },
                column("年龄", flex = 1f, align = TableCellAlign.CENTER) { it.age.toString() },
                column("成绩", flex = 1f, align = TableCellAlign.CENTER) { it.score.toString() },
                column("年级", flex = 1f) { it.grade },
            )
            data = students
            theme = TableTheme.DEFAULT
        }
    }
}
```

### 可排序表格

启用排序功能，点击表头循环切换排序状态（无 → 升序 → 降序）：

```kotlin
Table<Student> {
    attr {
        columns = listOf(
            column("姓名", flex = 2f, sortable = true) { it.name },
            column("年龄", flex = 1f, sortable = true,
                comparator = { a, b -> a.age.compareTo(b.age) }
            ) { it.age.toString() },
            column("成绩", flex = 1f, sortable = true,
                comparator = { a, b -> a.score.compareTo(b.score) }
            ) { it.score.toString() },
        )
        data = students
        sortable = true
        theme = TableTheme.DEFAULT
    }
    event {
        sortChanged { state ->
            println("排序: ${state.columnKey} ${if (state.ascending) "升序" else "降序"}")
        }
    }
}
```

### 自定义单元格渲染

使用 `columnRenderer` 在单元格中嵌入彩色文本、标签、进度条等自定义组件：

```kotlin
data class Product(
    val name: String,
    val price: Double,
    val stock: Int,
    val status: String,
)

val products = listOf(
    Product("iPhone 15 Pro", 8999.0, 156, "在售"),
    Product("MacBook Air M3", 9499.0, 42, "在售"),
    Product("AirPods Pro 2", 1899.0, 0, "缺货"),
)

Table<Product> {
    attr {
        columns = listOf(
            // 普通文本列
            column("产品名称", flex = 2f) { it.name },
            // 自定义渲染：彩色价格文本
            columnRenderer<Product>("价格", flex = 1f) { product ->
                Text {
                    attr {
                        text("¥${product.price.toInt()}")
                        fontSize(13f)
                        color(if (product.price > 5000) Color(0xFFE53935) else Color(0xFF43A047))
                    }
                }
            },
            // 自定义渲染：库存进度条
            columnRenderer<Product>("库存", flex = 1f) { product ->
                View {
                    attr { flexDirectionRow(); alignItemsCenter() }
                    View {
                        attr { flex(1f); height(6f); backgroundColor(Color(0xFFEEEEEE)); borderRadius(3f) }
                        View {
                            val ratio = (product.stock.coerceAtMost(200).toFloat() / 200f).coerceIn(0f, 1f)
                            attr {
                                height(6f); flex(ratio); borderRadius(3f)
                                backgroundColor(
                                    when {
                                        product.stock == 0 -> Color(0xFFE53935)
                                        product.stock < 50 -> Color(0xFFFFA726)
                                        else -> Color(0xFF43A047)
                                    }
                                )
                            }
                        }
                    }
                    Text { attr { text(product.stock.toString()); fontSize(11f) } }
                }
            },
            // 自定义渲染：彩色状态标签
            columnRenderer<Product>("状态", flex = 1f) { product ->
                View {
                    val (bgColor, textColor) = when (product.status) {
                        "在售" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
                        "缺货" -> Color(0xFFFFF3E0) to Color(0xFFE65100)
                        "下架" -> Color(0xFFEEEEEE) to Color(0xFF757575)
                        else -> Color(0xFFF5F5F5) to Color(0xFF333333)
                    }
                    attr { backgroundColor(bgColor); borderRadius(4f); padding(4f, 2f, 4f, 2f) }
                    Text { attr { text(product.status); fontSize(12f); color(textColor) } }
                }
            },
        )
        data = products
        theme = TableTheme.DEFAULT
    }
}
```

### 横向滚动表格

列数较多时启用横向滚动，设置 `horizontalScroll` 和 `minTableWidth`：

```kotlin
data class OrderRecord(
    val orderId: String,
    val customer: String,
    val product: String,
    val quantity: Int,
    val unitPrice: Double,
    val total: Double,
    val status: String,
)

Table<OrderRecord> {
    attr {
        columns = listOf(
            column("订单号", flex = 1f, minWidth = 100f) { it.orderId },
            column("客户", flex = 1f, minWidth = 80f) { it.customer },
            column("商品", flex = 2f, minWidth = 160f) { it.product },
            column("数量", flex = 1f, minWidth = 60f, align = TableCellAlign.CENTER) { it.quantity.toString() },
            column("单价", flex = 1f, minWidth = 100f, align = TableCellAlign.RIGHT) { "¥${it.unitPrice.toInt()}" },
            column("总价", flex = 1f, minWidth = 100f, align = TableCellAlign.RIGHT) { "¥${it.total.toInt()}" },
            column("状态", flex = 1f, minWidth = 80f, align = TableCellAlign.CENTER) { it.status },
        )
        data = orders
        horizontalScroll = true
        minTableWidth = 800f
        theme = TableTheme.BORDERED
    }
}
```

### 可排序 + 行选择

同时启用排序和行选择功能：

```kotlin
Table<Student> {
    attr {
        columns = listOf(
            column("姓名", flex = 2f, sortable = true) { it.name },
            column("成绩", flex = 1f, sortable = true,
                comparator = { a, b -> a.score.compareTo(b.score) }
            ) { it.score.toString() },
        )
        data = students
        sortable = true
        selectable = true
        theme = TableTheme.STRIPE
    }
    event {
        rowClick { item, index ->
            println("点击行 $index: ${item.name}")
        }
        selectionChanged { indices ->
            println("已选中 ${indices.size} 行")
        }
        sortChanged { state ->
            println("排序: ${state.columnKey} ${if (state.ascending) "升序" else "降序"}")
        }
    }
}
```

### 树形表格

使用 `TreeTable` 展示多层级组织架构数据，支持初始展开节点配置：

```kotlin
data class OrgNode(val name: String, val role: String)

TreeTable<OrgNode> {
    attr {
        treeData = listOf(
            TreeNode(OrgNode("总公司", ""), id = "root", children = listOf(
                TreeNode(OrgNode("技术部", ""), id = "tech", children = listOf(
                    TreeNode(OrgNode("前端组", "前端"), id = "fe"),
                    TreeNode(OrgNode("后端组", "后端"), id = "be")
                ))
            ))
        )
        columns = listOf(
            column("名称", flex = 2f) { it.name },
            column("职责") { it.role }
        )
        initialExpandedIds = setOf("root", "tech")
    }
}
```

### 分页表格

使用 `PaginatedTable` 对大量数据进行分页展示，通过 `pageSize` 控制每页条数：

```kotlin
PaginatedTable<Student> {
    attr {
        data = students  // 100 条数据
        columns = columns
        pageSize = 20
    }
}
```

## 📋 API 参考

### TableColumn 列定义

通过 `column()` 或 `columnRenderer()` 便捷函数创建，或直接构造 `TableColumn<T>` 实例。

| 参数名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `key` | `String` | `title` | 列唯一标识，用于排序等功能 |
| `title` | `String` | — | 列标题文本 |
| `flex` | `Float` | `1f` | 弹性比例，控制列宽分配 |
| `minWidth` | `Float` | `0f` | 最小宽度（像素），0 表示不限制 |
| `align` | `TableCellAlign` | `LEFT` | 单元格对齐方式：`LEFT` / `CENTER` / `RIGHT` |
| `sortable` | `Boolean` | `false` | 是否允许按此列排序 |
| `comparator` | `((T, T) -> Int)?` | `null` | 自定义比较函数，优先于 textExtractor 用于排序 |
| `textExtractor` | `((T) -> String)?` | `null` | 文本提取函数，将数据项转换为显示文本 |
| `cellRenderer` | `(ViewContainer<*, *>.(T) -> Unit)?` | `null` | 自定义单元格渲染函数，在单元格容器内自由布局 |

### TableAttr 属性配置

通过 `attr {}` DSL 块配置表格属性：

| 属性名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `columns` | `List<TableColumn<T>>` | `emptyList()` | 列定义列表 |
| `data` | `List<T>` | `emptyList()` | 数据列表 |
| `rowHeight` | `Float` | `48f` | 数据行高度 |
| `headerHeight` | `Float` | `44f` | 表头高度 |
| `showHeader` | `Boolean` | `true` | 是否显示表头 |
| `stickyHeader` | `Boolean` | `true` | 表头是否吸顶（固定在顶部不随列表滚动） |
| `sortable` | `Boolean` | `false` | 是否启用排序功能 |
| `selectable` | `Boolean` | `false` | 是否启用行选择功能 |
| `cellPadding` | `Float` | `12f` | 单元格内边距（左右） |
| `theme` | `TableTheme` | `DEFAULT` | 表格主题 |
| `separatorColor` | `Color` | `Color(0xFFEEEEEE)` | 分隔线颜色 |
| `separatorHeight` | `Float` | `0.5f` | 分隔线高度 |
| `headerBackgroundColor` | `Color` | `Color(0xFFFAFAFA)` | 表头背景色 |
| `headerTextColor` | `Color` | `Color(0xFF333333)` | 表头文字颜色 |
| `headerFontSize` | `Float` | `14f` | 表头文字大小 |
| `rowBackgroundColor` | `Color` | `Color.WHITE` | 数据行背景色 |
| `stripeRowBackgroundColor` | `Color` | `Color(0xFFF9F9F9)` | 斑马纹偶数行背景色（STRIPE 主题） |
| `selectedColor` | `Color` | `Color(0xFFE3F2FD)` | 选中行背景色 |
| `cellTextColor` | `Color` | `Color(0xFF333333)` | 单元格文字颜色 |
| `cellFontSize` | `Float` | `14f` | 单元格文字大小 |
| `borderColor` | `Color` | `Color(0xFFDDDDDD)` | 边框颜色（BORDERED 主题） |
| `borderWidth` | `Float` | `0.5f` | 边框宽度 |
| `sortIndicatorColor` | `Color` | `Color(0xFF4A90D9)` | 排序指示器颜色 |
| `horizontalScroll` | `Boolean` | `false` | 是否启用横向滚动（列总宽度超出容器时） |
| `minTableWidth` | `Float` | `0f` | 表格内容最小宽度（横向滚动时有用），0 表示自动计算 |

### TableEvent 事件回调

通过 `event {}` DSL 块注册事件回调：

| 事件名 | DSL 函数 | 参数 | 说明 |
|--------|----------|------|------|
| `onRowClick` | `rowClick {}` | `(T, Int)` | 行点击事件，参数为 (数据项, 行索引) |
| `onRowLongClick` | `rowLongClick {}` | `(T, Int)` | 行长按事件，参数为 (数据项, 行索引) |
| `onSortChanged` | `sortChanged {}` | `(SortState)` | 排序变化事件，参数为排序状态 |
| `onSelectionChanged` | `selectionChanged {}` | `(Set<Int>)` | 选中行变化事件，参数为选中行索引集合 |

### TableTheme 主题

| 主题 | 说明 |
|------|------|
| `DEFAULT` | 默认主题：仅底部分隔线，简洁大方 |
| `STRIPE` | 斑马纹主题：奇偶行交替背景色，提升可读性 |
| `BORDERED` | 全边框主题：每个单元格都有完整边框，适合数据密集型展示 |
| `MINIMAL` | 极简主题：无边框无分隔线，极简风格 |

### TableCellAlign 对齐方式

| 值 | 说明 |
|----|------|
| `LEFT` | 左对齐（默认） |
| `CENTER` | 居中对齐 |
| `RIGHT` | 右对齐 |

### SortState 排序状态

| 属性 | 类型 | 说明 |
|------|------|------|
| `columnKey` | `String` | 排序列的 key，对应 `TableColumn.key` |
| `ascending` | `Boolean` | 是否升序，`true` 为升序，`false` 为降序 |

## 📱 示例

🌐 [在线交互演示](https://hongming-huang.github.io/KuiklyTable/)

完整示例见 [`shared/src/commonMain/kotlin/com/tencent/kuikly/table/demo/TableDemoPage.kt`](shared/src/commonMain/kotlin/com/tencent/kuikly/table/demo/TableDemoPage.kt)，包含 20 个演示 Section：

1. **基础表格** — 最简单的使用方式
2. **可排序表格** — 点击表头排序
3. **可选择表格** — 多选行
4. **斑马纹主题** — STRIPE 主题
5. **全边框主题** — BORDERED 主题
6. **自定义单元格渲染** — 嵌入自定义组件
7. **横向滚动表格** — 列数较多时横向滚动浏览
8. **极简主题** — MINIMAL 主题
9. **大数据量** — 虚拟化滚动性能演示
10. **树形表格** — 多层级组织架构展示
11. **冻结列** — 固定列不随横向滚动
12. **分页表格** — 页码导航大数据分页
13. **搜索过滤** — 关键词实时搜索
14. **展开折叠** — 行点击展开详情
15. **滑动操作** — 行尾操作按钮
16. **内联编辑** — 单元格直接编辑
17. **批量操作** — 多选后批量操作
18. **合并表头** — 多行表头分组
19. **无限滚动** — 滚动自动加载
20. **空状态** — 空数据占位视图

## 🌐 平台支持

| 平台 | 状态 |
|------|------|
| Android | ✅ 支持 |
| iOS | ✅ 支持 |
| macOS | ✅ 支持 |
| JS/H5 | ✅ 支持 |
| OHOS (鸿蒙) | ✅ 支持 |

## 🔗 相关资源

- [KuiklyUI 官方文档](https://github.com/Tencent-TDS/KuiklyUI)
- [KuiklyUI 仓库](https://github.com/Tencent-TDS/KuiklyUI)

## 📄 License

[KuiklyUI License](https://github.com/Tencent-TDS/KuiklyUI/blob/main/LICENSE)
