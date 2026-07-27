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
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.directives.vbind
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.List
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

/**
 * 展开行数据项，用于展平后的数据列表。
 *
 * 使用 [isExpandedRow] 标记区分普通数据行和展开详情行。
 *
 * @param T 数据行类型
 * @param data 原始数据项
 * @param originalIndex 原始数据索引
 * @param isExpandedRow 是否为展开详情行，false 表示普通数据行
 */
data class ExpandableItem<T>(
    val data: T,
    val originalIndex: Int,
    val isExpandedRow: Boolean = false,
)

/**
 * 展开/折叠行表格组件，支持点击展开图标显示行的详细内容。
 *
 * 在表格最左侧添加一个展开/折叠图标列，点击后在对应行下方插入展开详情行。
 * 展开内容通过 [ExpandableTableAttr.expandRenderer] 自定义渲染。
 *
 * 内部使用展平策略：将原始数据与展开详情行交替排列，使用 [List] 组件渲染。
 *
 * 使用示例：
 * ```kotlin
 * ExpandableTable {
 *     attr {
 *         columns = columns
 *         data = orders
 *         expandRenderer = { order ->
 *             Text { attr { text("订单详情: ${order.id}") } }
 *         }
 *     }
 *     event {
 *         expandChanged { index, expanded ->
 *             println("行 $index ${if (expanded) "展开" else "折叠"}")
 *         }
 *     }
 * }
 * ```
 *
 * @param T 数据行类型
 *
 * @see ExpandableTableAttr 展开行属性配置
 * @see ExpandableTableEvent 展开行事件回调
 * @see ExpandableItem 展平后的数据项
 */
class ExpandableTableView<T> : ComposeView<ExpandableTableAttr<T>, ExpandableTableEvent<T>>() {

    @Suppress("UNCHECKED_CAST")
    override fun createAttr(): ExpandableTableAttr<T> = ExpandableTableAttr()

    @Suppress("UNCHECKED_CAST")
    override fun createEvent(): ExpandableTableEvent<T> = ExpandableTableEvent()

    // region 响应式状态

    /** 展开的行索引集合 */
    var expandedIndices: Set<Int> by observable(emptySet())

    /** 数据版本标记，用于 vbind 触发重建 */
    private var dataVersion: Int by observable(0)

    // endregion

    /**
     * 切换指定行的展开/折叠状态。
     *
     * @param index 原始数据行索引
     */
    fun toggleExpand(index: Int) {
        expandedIndices = if (index in expandedIndices) {
            expandedIndices - index
        } else {
            expandedIndices + index
        }
        dataVersion++
        event.onExpandChanged?.invoke(index, index in expandedIndices)
    }

    /**
     * 构建展平后的数据列表。
     *
     * 在展开行的后面插入展开详情行，形成交替排列的展平列表。
     *
     * @return 展平后的 [ExpandableItem] 列表
     */
    fun getFlattenedData(): List<ExpandableItem<T>> {
        val result = mutableListOf<ExpandableItem<T>>()
        for ((index, item) in attr.data.withIndex()) {
            result.add(ExpandableItem(data = item, originalIndex = index, isExpandedRow = false))
            if (index in expandedIndices) {
                result.add(ExpandableItem(data = item, originalIndex = index, isExpandedRow = true))
            }
        }
        return result
    }

    /**
     * 构建包含展开图标列的完整列定义列表。
     *
     * 在原始列前面添加一个固定宽度的展开/折叠图标列。
     *
     * @return 含展开图标列的列定义列表
     */
    fun getAllColumns(): List<TableColumn<T>> {
        val expandCol = columnRenderer<T>(
            title = "",
            flex = 0f,
            key = "__expand_icon__",
            minWidth = attr.expandColumnWidth,
            align = TableCellAlign.CENTER
        ) { _ ->
            // 图标渲染在行级别处理，此处为空占位
        }
        return listOf(expandCol) + attr.columns
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                flex(1f)
                flexDirectionColumn()
            }

            // 表头（含展开图标列占位）
            TableHeader {
                attr {
                    columns = ctx.getAllColumns()
                    headerHeight = 44f
                    headerBackgroundColor = Color(0xFFFAFAFA)
                    headerTextColor = Color(0xFF333333)
                    headerFontSize = 14f
                    cellPadding = 12f
                }
            }

