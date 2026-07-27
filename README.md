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

### column() 函数签名

创建文本列的便捷函数，通过 `extractor` 将数据项转换为显示文本：

```kotlin
fun <T> column(
    title: String,
    flex: Float = 1f,
    key: String = title,
    minWidth: Float = 0f,
    align: TableCellAlign = TableCellAlign.LEFT,
    sortable: Boolean = false,
    comparator: ((T, T) -> Int)? = null,
    extractor: (T) -> String,
): TableColumn<T>
```

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `title` | `String` | — | 列标题 |
| `flex` | `Float` | `1f` | 弹性比例 |
| `key` | `String` | `title` | 列标识 |
| `minWidth` | `Float` | `0f` | 最小宽度（像素） |
| `align` | `TableCellAlign` | `LEFT` | 对齐方式 |
| `sortable` | `Boolean` | `false` | 是否可排序 |
| `comparator` | `((T, T) -> Int)?` | `null` | 自定义比较函数 |
| `extractor` | `(T) -> String` | — | 文本提取函数 |

### columnRenderer() 函数签名

创建自定义渲染列的便捷函数，通过 `renderer` 在单元格内自由布局：

```kotlin
fun <T> columnRenderer(
    title: String,
    flex: Float = 1f,
    key: String = title,
    minWidth: Float = 0f,
    align: TableCellAlign = TableCellAlign.LEFT,
    sortable: Boolean = false,
    comparator: ((T, T) -> Int)? = null,
    renderer: ViewContainer<*, *>.(T) -> Unit,
): TableColumn<T>
```

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `title` | `String` | — | 列标题 |
| `flex` | `Float` | `1f` | 弹性比例 |
| `key` | `String` | `title` | 列标识 |
| `minWidth` | `Float` | `0f` | 最小宽度（像素） |
| `align` | `TableCellAlign` | `LEFT` | 对齐方式 |
| `sortable` | `Boolean` | `false` | 是否可排序 |
| `comparator` | `((T, T) -> Int)?` | `null` | 自定义比较函数 |
| `renderer` | `ViewContainer<*, *>.(T) -> Unit` | — | 自定义渲染函数 |

### TreeNode&lt;T&gt; 树节点

树形表格的数据模型，递归结构：

```kotlin
data class TreeNode<T>(
    val data: T,
    val children: List<TreeNode<T>>? = null,
    val id: String = "",
    val expanded: Boolean = false,
)
```

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `data` | `T` | — | 节点数据 |
| `children` | `List<TreeNode<T>>?` | `null` | 子节点列表，`null` 表示叶子节点 |
| `id` | `String` | `""` | 节点唯一标识，用于展开/折叠状态管理 |
| `expanded` | `Boolean` | `false` | 是否展开 |

### 数据模型

#### PageInfo 分页信息

```kotlin
data class PageInfo(
    val currentPage: Int = 1,
    val pageSize: Int = 20,
    val totalItems: Int = 0,
)
```

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `currentPage` | `Int` | `1` | 当前页码（从 1 开始） |
| `pageSize` | `Int` | `20` | 每页条数 |
| `totalItems` | `Int` | `0` | 总数据条数 |
| `totalPages` | `Int` | 计算属性 | 总页数 |

#### SwipeAction 滑动操作定义

```kotlin
data class SwipeAction(
    val label: String,
    val color: Color = Color(0xFFFF4444),
    val icon: String? = null,
    val onClick: () -> Unit = {},
)
```

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `label` | `String` | — | 操作标签文本 |
| `color` | `Color` | `Color(0xFFFF4444)` | 操作按钮背景色 |
| `icon` | `String?` | `null` | 操作图标文本（可选） |
| `onClick` | `() -> Unit` | `{}` | 点击回调 |

#### HeaderGroup 表头分组定义

```kotlin
data class HeaderGroup(
    val title: String,
    val childColumnKeys: List<String>,
)
```

| 属性 | 类型 | 说明 |
|------|------|------|
| `title` | `String` | 分组标题 |
| `childColumnKeys` | `List<String>` | 该分组包含的列 key 列表 |

#### BatchAction 批量操作定义

