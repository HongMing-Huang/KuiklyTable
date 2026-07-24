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
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

/**
 * 滑动操作行表格组件，在表格最右侧添加操作按钮列。
 *
 * 通过在 [Table] 的列定义中追加一个"操作"列，使用 [columnRenderer] 渲染
 * 每个 [SwipeAction] 对应的按钮。支持通过 [actionBinder] 将操作绑定到具体数据项。
 *
 * 使用示例：
 * ```kotlin
 * SwipeableTable {
 *     attr {
 *         columns = columns
 *         data = items
 *         swipeActions = listOf(
 *             SwipeAction("删除", Color(0xFFFF4444)) { /* delete */ },
 *             SwipeAction("编辑", Color(0xFF4A90D9)) { /* edit */ }
 *         )
 *         actionBinder = { item, action ->
 *             println("操作 ${action.label} 作用于 $item")
 *         }
 *     }
 * }
 * ```
 *
 * @param T 数据行类型
 *
 * @see SwipeableTableAttr 滑动操作行属性配置
 * @see SwipeAction 滑动操作定义
 */
class SwipeableTableView<T> : ComposeView<SwipeableTableAttr<T>, ComposeEvent>() {

    @Suppress("UNCHECKED_CAST")
    override fun createAttr(): SwipeableTableAttr<T> = SwipeableTableAttr()

    override fun createEvent(): ComposeEvent = ComposeEvent()

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                flex(1f)
            }
            Table<T> {
                attr {
                    // 原始列 + 操作列
                    val allColumns = ctx.attr.columns + listOf(
                        columnRenderer<T>(
                            title = "操作",
                            flex = 0f,
                            key = "__swipe_actions__",
                            minWidth = ctx.attr.swipeActions.size * ctx.attr.actionButtonWidth,
                            align = TableCellAlign.CENTER
                        ) { item ->
                            View {
                                attr {
                                    flexDirectionRow()
                                }
                                for (action in ctx.attr.swipeActions) {
                                    View {
                                        attr {
                                            width(ctx.attr.actionButtonWidth)
                                            height(ctx.attr.rowHeight)
                                            backgroundColor(action.color)
                                            justifyContentCenter()
                                            alignItemsCenter()
                                            flexDirectionRow()
                                        }
                                        event {
                                            click {
                                                if (ctx.attr.actionBinder != null) {
                                                    ctx.attr.actionBinder!!.invoke(item, action)
                                                } else {
                                                    action.onClick()
                                                }
                                            }
                                        }
                                        Text {
                                            attr {
                                                text(action.label)
                                                color(Color(0xFFFFFFFF))
                                                fontSize(12f)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    )
                    columns = allColumns
                    data = ctx.attr.data
                }
                ctx.attr.tableInit?.invoke(this)
            }
        }
    }
}

/**
 * 滑动操作行表格属性配置。
 *
 * @param T 数据行类型
 */
class SwipeableTableAttr<T> : ComposeAttr() {

    /** 数据列表 */
    var data: List<T> = emptyList()

    /** 列定义列表 */
    var columns: List<TableColumn<T>> = emptyList()

    /** 滑动操作列表 */
    var swipeActions: List<SwipeAction> = emptyList()

    /** 每个操作按钮宽度，默认 72f */
    var actionButtonWidth: Float = 72f

    /** 数据行高度（与 Table 保持一致），默认 48f */
    var rowHeight: Float = 48f

    /**
     * 将 SwipeAction 绑定到具体数据项的包装函数。
     *
     * 当设置后，操作按钮点击时会调用此函数并传入当前行数据和操作定义，
     * 替代 [SwipeAction.onClick] 的无参回调。
     */
    var actionBinder: ((T, SwipeAction) -> Unit)? = null

    /** 额外表格配置回调，用于设置 Table 的其他属性或事件 */
    var tableInit: (TableView<T>.() -> Unit)? = null
}

/**
 * 滑动操作行表格 DSL 入口。
 *
 * 在 [ViewContainer] 中添加 [SwipeableTableView]，支持通过 `attr {}` 配置。
 *
 * @param T 数据行类型
 * @param init 初始化回调，用于设置属性
 */
fun <T> ViewContainer<*, *>.SwipeableTable(init: SwipeableTableView<T>.() -> Unit) {
    addChild(SwipeableTableView<T>(), init)
}
