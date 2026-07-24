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
 * 表格头部视图，显示列标题和排序指示器。
 *
 * 组件结构：
 * ```
 * | 列1标题 ▲ | 列2标题   | 列3标题 ▼ |
 * ```
 *
 * 使用方式：
 * ```kotlin
 * TableHeader {
 *     attr {
 *         columns = listOf(...)
 *         headerHeight = 44f
 *         sortState = SortState("name", true)
 *     }
 *     event {
 *         onSortToggle = { key -> /* 切换排序 */ }
 *     }
 * }
 * ```
 */
class TableHeaderView : ComposeView<TableHeaderAttr, TableHeaderEvent>() {

    override fun createAttr(): TableHeaderAttr = TableHeaderAttr()

    override fun createEvent(): TableHeaderEvent = TableHeaderEvent()

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                flexDirectionRow()
                // MINIMAL 主题不显示背景色
                if (ctx.attr.theme != TableTheme.MINIMAL) {
                    backgroundColor(ctx.attr.headerBackgroundColor)
                }
                height(ctx.attr.headerHeight)
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

                    // 列标题文本
                    Text {
                        attr {
                            text(col.title)
                            fontSize(ctx.attr.headerFontSize)
                            color(ctx.attr.headerTextColor)
                            fontWeightSemiBold()

                            when (col.align) {
                                TableCellAlign.LEFT -> textAlignLeft()
                                TableCellAlign.CENTER -> textAlignCenter()
                                TableCellAlign.RIGHT -> textAlignRight()
                            }
                        }
                    }

                    // 排序指示器
                    vif({ ctx.attr.sortable && col.sortable }) {
                        Text {
                            attr {
                                val indicator = when {
                                    ctx.attr.sortState?.columnKey == col.key && ctx.attr.sortState?.ascending == true -> " ▲"
                                    ctx.attr.sortState?.columnKey == col.key && ctx.attr.sortState?.ascending == false -> " ▼"
                                    else -> ""
                                }
                                text(indicator)
                                color(ctx.attr.sortIndicatorColor)
                                fontSize(10f)
                                marginLeft(4f)
                            }
                        }
                    }

                    // 点击触发排序
                    if (ctx.attr.sortable && col.sortable) {
                        event {
                            click { ctx.event.onSortToggle?.invoke(col.key) }
                        }
                    }
                }
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
 * 表格头部属性配置。
 *
 * @property columns 列定义列表
 * @property headerHeight 头部高度，默认 44f
 * @property headerBackgroundColor 头部背景色
 * @property headerTextColor 头部文字颜色
 * @property headerFontSize 头部文字大小，默认 14f
 * @property cellPadding 单元格内边距，默认 12f
 * @property sortState 当前排序状态
 * @property sortable 是否启用排序
 * @property sortIndicatorColor 排序指示器颜色
 * @property theme 表格主题
 * @property borderColor 边框颜色（BORDERED 主题）
 * @property borderWidth 边框宽度
 * @property separatorColor 分隔线颜色
 * @property separatorHeight 分隔线高度
 */
class TableHeaderAttr : ComposeAttr() {
    /** 列定义列表 */
    var columns: List<TableColumn<*>> = emptyList()

    /** 头部高度 */
    var headerHeight: Float = 44f

    /** 头部背景色 */
    var headerBackgroundColor: Color = Color(0xFFFAFAFA)

    /** 头部文字颜色 */
    var headerTextColor: Color = Color(0xFF333333)

    /** 头部文字大小 */
    var headerFontSize: Float = 14f

    /** 单元格内边距 */
    var cellPadding: Float = 12f

    /** 当前排序状态 */
    var sortState: SortState? = null

    /** 是否启用排序 */
    var sortable: Boolean = false

    /** 排序指示器颜色 */
    var sortIndicatorColor: Color = Color(0xFF4A90D9)

    /** 表格主题 */
    var theme: TableTheme = TableTheme.DEFAULT

    /** 边框颜色 */
    var borderColor: Color = Color(0xFFDDDDDD)

    /** 边框宽度 */
    var borderWidth: Float = 0.5f

    /** 分隔线颜色 */
    var separatorColor: Color = Color(0xFFEEEEEE)

    /** 分隔线高度 */
    var separatorHeight: Float = 0.5f
}

/**
 * 表格头部事件回调。
 *
 * @property onSortToggle 排序列点击时触发，参数为列 key
 */
class TableHeaderEvent : ComposeEvent() {
    /** 排序列点击回调，参数为列 key */
    var onSortToggle: ((String) -> Unit)? = null
}

/**
 * 在 [ViewContainer] 中添加 [TableHeaderView] 的扩展函数。
 *
 * @param init 初始化回调，用于设置属性和事件
 */
internal fun ViewContainer<*, *>.TableHeader(init: TableHeaderView.() -> Unit) {
    addChild(TableHeaderView(), init)
}