```kotlin
data class BatchAction(
    val label: String,
    val icon: String? = null,
    val onClick: (Set<Int>) -> Unit = {},
)
```

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `label` | `String` | — | 操作标签文本 |
| `icon` | `String?` | `null` | 操作图标文本（可选） |
| `onClick` | `(Set<Int>) -> Unit` | `{}` | 点击回调，参数为选中行索引集合 |

#### EditTrigger 编辑触发方式

| 值 | 说明 |
|----|------|
| `CLICK` | 单击触发编辑 |
| `DOUBLE_CLICK` | 双击触发编辑（需外部实现双击检测逻辑） |

### 企业级组件 API

#### 1. PaginatedTable — 分页表格

DSL 入口：`PaginatedTable<T> { attr { ... } event { ... } }`

**属性（`PaginatedTableAttr`）**

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `data` | `List<T>` | `emptyList()` | 全量数据列表 |
| `columns` | `List<TableColumn<T>>` | `emptyList()` | 列定义列表 |
| `pageSize` | `Int` | `20` | 每页条数 |
| `pageSizeOptions` | `List<Int>` | `listOf(10, 20, 50, 100)` | 可选每页条数列表 |
| `tableInit` | `(TableView<T>.() -> Unit)?` | `null` | 额外表格配置回调（可覆盖样式等属性） |
| `rowHeight` | `Float` | `48f` | 数据行高度 |
| `headerHeight` | `Float` | `44f` | 表头高度 |
| `theme` | `TableTheme` | `DEFAULT` | 表格主题 |
| `paginationBarHeight` | `Float` | `48f` | 分页栏高度 |
| `paginationBarBackgroundColor` | `Color` | `Color(0xFFFAFAFA)` | 分页栏背景色 |
| `paginationBarTextColor` | `Color` | `Color(0xFF333333)` | 分页栏文字颜色 |
| `paginationBarDisabledColor` | `Color` | `Color(0xFFCCCCCC)` | 分页栏按钮禁用颜色 |
| `paginationBarFontSize` | `Float` | `14f` | 分页栏字号 |
| `maxVisiblePages` | `Int` | `5` | 分页栏中可见的最大页码按钮数量 |

**事件（`PaginatedTableEvent`）**

| 事件 | DSL 函数 | 参数类型 | 说明 |
|------|----------|----------|------|
| `onPageChanged` | `pageChanged {}` | `(Int) -> Unit` | 页码变化回调，参数为新页码 |
| `onPageSizeChanged` | `pageSizeChanged {}` | `(Int) -> Unit` | 每页条数变化回调，参数为新的每页条数 |

**关键方法**

| 方法 | 参数 | 说明 |
|------|------|------|
| `getPageData()` | 无 | 返回当前页的数据切片 |
| `prevPage()` | 无 | 跳转到上一页，已到首页时无效 |
| `nextPage()` | 无 | 跳转到下一页，已到末页时无效 |
| `goToPage(page: Int)` | `page` 目标页码 | 跳转到指定页码，自动 clamp 到合法范围 |
| `getVisiblePages()` | 无 | 计算当前可见的页码列表，-1 表示省略号 |

#### 2. FilterableTable — 搜索过滤

DSL 入口：`FilterableTable<T> { attr { ... } event { ... } }`

**属性（`SearchFilterTableAttr`）**

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `data` | `List<T>` | `emptyList()` | 全量数据列表 |
| `columns` | `List<TableColumn<T>>` | `emptyList()` | 列定义列表 |
| `searchPlaceholder` | `String` | `"搜索..."` | 搜索框占位文本 |
| `filterExtractor` | `((T, String) -> Boolean)?` | `null` | 自定义匹配逻辑，为 null 时自动使用所有列的 `textExtractor` 进行匹配 |
| `debounceMs` | `Long` | `300` | 防抖延迟毫秒数（当前未生效，保留为扩展接口） |
| `tableInit` | `(TableView<T>.() -> Unit)?` | `null` | 额外表格配置回调 |
| `searchBarHeight` | `Float` | `44f` | 搜索栏高度 |
| `searchBarPadding` | `Float` | `8f` | 搜索栏内边距 |
| `searchBarBackgroundColor` | `Color` | `Color(0xFFF5F5F5)` | 搜索栏背景色 |

**事件（`SearchFilterTableEvent`）**

