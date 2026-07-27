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
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

/**
 * 批量操作表格属性配置。
 *
 * 在 [BatchActionTableView] 的 `attr {}` DSL 中使用：
 * ```kotlin
 * BatchActionTable<Student> {
 *     attr {
 *         data = students
 *         columns = studentColumns
 *         batchActions = listOf(
 *             BatchAction("批量删除") { indices -> viewModel.delete(indices) },
 *             BatchAction("批量导出") { indices -> viewModel.export(indices) }
 *         )
 *     }
 *     event {
 *         batchAction { action, indices ->
 *             println("执行 ${action.label}，选中 $indices")
 *         }
 *     }
 * }
 * ```
 *
 * @param T 数据行类型
 */
class BatchActionTableAttr<T> : ComposeAttr() {

    /** 数据列表 */
    var data: List<T> = emptyList()

    /** 列定义列表 */
    var columns: List<TableColumn<T>> = emptyList()

    /** 批量操作列表，定义可选的操作按钮 */
    var batchActions: List<BatchAction> = emptyList()

    /** 额外表格配置回调 */
    var tableInit: (TableView<T>.() -> Unit)? = null

    // === 样式属性 ===

    /** 数据行高度 */
    var rowHeight: Float = 48f

    /** 表格主题 */
    var theme: TableTheme = TableTheme.DEFAULT

    /** 批量操作栏高度，默认 56f */
    var actionBarHeight: Float = 56f

    /** 批量操作栏背景色 */
    var actionBarBackgroundColor: Color = Color(0xFFE3F2FD)

    /** 批量操作栏文字颜色 */
    var actionBarTextColor: Color = Color(0xFF333333)

    /** 操作按钮文字颜色 */
    var actionButtonTextColor: Color = Color(0xFF4A90D9)

    /** 取消按钮文字颜色 */
    var cancelButtonTextColor: Color = Color(0xFF999999)

    /** 操作栏字号 */
    var actionBarFontSize: Float = 14f
}

/**
 * 批量操作表格事件回调。
 *
 * @param T 数据行类型
 */
class BatchActionTableEvent<T> : ComposeEvent() {

    /** 批量操作执行回调，参数为 (操作定义, 选中行索引集合) */
    var onBatchAction: ((BatchAction, Set<Int>) -> Unit)? = null

    /** 选中行变化回调，参数为当前选中行索引集合 */
    var onSelectionChanged: ((Set<Int>) -> Unit)? = null

    /**
     * 设置批量操作执行回调（DSL 风格）
     *
     * @param handler 回调函数，参数为 (操作定义, 选中行索引集合)
     */
    fun batchAction(handler: (BatchAction, Set<Int>) -> Unit) {
        onBatchAction = handler
    }

    /**
     * 设置选中行变化回调（DSL 风格）
     *
     * @param handler 回调函数，参数为选中行索引集合
     */
    fun selectionChanged(handler: (Set<Int>) -> Unit) {
        onSelectionChanged = handler
    }
}

/**
 * 带批量操作栏的表格组件。
 *
 * 通过 [ComposeView] 三件套封装，内部强制启用 [TableView] 的行选择功能。
 * 当有行被选中时，底部滑出批量操作栏，展示已配置的操作按钮和取消按钮。
 *
 * 使用示例：
 * ```kotlin
 * BatchActionTable<Student> {
 *     attr {
 *         data = students
 *         columns = studentColumns
 *         batchActions = listOf(
 *             BatchAction("批量删除") { indices -> viewModel.delete(indices) },
 *             BatchAction("批量导出") { indices -> viewModel.export(indices) }
 *         )
 *     }
 *     event {
 *         batchAction { action, indices ->
 *             println("执行 ${action.label}")
 *         }
 *     }
 * }
 * ```
 *
 * @param T 数据行类型
 *
 * @see BatchActionTableAttr 属性配置
 * @see BatchActionTableEvent 事件回调
 * @see BatchAction 批量操作定义
 * @see TableView 内部表格组件
 */
class BatchActionTableView<T> : ComposeView<BatchActionTableAttr<T>, BatchActionTableEvent<T>>() {

