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

import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ComposeAttr

/**
 * 表格组件属性配置类。
 *
 * 通过 DSL 风格设置表格的各种属性：
 * ```kotlin
 * Table {
 *     attr {
 *         columns = listOf(
 *             column("姓名", flex = 2f) { it.name },
 *             column("分数", flex = 1f) { it.score.toString() }
 *         )
 *         data = students
 *         theme = TableTheme.BORDERED
 *     }
 * }
 * ```
 */
class TableAttr<T> : ComposeAttr() {

    // === 数据配置 ===

    /** 列定义列表 */
    var columns: List<TableColumn<T>> = emptyList()

    /** 数据列表 */
    var data: List<T> = emptyList()

    /**
     * 稳定行标识提取器。
     *
     * 提供后，行选择基于数据项标识（而非行索引），排序/数据变化时选中状态
     * 会跟随数据项而非位置漂移。未配置时回退为行索引语义。
     */
    var rowKey: ((T) -> Any)? = null

    // === 布局配置 ===

    /** 数据行高度，默认 48f */
    var rowHeight: Float = 48f

    /** 表头高度，默认 44f */
    var headerHeight: Float = 44f

    /** 是否显示表头，默认 true */
    var showHeader: Boolean = true

    /** 表头是否吸顶（固定在顶部不随列表滚动），默认 true */
    var stickyHeader: Boolean = true

    /** 是否启用排序功能，默认 false */
    var sortable: Boolean = false

    /** 是否启用行选择功能，默认 false */
    var selectable: Boolean = false

    /** 单元格内边距（左右），默认 12f */
    var cellPadding: Float = 12f

    /** 表格主题，默认 DEFAULT */
    var theme: TableTheme = TableTheme.DEFAULT

    // === 样式配置 ===

    /** 分隔线颜色 */
    var separatorColor: Color = Color(0xFFEEEEEE)

    /** 分隔线高度，默认 0.5f */
    var separatorHeight: Float = 0.5f

    /** 表头背景色 */
    var headerBackgroundColor: Color = Color(0xFFFAFAFA)

    /** 表头文字颜色 */
    var headerTextColor: Color = Color(0xFF333333)

    /** 表头文字大小 */
    var headerFontSize: Float = 14f

    /** 数据行背景色 */
    var rowBackgroundColor: Color = Color.WHITE

    /** 斑马纹偶数行背景色（STRIPE 主题使用） */
    var stripeRowBackgroundColor: Color = Color(0xFFF9F9F9)

    /** 选中行背景色 */
    var selectedColor: Color = Color(0xFFE3F2FD)

    /** 单元格文字颜色 */
    var cellTextColor: Color = Color(0xFF333333)

    /** 单元格文字大小 */
    var cellFontSize: Float = 14f

    /** 边框颜色（BORDERED 主题使用） */
    var borderColor: Color = Color(0xFFDDDDDD)

    /** 边框宽度 */
    var borderWidth: Float = 0.5f

    /** 排序指示器颜色 */
    var sortIndicatorColor: Color = Color(0xFF4A90D9)

    /** 是否启用横向滚动（当列总宽度超出容器时），默认 false */
    var horizontalScroll: Boolean = false

    /** 表格内容最小宽度（横向滚动时有用），默认 0f 表示自动计算 */
    var minTableWidth: Float = 0f
}