| 事件 | DSL 函数 | 参数类型 | 说明 |
|------|----------|----------|------|
| `onKeywordChanged` | `keywordChanged {}` | `(String) -> Unit` | 搜索关键词变化回调 |
| `onFilterResultChanged` | `filterResultChanged {}` | `(Int) -> Unit` | 过滤结果数量变化回调，参数为过滤后的数据量 |

#### 3. TreeTable — 树形表格

DSL 入口：`TreeTable<T> { attr { ... } event { ... } }`

**属性（`TreeTableAttr`）**

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `treeData` | `List<TreeNode<T>>` | `emptyList()` | 树形数据列表 |
| `columns` | `List<TableColumn<T>>` | `emptyList()` | 列定义列表（第一列会被包装为树形列） |
| `indentWidth` | `Float` | `24f` | 每层缩进宽度（像素） |
| `expandIcon` | `String` | `"▶"` | 折叠态图标文本 |
| `collapseIcon` | `String` | `"▼"` | 展开态图标文本 |
| `leafIcon` | `String` | `" "` | 叶子节点占位文本 |
| `nodeKeyExtractor` | `((T) -> String)?` | `null` | 节点 key 提取器，当 `TreeNode.id` 为空时使用 |
| `initialExpandedIds` | `Set<String>` | `emptySet()` | 初始展开的节点 ID 集合 |
| `headerHeight` | `Float` | `44f` | 表头高度 |
| `headerBackgroundColor` | `Color` | `Color(0xFFFAFAFA)` | 表头背景色 |
| `headerTextColor` | `Color` | `Color(0xFF333333)` | 表头文字颜色 |
| `headerFontSize` | `Float` | `14f` | 表头文字大小 |
| `rowHeight` | `Float` | `48f` | 数据行高度 |
| `cellPadding` | `Float` | `12f` | 单元格内边距（左右） |
| `cellTextColor` | `Color` | `Color(0xFF333333)` | 单元格文字颜色 |
| `cellFontSize` | `Float` | `14f` | 单元格文字大小 |
| `rowBackgroundColor` | `Color` | `Color.WHITE` | 默认行背景色 |
| `stripeRowBackgroundColor` | `Color` | `Color(0xFFF9F9F9)` | 斑马纹偶数行背景色 |
| `separatorColor` | `Color` | `Color(0xFFEEEEEE)` | 分隔线颜色 |
| `separatorHeight` | `Float` | `0.5f` | 分隔线高度 |

**事件（`TreeTableEvent`）**

| 事件 | DSL 函数 | 参数类型 | 说明 |
|------|----------|----------|------|
| `onNodeExpand` | `nodeExpand {}` | `(TreeNode<T>, Boolean) -> Unit` | 节点展开/折叠回调，参数为 (节点, 是否展开) |
| `onNodeClick` | `nodeClick {}` | `(TreeNode<T>, Int) -> Unit` | 节点点击回调，参数为 (节点, 展平后行索引) |

**关键方法**

| 方法 | 参数 | 说明 |
|------|------|------|
| `toggleNode(node: TreeNode<T>)` | `node` 要切换的树节点 | 切换节点的展开/折叠状态 |
| `expandNode(node: TreeNode<T>)` | `node` 要展开的树节点 | 展开指定节点 |
| `collapseNode(node: TreeNode<T>)` | `node` 要折叠的树节点 | 折叠指定节点 |

#### 4. FrozenColumnTable — 冻结列

DSL 入口：`FrozenColumnTable<T> { attr { ... } event { ... } }`

