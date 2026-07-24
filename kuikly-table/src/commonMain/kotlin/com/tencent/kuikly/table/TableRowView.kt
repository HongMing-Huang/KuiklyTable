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

import com.tencent.kuikly.core.base.Border
import com.tencent.kuikly.core.base.BorderStyle
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

/**
 * 表格数据行视图，渲染单行数据。
 *
 * 支持斑马纹背景、选中态高亮、自定义单元格渲染和文本提取。
 * 根据 [TableRowAttr.theme] 适配不同主题样式。
 *
 * 使用方式：
 * ```kotlin
 * TableRow {
 *     attr {
 *         item = student
 *         index = 0
 *         columns = listOf(...)
 *     }
 *     event {
 *         onClick = { /* 行点击 */ }
 *     }
 * }
 * ```
 */
class TableRowView<T> : ComposeView<TableRowAttr<T>, TableRowEvent<T>>() {

    @Suppress("UNCHECKED_CAST")
    override fun createAttr(): TableRowAttr<T> = TableRowAttr()

    @Suppress("UNCHECKED_CAST")
    override fun createEvent(): TableRowEvent<T> = TableRowEvent()

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                flexDirectionRow()
                height(ctx.attr.rowHeight)

                // 背景色：MINIMAL 主题不显示背景色，其他主题按规则显示
                if (ctx.attr.theme != TableTheme.MINIMAL) {
                    val bgColor = when {
                        ctx.attr.isSelected -> ctx.attr.selectedColor
                        ctx.attr.theme == TableTheme.STRIPE && ctx.attr.index % 2 == 1 -> ctx.attr.stripeRowBackgroundColor
                        else -> ctx.attr.rowBackgroundColor
                    }
                    backgroundColor(bgColor)
                }
            }

            for (col in ctx.attr.columns) {
                View {
                    attr {
                        flex(col.flex)
                        if (col.minWidth > 0f) minWidth(col.minWidth)
                        flexDirectionRow()
                        alignItemsCenter()
                        padding(0f, ctx.attr.cellPadding, 0f, ctx.attr.cellPadding)

                        // 对齐方式
                        when (col.align) {
                            TableCellAlign.LEFT -> justifyContentFlexStart()
                            TableCellAlign.CENTER -> justifyContentCenter()
                            TableCellAlign.RIGHT -> justifyContentFlexEnd()
                        }

                        // BORDERED 主题边框
                        if (ctx.attr.theme == TableTheme.BORDERED) {
                            border(Border(ctx.attr.borderWidth, BorderStyle.SOLID, ctx.attr.borderColor))
                        }
                    }

                    // 自定义渲染
                    if (col.cellRenderer != null) {
                        col.cellRenderer.invoke(this, ctx.attr.item)
                    }
                    // 文本渲染
                    else if (col.textExtractor != null) {
                        Text {
                            attr {
                                text(col.textExtractor.invoke(ctx.attr.item))
                                fontSize(ctx.attr.cellFontSize)
                                color(ctx.attr.cellTextColor)

                                when (col.align) {
                                    TableCellAlign.LEFT -> textAlignLeft()
                                    TableCellAlign.CENTER -> textAlignCenter()
                                    TableCellAlign.RIGHT -> textAlignRight()
                                }
                            }
                        }
                    }
                }
            }

            // 事件
            event {
                click { ctx.event.onClick?.invoke() }
                longPress { ctx.event.onLongClick?.invoke() }
            }

            // 底部分隔线（DEFAULT / STRIPE 主题）
            vif({ ctx.attr.theme == TableTheme.DEFAULT || ctx.attr.theme == TableTheme.STRIPE }) {
                View {
                    attr {
                        positionAbsolute()
                        bottom(0f)
                        left(0f)
                        right(0f)
                        height(ctx.attr.separatorHeight)
                        backgroundColor(ctx.attr.separatorColor)
                    }
                }
            }
        }
    }
}

/**
 * 表格数据行属性配置。
 *
 * @param T 数据行类型
 * @property item 当前行数据
 * @property index 行索引（用于斑马纹和选中态判断）
 * @property columns 列定义列表
 * @property rowHeight 行高，默认 48f
 * @property cellPadding 单元格内边距，默认 12f
 * @property cellTextColor 单元格文字颜色
 * @property cellFontSize 单元格文字大小，默认 14f
 * @property rowBackgroundColor 默认行背景色
 * @property stripeRowBackgroundColor 斑马纹偶数行背景色
 * @property selectedColor 选中行背景色
 * @property isSelected 是否选中
 * @property theme 表格主题
 * @property separatorColor 分隔线颜色
 * @property separatorHeight 分隔线高度
 * @property borderColor 边框颜色
 * @property borderWidth 边框宽度
 */
class TableRowAttr<T> : ComposeAttr() {
    /** 当前行数据（必须在使用前赋值）*/
    @Suppress("UNCHECKED_CAST")
    var item: T = null as T

    /** 行索引 */
    var index: Int = 0

    /** 列定义列表 */
    var columns: List<TableColumn<T>> = emptyList()

    /** 行高 */
    var rowHeight: Float = 48f

    /** 单元格内边距 */
    var cellPadding: Float = 12f

    /** 单元格文字颜色 */
    var cellTextColor: Color = Color(0xFF333333)

    /** 单元格文字大小 */
    var cellFontSize: Float = 14f

    /** 默认行背景色 */
    var rowBackgroundColor: Color = Color.WHITE

    /** 斑马纹偶数行背景色 */
    var stripeRowBackgroundColor: Color = Color(0xFFF9F9F9)

    /** 选中行背景色 */
    var selectedColor: Color = Color(0xFFE3F2FD)

    /** 是否选中 */
    var isSelected: Boolean = false

    /** 表格主题 */
    var theme: TableTheme = TableTheme.DEFAULT

    /** 分隔线颜色 */
    var separatorColor: Color = Color(0xFFEEEEEE)

    /** 分隔线高度 */
    var separatorHeight: Float = 0.5f

    /** 边框颜色 */
    var borderColor: Color = Color(0xFFDDDDDD)

    /** 边框宽度 */
    var borderWidth: Float = 0.5f
}

/**
 * 表格数据行事件回调。
 *
 * @param T 数据行类型
 * @property onClick 行点击回调
 * @property onLongClick 行长按回调
 */
class TableRowEvent<T> : ComposeEvent() {
    /** 行点击回调 */
    var onClick: (() -> Unit)? = null

    /** 行长按回调 */
    var onLongClick: (() -> Unit)? = null
}

/**
 * 在 [ViewContainer] 中添加 [TableRowView] 的扩展函数。
 *
 * @param T 数据行类型
 * @param init 初始化回调，用于设置属性和事件
 */
internal fun <T> ViewContainer<*, *>.TableRow(init: TableRowView<T>.() -> Unit) {
    addChild(TableRowView<T>(), init)
}