    @Suppress("UNCHECKED_CAST")
    override fun createAttr(): BatchActionTableAttr<T> = BatchActionTableAttr()

    @Suppress("UNCHECKED_CAST")
    override fun createEvent(): BatchActionTableEvent<T> = BatchActionTableEvent()

    // region 响应式状态

    /** 选中行索引集合 */
    var selectedIndices: Set<Int> by observable(emptySet())

    // endregion

    // region 选择逻辑

    /**
     * 处理表格选中行变化事件。
     *
     * 由内部 [TableView] 的 `selectionChanged` 事件触发。
     *
     * @param indices 新的选中行索引集合
     */
    fun onSelectionChanged(indices: Set<Int>) {
        selectedIndices = indices
        event.onSelectionChanged?.invoke(indices)
    }

    /**
     * 清空所有选中行。
     */
    fun clearSelection() {
        selectedIndices = emptySet()
        event.onSelectionChanged?.invoke(emptySet())
    }

    /**
     * 执行批量操作并清空选择。
     *
     * @param action 要执行的批量操作
     */
    fun executeAction(action: BatchAction) {
        val indices = selectedIndices
        action.onClick(indices)
        event.onBatchAction?.invoke(action, indices)
        clearSelection()
    }

    // endregion

    // region body 布局

    /**
     * 构建批量操作表格的视图层级。
     *
     * 结构：
     * 1. 表格区域（flex: 1，强制启用选择模式）
     * 2. 批量操作栏（条件渲染：有选中行时显示）
     */
    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                flex(1f)
                flexDirectionColumn()
            }

            // 表格区域（强制启用选择）
            View {
                attr {
                    flex(1f)
                }
                Table<T> {
                    attr {
                        columns = ctx.attr.columns
                        data = ctx.attr.data
                        selectable = true  // 强制启用行选择
                        rowHeight = ctx.attr.rowHeight
                        theme = ctx.attr.theme
                    }
                    event {
                        selectionChanged { indices ->
                            ctx.onSelectionChanged(indices)
                        }
                    }
                    ctx.attr.tableInit?.invoke(this)
                }
            }

            // 批量操作栏（有选中时显示）
            vif({ ctx.selectedIndices.isNotEmpty() }) {
                View {
                    attr {
                        height(ctx.attr.actionBarHeight)
                        flexDirectionRow()
                        alignItemsCenter()
                        padding(8f)
                        backgroundColor(ctx.attr.actionBarBackgroundColor)
                    }

                    // 选中数量提示
                    View {
                        attr {
                            padding(8f)
                        }
                        Text {
                            attr {
                                text("已选 ${ctx.selectedIndices.size} 项")
                                color(ctx.attr.actionBarTextColor)
                                fontSize(ctx.attr.actionBarFontSize)
                            }
                        }
                    }

                    // 批量操作按钮
                    for (action in ctx.attr.batchActions) {
                        View {
                            attr {
                                padding(8f)
                            }
                            event {
                                click { ctx.executeAction(action) }
                            }
                            Text {
                                attr {
                                    text(action.label)
                                    color(ctx.attr.actionButtonTextColor)
                                    fontSize(ctx.attr.actionBarFontSize)
                                }
                            }
                        }
                    }

                    // 取消选择按钮
                    View {
                        attr {
                            padding(8f)
                        }
                        event {
                            click { ctx.clearSelection() }
                        }
                        Text {
                            attr {
                                text("取消")
                                color(ctx.attr.cancelButtonTextColor)
                                fontSize(ctx.attr.actionBarFontSize)
                            }
                        }
                    }
                }
            }
        }
    }

    // endregion
}

// region DSL 入口

/**
 * 批量操作表格组件 DSL 入口。
 *
 * 在 [ViewContainer] 中添加 [BatchActionTableView]，支持通过 `attr {}` 和 `event {}` DSL 配置。
 *
 * @param T 数据行类型
 * @param init 初始化回调，用于设置属性和事件
 */
fun <T> ViewContainer<*, *>.BatchActionTable(init: BatchActionTableView<T>.() -> Unit) {
    addChild(BatchActionTableView<T>(), init)
}

// endregion