**属性（`FrozenColumnTableAttr`）**

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `data` | `List<T>` | `emptyList()` | 数据列表 |
| `columns` | `List<TableColumn<T>>` | `emptyList()` | 全部列定义列表 |
| `frozenColumnKeys` | `List<String>` | `emptyList()` | 冻结列的 key 列表（左侧冻结） |
| `frozenColumnWidth` | `Float` | `0f` | 冻结列总宽度，0 时自动计算 |
| `rowHeight` | `Float` | `48f` | 数据行高度 |
| `headerHeight` | `Float` | `44f` | 表头高度 |
| `showHeader` | `Boolean` | `true` | 是否显示表头 |
| `stickyHeader` | `Boolean` | `true` | 表头是否吸顶 |
| `cellPadding` | `Float` | `12f` | 单元格内边距（左右） |
| `theme` | `TableTheme` | `DEFAULT` | 表格主题 |
| `separatorColor` | `Color` | `Color(0xFFEEEEEE)` | 分隔线颜色 |
| `separatorHeight` | `Float` | `0.5f` | 分隔线高度 |
| `headerBackgroundColor` | `Color` | `Color(0xFFFAFAFA)` | 表头背景色 |
| `headerTextColor` | `Color` | `Color(0xFF333333)` | 表头文字颜色 |
| `headerFontSize` | `Float` | `14f` | 表头文字大小 |
| `rowBackgroundColor` | `Color` | `Color.WHITE` | 数据行背景色 |
| `stripeRowBackgroundColor` | `Color` | `Color(0xFFF9F9F9)` | 斑马纹偶数行背景色 |
| `selectedColor` | `Color` | `Color(0xFFE3F2FD)` | 选中行背景色 |
| `cellTextColor` | `Color` | `Color(0xFF333333)` | 单元格文字颜色 |
| `cellFontSize` | `Float` | `14f` | 单元格文字大小 |
| `borderColor` | `Color` | `Color(0xFFDDDDDD)` | 边框颜色 |
| `borderWidth` | `Float` | `0.5f` | 边框宽度 |
| `sortIndicatorColor` | `Color` | `Color(0xFF4A90D9)` | 排序指示器颜色 |
| `frozenSeparatorColor` | `Color` | `Color(0xFFDDDDDD)` | 冻结列与主列之间的分隔线颜色 |
| `frozenSeparatorWidth` | `Float` | `1f` | 冻结列与主列之间的分隔线宽度 |
| `tableInit` | `(TableView<T>.() -> Unit)?` | `null` | 额外配置回调 |

**事件（`FrozenColumnTableEvent`）**

| 事件 | DSL 函数 | 参数类型 | 说明 |
|------|----------|----------|------|
| `onRowClick` | `rowClick {}` | `(T, Int) -> Unit` | 行点击事件 |
| `onRowLongClick` | `rowLongClick {}` | `(T, Int) -> Unit` | 行长按事件 |

**关键方法**

| 方法 | 参数 | 说明 |
|------|------|------|
| `notifyDataChanged()` | 无 | 通知数据已变化，触发 UI 重建 |

#### 5. ExpandableTable — 展开折叠

DSL 入口：`ExpandableTable<T> { attr { ... } event { ... } }`

**属性（`ExpandableTableAttr`）**

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `data` | `List<T>` | `emptyList()` | 数据列表 |
| `columns` | `List<TableColumn<T>>` | `emptyList()` | 列定义列表 |
| `expandRenderer` | `(ViewContainer<*, *>.(T) -> Unit)?` | `null` | 展开内容渲染器 |
| `expandColumnWidth` | `Float` | `32f` | 展开/折叠图标列宽度 |
| `expandIcon` | `String` | `"▶"` | 折叠态图标文本 |
| `collapseIcon` | `String` | `"▼"` | 展开态图标文本 |
| `expandRowHeight` | `Float` | `0f` | 展开行高度，0 表示自动高度 |

**事件（`ExpandableTableEvent`）**

| 事件 | DSL 函数 | 参数类型 | 说明 |
|------|----------|----------|------|
| `onExpandChanged` | `expandChanged {}` | `(Int, Boolean) -> Unit` | 展开状态变化回调，参数为 (行索引, 是否展开) |
| `onRowClick` | `rowClick {}` | `(T, Int) -> Unit` | 行点击回调 |

**关键方法**

| 方法 | 参数 | 说明 |
|------|------|------|
| `toggleExpand(index: Int)` | `index` 原始数据行索引 | 切换指定行的展开/折叠状态 |
| `getFlattenedData()` | 无 | 获取展平后的数据列表（含展开详情行） |

#### 6. InfiniteTable — 无限滚动

DSL 入口：`InfiniteTable<T> { attr { ... } event { ... } }`

