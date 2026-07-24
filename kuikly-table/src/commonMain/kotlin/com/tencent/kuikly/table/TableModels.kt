/*
 * Tencent is pleased to support the open source community by making KuiklyUI
 * available.
 * Copyright (C) 2025 Tencent. All rights reserved.
 * Licensed under the License of KuiklyUI;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://github.com/Tencent-TDS/KuiklyUI/blob/main/LICENSE
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tencent.kuikly.table

import com.tencent.kuikly.core.base.ViewContainer

/**
 * 表格单元格对齐方式。
 */
enum class TableCellAlign {
    /** 左对齐 */
    LEFT,
    /** 居中对齐 */
    CENTER,
    /** 右对齐 */
    RIGHT,
}

/**
 * 表格预置主题。
 *
 * 提供四种常用表格外观风格，可通过 [TableAttr] 中的样式属性进一步自定义。
 */
enum class TableTheme {
    /** 默认主题：仅底部分隔线 */
    DEFAULT,
    /** 斑马纹主题：奇偶行交替背景色 */
    STRIPE,
    /** 全边框主题：每个单元格都有完整边框 */
    BORDERED,
    /** 极简主题：无边框无分隔线 */
    MINIMAL,
}

/**
 * 表格列定义。
 *
 * 每一列描述了标题、宽度比例、对齐方式、是否可排序以及数据提取/渲染方式。
 * 推荐使用 [column] 或 [columnRenderer] 便捷函数创建实例，而非直接构造。
 *
 * 使用示例：
 * ```kotlin
 * // 文本列
 * val nameCol = TableColumn<Student>(
 *     key = "name",
 *     title = "姓名",
 *     flex = 2f,
 *     textExtractor = { it.name }
 * )
 *
 * // 自定义渲染列
 * val avatarCol = TableColumn<Student>(
 *     key = "avatar",
 *     title = "头像",
 *     flex = 1f,
 *     cellRenderer = { student ->
 *         Image { attr { url(student.avatarUrl) } }
 *     }
 * )
 * ```
 *
 * @param T 数据行类型
 * @param key 列唯一标识，用于排序等功能
 * @param title 列标题文本
 * @param flex 弹性比例，控制列宽分配，默认 1f
 * @param minWidth 最小宽度（像素），0 表示不限制，默认 0f
 * @param align 单元格内容对齐方式，默认 [TableCellAlign.LEFT]
 * @param sortable 是否允许按此列排序，默认 false
 * @param comparator 自定义比较函数，优先于 [textExtractor] 用于排序。适用于数值列等需要按原始值排序的场景
 * @param textExtractor 文本提取函数，将数据项转换为显示文本。与 [cellRenderer] 二选一
 * @param cellRenderer 自定义单元格渲染函数，在单元格容器内自由布局。与 [textExtractor] 二选一
 */
data class TableColumn<T>(
    val key: String,
    val title: String,
    val flex: Float = 1f,
    val minWidth: Float = 0f,
    val align: TableCellAlign = TableCellAlign.LEFT,
    val sortable: Boolean = false,
    val textExtractor: ((T) -> String)? = null,
    val cellRenderer: (ViewContainer<*, *>.(T) -> Unit)? = null,
    val comparator: ((T, T) -> Int)? = null,
)

/**
 * 创建文本列的便捷函数。
 *
 * 通过 [extractor] 将数据项转换为显示文本，适用于纯文本展示场景。
 *
 * 使用示例：
 * ```kotlin
 * val columns = listOf(
 *     column<Student>("姓名", flex = 2f) { it.name },
 *     column<Student>("分数", flex = 1f, align = TableCellAlign.RIGHT) { it.score.toString() },
 *     column<Student>("排名", flex = 1f, sortable = true) { it.rank.toString() }
 * )
 * ```
 *
 * @param T 数据行类型
 * @param title 列标题
 * @param flex 弹性比例，默认 1f
 * @param key 列标识，默认取 title
 * @param minWidth 最小宽度（像素），默认 0f
 * @param align 对齐方式，默认 [TableCellAlign.LEFT]
 * @param sortable 是否可排序，默认 false
 * @param comparator 自定义比较函数，优先于文本提取器用于排序，默认 null
 * @param extractor 文本提取函数，将数据项转为字符串
 * @return 配置好的 [TableColumn] 实例
 */
fun <T> column(
    title: String,
    flex: Float = 1f,
    key: String = title,
    minWidth: Float = 0f,
    align: TableCellAlign = TableCellAlign.LEFT,
    sortable: Boolean = false,
    comparator: ((T, T) -> Int)? = null,
    extractor: (T) -> String,
): TableColumn<T> = TableColumn(
    key = key,
    title = title,
    flex = flex,
    minWidth = minWidth,
    align = align,
    sortable = sortable,
    textExtractor = extractor,
    comparator = comparator,
)

/**
 * 创建自定义渲染列的便捷函数。
 *
 * 通过 [renderer] 在单元格容器内自由布局，适用于需要展示图片、按钮等复杂内容的场景。
 *
 * 使用示例：
 * ```kotlin
 * val columns = listOf(
 *     columnRenderer<Student>("头像", flex = 1f) { student ->
 *         Image { attr { url(student.avatarUrl); size(32f, 32f) } }
 *     },
 *     columnRenderer<Student>("操作", flex = 1f, align = TableCellAlign.CENTER) { student ->
 *         Button { attr { text("编辑") } }
 *     }
 * )
 * ```
 *
 * @param T 数据行类型
 * @param title 列标题
 * @param flex 弹性比例，默认 1f
 * @param key 列标识，默认取 title
 * @param minWidth 最小宽度（像素），默认 0f
 * @param align 对齐方式，默认 [TableCellAlign.LEFT]
 * @param sortable 是否可排序，默认 false
 * @param comparator 自定义比较函数，优先于文本提取器用于排序，默认 null
 * @param renderer 自定义渲染函数，在 [ViewContainer] 内自由布局
 * @return 配置好的 [TableColumn] 实例
 */
fun <T> columnRenderer(
    title: String,
    flex: Float = 1f,
    key: String = title,
    minWidth: Float = 0f,
    align: TableCellAlign = TableCellAlign.LEFT,
    sortable: Boolean = false,
    comparator: ((T, T) -> Int)? = null,
    renderer: ViewContainer<*, *>.(T) -> Unit,
): TableColumn<T> = TableColumn(
    key = key,
    title = title,
    flex = flex,
    minWidth = minWidth,
    align = align,
    sortable = sortable,
    cellRenderer = renderer,
    comparator = comparator,
)

/**
 * 表格排序状态。
 *
 * 记录当前排序列和排序方向，由 [TableView.toggleSort] 自动管理。
 * 当排序状态变化时，通过 [TableEvent.onSortChanged] 回调通知外部。
 *
 * @param columnKey 排序列的 key，对应 [TableColumn.key]
 * @param ascending 是否升序，`true` 为升序，`false` 为降序，默认 true
 */
data class SortState(
    val columnKey: String,
    val ascending: Boolean = true,
)
