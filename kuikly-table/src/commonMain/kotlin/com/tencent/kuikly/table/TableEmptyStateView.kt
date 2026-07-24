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
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

/**
 * 空状态表格属性配置。
 *
 * 在 [TableEmptyStateView] 的 `attr {}` DSL 中使用：
 * ```kotlin
 * TableWithEmptyState<Student> {
 *     attr {
 *         isEmpty = students.isEmpty()
 *         emptyText = "暂无学生数据"
 *         emptyTextColor = Color(0xFF999999)
 *         emptyTextSize = 16f
 *         emptyIcon = "📋"
 *         containerHeight = 500f
 *         tableInit = {
 *             attr {
 *                 columns = studentColumns
 *                 data = students
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * @param T 数据行类型
 */
class TableEmptyStateAttr<T> : ComposeAttr() {

    /** 传递给内部 [TableView] 的初始化回调，null 时不渲染表格 */
    var tableInit: (TableView<T>.() -> Unit)? = null

    /** 是否为空状态，由外部根据数据判断设置 */
    var isEmpty: Boolean = false

    /** 空状态提示文案，默认 "暂无数据" */
    var emptyText: String = "暂无数据"

    /** 空状态文案颜色，默认灰色 */
    var emptyTextColor: Color = Color(0xFF999999)

    /** 空状态文案字号，默认 14f */
    var emptyTextSize: Float = 14f

    /** 空状态图标文本（可选），显示在文案上方 */
    var emptyIcon: String? = null

    /** 容器高度，默认 400f */
    var containerHeight: Float = 400f
}

/**
 * 空状态表格事件回调。
 *
 * 当前无自定义事件，保留扩展空间。
 *
 * @param T 数据行类型
 */
class TableEmptyStateEvent<T> : ComposeEvent()

/**
 * 带空状态视图的表格包装组件。
 *
 * 通过 [ComposeView] 三件套封装，内部根据 [TableEmptyStateAttr.isEmpty] 条件渲染：
 * - 有数据时显示内部 [TableView]
 * - 无数据时显示空状态占位视图（可选图标 + 文案）
 *
 * 使用示例：
 * ```kotlin
 * TableWithEmptyState<Student> {
 *     attr {
 *         isEmpty = students.isEmpty()
 *         emptyText = "暂无学生数据"
 *         tableInit = {
 *             attr {
 *                 columns = studentColumns
 *                 data = students
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * @param T 数据行类型
 *
 * @see TableEmptyStateAttr 属性配置
 * @see TableEmptyStateEvent 事件回调
 * @see TableView 内部表格组件
 */
class TableEmptyStateView<T> : ComposeView<TableEmptyStateAttr<T>, TableEmptyStateEvent<T>>() {

    @Suppress("UNCHECKED_CAST")
    override fun createAttr(): TableEmptyStateAttr<T> = TableEmptyStateAttr()

    @Suppress("UNCHECKED_CAST")
    override fun createEvent(): TableEmptyStateEvent<T> = TableEmptyStateEvent()

    /**
     * 构建空状态表格的视图层级。
     *
     * 结构：
     * 1. 外层容器（固定高度）
     * 2. 条件渲染：有数据 → [TableView]；无数据 → 空状态占位视图
     */
    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                flex(1f)
                height(ctx.attr.containerHeight)
            }

            // 有数据时显示表格
            vif({ !ctx.attr.isEmpty && ctx.attr.tableInit != null }) {
                Table<T> {
                    ctx.attr.tableInit?.invoke(this)
                }
            }

            // 无数据时显示空状态
            vif({ ctx.attr.isEmpty }) {
                View {
                    attr {
                        flex(1f)
                        justifyContentCenter()
                        alignItemsCenter()
                    }

                    // 可选图标
                    vif({ ctx.attr.emptyIcon != null }) {
                        Text {
                            attr {
                                text(ctx.attr.emptyIcon ?: "")
                                fontSize(ctx.attr.emptyTextSize * 2f)
                            }
                        }
                    }

                    // 空状态文案
                    Text {
                        attr {
                            text(ctx.attr.emptyText)
                            color(ctx.attr.emptyTextColor)
                            fontSize(ctx.attr.emptyTextSize)
                        }
                    }
                }
            }
        }
    }
}

// region DSL 入口

/**
 * 带空状态的表格组件 DSL 入口。
 *
 * 在 [ViewContainer] 中添加 [TableEmptyStateView]，支持通过 `attr {}` 和 `event {}` DSL 配置。
 *
 * @param T 数据行类型
 * @param init 初始化回调，用于设置属性和事件
 */
fun <T> ViewContainer<*, *>.TableWithEmptyState(init: TableEmptyStateView<T>.() -> Unit) {
    addChild(TableEmptyStateView<T>(), init)
}

// endregion