**属性（`InfiniteTableAttr`）**

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `data` | `List<T>` | `emptyList()` | 初始数据列表 |
| `columns` | `List<TableColumn<T>>` | `emptyList()` | 列定义列表 |
| `loadThreshold` | `Int` | `5` | 距离底部多少行时触发加载 |
| `loadingText` | `String` | `"加载中..."` | 加载中提示文案 |
| `noMoreText` | `String` | `"没有更多了"` | 无更多数据提示文案 |
| `loadMoreButtonText` | `String` | `"加载更多"` | “加载更多”按钮文案 |
| `isLoading` | `Boolean` | `false` | 外部控制加载状态 |
| `hasMore` | `Boolean` | `true` | 是否还有更多数据 |
| `tableInit` | `(TableView<T>.() -> Unit)?` | `null` | 额外表格配置回调 |

**事件（`InfiniteTableEvent`）**

| 事件 | 参数类型 | 说明 |
|------|----------|------|
| `onLoadMore` | `() -> Unit` | 加载更多回调，滚动到底部或手动触发时调用 |

**关键方法**

| 方法 | 参数 | 说明 |
|------|------|------|
| `appendData(newItems: List<T>)` | `newItems` 新数据项列表 | 追加新数据到表格，自动将 loading 置为 false |
| `loadMore(items: List<T>)` | `items` 新加载的数据项列表 | 追加数据并自动检测是否需要继续加载 |
| `checkAndLoadMore(visibleIndex: Int, totalItems: Int)` | 可见索引和总数 | 基于可见索引检测是否应触发加载 |
| `checkAndLoadMore()` | 无 | 无参版本，基于内部数据量自动判断 |
| `triggerLoadMore()` | 无 | 手动触发加载更多 |
| `setLoading(isLoading: Boolean)` | `isLoading` | 手动设置加载状态 |

#### 7. EditableTable — 内联编辑

DSL 入口：`EditableTable<T> { attr { ... } event { ... } }`

**属性（`EditableTableAttr`）**

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `data` | `List<T>` | `emptyList()` | 数据列表 |
| `columns` | `List<TableColumn<T>>` | `emptyList()` | 列定义列表 |
| `editableColumns` | `Set<String>` | `emptySet()` | 可编辑的列 key 集合 |
| `editTrigger` | `EditTrigger` | `EditTrigger.CLICK` | 编辑触发方式 |
| `cellTextColor` | `Color` | `Color(0xFF333333)` | 单元格文字颜色 |
| `cellFontSize` | `Float` | `14f` | 单元格文字大小 |
| `onSave` | `((item: T, index: Int, columnKey: String, newValue: String) -> T)?` | `null` | 保存回调，返回更新后的数据项 |
| `tableInit` | `(TableView<T>.() -> Unit)?` | `null` | 额外表格配置回调 |

**事件（`EditableTableEvent`）**

| 事件 | DSL 函数 | 参数类型 | 说明 |
|------|----------|----------|------|
| `onEditStart` | `editStart {}` | `(Int, String) -> Unit` | 开始编辑回调，参数为 (行索引, 列 key) |
| `onEditEnd` | `editEnd {}` | `(Int, String) -> Unit` | 结束编辑回调，参数为 (行索引, 列 key) |

**关键方法**

| 方法 | 参数 | 说明 |
|------|------|------|
| `startEdit(index: Int, columnKey: String, currentValue: String)` | 行索引、列 key、当前值 | 开始编辑指定单元格 |
| `saveEdit(item: T, index: Int)` | 当前行数据项、行索引 | 保存当前编辑内容 |
| `cancelEdit()` | 无 | 取消当前编辑，恢复原始值 |

#### 8. SwipeableTable — 滑动操作

DSL 入口：`SwipeableTable<T> { attr { ... } }`

**属性（`SwipeableTableAttr`）**

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `data` | `List<T>` | `emptyList()` | 数据列表 |
| `columns` | `List<TableColumn<T>>` | `emptyList()` | 列定义列表 |
| `swipeActions` | `List<SwipeAction>` | `emptyList()` | 滑动操作列表 |
| `actionButtonWidth` | `Float` | `72f` | 每个操作按钮宽度 |
| `rowHeight` | `Float` | `48f` | 数据行高度 |
| `actionBinder` | `((T, SwipeAction) -> Unit)?` | `null` | 将 SwipeAction 绑定到具体数据项的包装函数 |
| `showActionsOnTap` | `Boolean` | `true` | 是否通过点击行来展示/隐藏操作按钮，false 时操作列始终显示 |
| `tableInit` | `(TableView<T>.() -> Unit)?` | `null` | 额外表格配置回调 |