            // 数据区域 - 使用 List 虚拟化
            List {
                attr {
                    flex(1f)
                }

                vbind({ ctx.dataVersion.toLong() }) {
                    for (item in ctx.getFlattenedData()) {
                        if (!item.isExpandedRow) {
                            // 普通数据行
                            View {
                                attr {
                                    flexDirectionRow()
                                    height(48f)
                                    backgroundColor(
                                        if (item.originalIndex in ctx.expandedIndices)
                                            Color(0xFFF5F5F5)
                                        else
                                            Color.WHITE
                                    )
                                }

                                // 展开/折叠图标
                                View {
                                    attr {
                                        width(ctx.attr.expandColumnWidth)
                                        justifyContentCenter()
                                        alignItemsCenter()
                                        flexDirectionRow()
                                    }
                                    event {
                                        click { ctx.toggleExpand(item.originalIndex) }
                                    }
                                    Text {
                                        attr {
                                            text(
                                                if (item.originalIndex in ctx.expandedIndices)
                                                    ctx.attr.collapseIcon
                                                else
                                                    ctx.attr.expandIcon
                                            )
                                            fontSize(12f)
                                            color(Color(0xFF666666))
                                        }
                                    }
                                }

                                // 数据列
                                for (col in ctx.attr.columns) {
                                    View {
                                        attr {
                                            flex(col.flex)
                                            if (col.minWidth > 0f) minWidth(col.minWidth)
                                            flexDirectionRow()
                                            alignItemsCenter()
                                            padding(0f, 12f, 0f, 12f)

                                            when (col.align) {
                                                TableCellAlign.LEFT -> justifyContentFlexStart()
                                                TableCellAlign.CENTER -> justifyContentCenter()
                                                TableCellAlign.RIGHT -> justifyContentFlexEnd()
                                            }
                                        }

                                        if (col.cellRenderer != null) {
                                            col.cellRenderer.invoke(this, item.data)
                                        } else if (col.textExtractor != null) {
                                            Text {
                                                attr {
                                                    text(col.textExtractor.invoke(item.data))
                                                    fontSize(14f)
                                                    color(Color(0xFF333333))
                                                }
                                            }
                                        }
                                    }
                                }

                                // 行点击事件
                                event {
                                    click {
                                        ctx.event.onRowClick?.invoke(item.data, item.originalIndex)
                                    }
                                }
                            }
                        } else {
                            // 展开详情行
                            View {
                                attr {
                                    flexDirectionRow()
                                    backgroundColor(Color(0xFFFAFAFA))
                                    if (ctx.attr.expandRowHeight > 0f) {
                                        height(ctx.attr.expandRowHeight)
                                    }
                                }

                                // 展开图标列占位
                                View {
                                    attr {
                                        width(ctx.attr.expandColumnWidth)
                                    }
                                }

                                // 展开内容区域
                                View {
                                    attr {
                                        flex(1f)
                                        padding(8f, 12f, 8f, 12f)
                                    }
                                    ctx.attr.expandRenderer?.invoke(this, item.data)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 展开/折叠行表格属性配置。
 *
 * @param T 数据行类型
 */
class ExpandableTableAttr<T> : ComposeAttr() {

    /** 数据列表 */
    var data: List<T> = emptyList()

    /** 列定义列表 */
    var columns: List<TableColumn<T>> = emptyList()

    /**
     * 展开内容渲染器。
     *
     * 在 [ViewContainer] 中自定义渲染展开行的详细内容。
     */
    var expandRenderer: (ViewContainer<*, *>.(T) -> Unit)? = null

    /** 展开/折叠图标列宽度，默认 32f */
    var expandColumnWidth: Float = 32f

    /** 折叠态图标文本，默认 "▶" */
    var expandIcon: String = "▶"

    /** 展开态图标文本，默认 "▼" */
    var collapseIcon: String = "▼"

    /** 展开行高度，0 表示自动高度，默认 0f */
    var expandRowHeight: Float = 0f
}

/**
 * 展开/折叠行表格事件回调。
 *
 * @param T 数据行类型
 */
class ExpandableTableEvent<T> : ComposeEvent() {

    /** 展开状态变化回调，参数为 (行索引, 是否展开) */
    var onExpandChanged: ((Int, Boolean) -> Unit)? = null

    /** 行点击回调，参数为 (数据项, 行索引) */
    var onRowClick: ((T, Int) -> Unit)? = null

    /**
     * 设置展开状态变化回调（DSL 风格）。
     *
     * @param handler 回调函数，参数为 (行索引, 是否展开)
     */
    fun expandChanged(handler: (Int, Boolean) -> Unit) {
        onExpandChanged = handler
    }

    /**
     * 设置行点击回调（DSL 风格）。
     *
     * @param handler 回调函数，参数为 (数据项, 行索引)
     */
    fun rowClick(handler: (T, Int) -> Unit) {
        onRowClick = handler
    }
}

/**
 * 展开/折叠行表格 DSL 入口。
 *
 * 在 [ViewContainer] 中添加 [ExpandableTableView]，支持通过 `attr {}` 和 `event {}` 配置。
 *
 * @param T 数据行类型
 * @param init 初始化回调，用于设置属性和事件
 */
fun <T> ViewContainer<*, *>.ExpandableTable(init: ExpandableTableView<T>.() -> Unit) {
    addChild(ExpandableTableView<T>(), init)
}