> 无自定义事件，通过 `actionBinder` 或 `SwipeAction.onClick` 处理操作点击。

#### 9. BatchActionTable — 批量操作

DSL 入口：`BatchActionTable<T> { attr { ... } event { ... } }`

**属性（`BatchActionTableAttr`）**

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `data` | `List<T>` | `emptyList()` | 数据列表 |
| `columns` | `List<TableColumn<T>>` | `emptyList()` | 列定义列表 |
| `batchActions` | `List<BatchAction>` | `emptyList()` | 批量操作列表 |
| `tableInit` | `(TableView<T>.() -> Unit)?` | `null` | 额外表格配置回调 |
| `rowHeight` | `Float` | `48f` | 数据行高度 |
| `theme` | `TableTheme` | `DEFAULT` | 表格主题 |
| `actionBarHeight` | `Float` | `56f` | 批量操作栏高度 |
| `actionBarBackgroundColor` | `Color` | `Color(0xFFE3F2FD)` | 批量操作栏背景色 |
| `actionBarTextColor` | `Color` | `Color(0xFF333333)` | 批量操作栏文字颜色 |
| `actionButtonTextColor` | `Color` | `Color(0xFF4A90D9)` | 操作按钮文字颜色 |
| `cancelButtonTextColor` | `Color` | `Color(0xFF999999)` | 取消按钮文字颜色 |
| `actionBarFontSize` | `Float` | `14f` | 操作栏字号 |

**事件（`BatchActionTableEvent`）**

| 事件 | DSL 函数 | 参数类型 | 说明 |
|------|----------|----------|------|
| `onBatchAction` | `batchAction {}` | `(BatchAction, Set<Int>) -> Unit` | 批量操作执行回调，参数为 (操作定义, 选中行索引集合) |
| `onSelectionChanged` | `selectionChanged {}` | `(Set<Int>) -> Unit` | 选中行变化回调 |

**关键方法**

| 方法 | 参数 | 说明 |
|------|------|------|
| `clearSelection()` | 无 | 清空所有选中行 |
| `executeAction(action: BatchAction)` | `action` 要执行的批量操作 | 执行批量操作并清空选择 |

#### 10. GroupedHeaderTable — 合并表头

DSL 入口：`GroupedHeaderTable<T> { attr { ... } }`

**属性（`MergedHeaderAttr`）**

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `data` | `List<T>` | `emptyList()` | 数据列表 |
| `columns` | `List<TableColumn<T>>` | `emptyList()` | 列定义列表 |
| `headerGroups` | `List<HeaderGroup>` | `emptyList()` | 表头分组定义列表 |
| `groupHeaderHeight` | `Float` | `36f` | 分组表头行高 |
| `groupHeaderBackgroundColor` | `Color` | `Color(0xFFF0F0F0)` | 分组表头背景色 |
| `groupHeaderTextColor` | `Color` | `Color(0xFF333333)` | 分组表头文字颜色 |
| `groupHeaderFontSize` | `Float` | `14f` | 分组表头文字大小 |
| `tableInit` | `(TableView<T>.() -> Unit)?` | `null` | 额外表格配置回调 |

> 无自定义事件，通过 `tableInit` 配置内部表格的事件。

#### 11. TableWithEmptyState — 空状态

DSL 入口：`TableWithEmptyState<T> { attr { ... } }`

**属性（`TableEmptyStateAttr`）**

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `tableInit` | `(TableView<T>.() -> Unit)?` | `null` | 传递给内部 TableView 的初始化回调 |
| `isEmpty` | `Boolean` | `false` | 是否为空状态，由外部根据数据判断设置 |
| `emptyText` | `String` | `"暂无数据"` | 空状态提示文案 |
| `emptyTextColor` | `Color` | `Color(0xFF999999)` | 空状态文案颜色 |
| `emptyTextSize` | `Float` | `14f` | 空状态文案字号 |
| `emptyIcon` | `String?` | `null` | 空状态图标文本（可选），显示在文案上方 |
| `containerHeight` | `Float` | `400f` | 容器高度 |

> 无自定义事件。当 `isEmpty = true` 时显示空状态占位视图，否则显示内部表格。

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
