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
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.List
import com.tencent.kuikly.core.views.Scroller
import com.tencent.kuikly.core.views.View

// region 属性配置

/**
 * 冻结列表格属性配置类。
 *
 * 通过 DSL 风格设置冻结列表格的各种属性：
 * ```kotlin
 * FrozenColumnTable {
 *     attr {
 *         columns = listOf(
 *             column("ID", key = "id") { it.id.toString() },
 *             column("姓名", flex = 2f) { it.name },
 *             column("分数", flex = 1f) { it.score.toString() }
 *         )
 *         data = students
 *         frozenColumnKeys = listOf("id")
 *         theme = TableTheme.BORDERED
 *     }
 * }
 * ```
 *
 * @param T 数据行类型
 */
class FrozenColumnTableAttr<T> : ComposeAttr() {

    // === 数据配置 ===

    /** 数据列表 */
    var data: List<T> = emptyList()

    /** 全部列定义列表 */
    var columns: List<TableColumn<T>> = emptyList()

    /**
     * 冻结列的 key 列表（左侧冻结）。
     *
     * 匹配 [TableColumn.key]，命中的列将被固定在表格左侧，不随横向滚动移动。
     */
    var frozenColumnKeys: List<String> = emptyList()

    /**
     * 冻结列总宽度（像素）。
     *
     * 设为 0 时自动根据冻结列的 [TableColumn.minWidth]（最小 80f）求和计算。
     */
    var frozenColumnWidth: Float = 0f

    // === 布局配置 ===

    /** 数据行高度（固定行高确保两侧对齐），默认 48f */
    var rowHeight: Float = 48f

    /** 表头高度，默认 44f */
    var headerHeight: Float = 44f

    /** 是否显示表头，默认 true */
    var showHeader: Boolean = true

    /** 表头是否吸顶（固定在顶部不随列表滚动），默认 true */
    var stickyHeader: Boolean = true

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

    /** 冻结列与主列之间的分隔线颜色 */
    var frozenSeparatorColor: Color = Color(0xFFDDDDDD)

    /** 冻结列与主列之间的分隔线宽度 */
    var frozenSeparatorWidth: Float = 1f

    /**
     * 额外配置回调。
     *
     * 可在冻结列表格内部对底层 [TableView] 进行额外配置（高级用法），默认 null。
     */
    var tableInit: (TableView<T>.() -> Unit)? = null
}

// endregion

// region 事件回调

/**
 * 冻结列表格事件回调类。
 *
 * 通过 DSL 风格注册事件回调：
 * ```kotlin
 * FrozenColumnTable {
 *     event {
 *         rowClick { item, index ->
 *             println("点击行 $index: $item")
 *         }
 *     }
 * }
 * ```
 *
 * @param T 数据行类型
 */
class FrozenColumnTableEvent<T> : ComposeEvent() {

    /** 行点击事件回调 */
    var onRowClick: ((T, Int) -> Unit)? = null

    /** 行长按事件回调 */
    var onRowLongClick: ((T, Int) -> Unit)? = null

    /**
     * 设置行点击回调（DSL 风格）
     *
     * @param handler 回调函数，参数为 (数据项, 行索引)
     */
    fun rowClick(handler: (T, Int) -> Unit) {
        onRowClick = handler
    }

    /**
     * 设置行长按回调（DSL 风格）
     *
     * @param handler 回调函数，参数为 (数据项, 行索引)
     */
    fun rowLongClick(handler: (T, Int) -> Unit) {
        onRowLongClick = handler
    }
}

// endregion

// region 主组件

/**
 * 冻结列表格组件，支持将指定列固定在左侧，主列区域可横向滚动。
 *
 * 通过 ComposeView 三件套封装，将表格拆分为冻结列区域和主列区域：
 * - **冻结列区域**（左侧）：固定宽度，不随横向滚动移动
 * - **主列区域**（右侧）：可横向滚动，显示剩余列
 *
 * 使用单一 [List] 组件渲染所有数据行，每行内部用 `flexDirection("row")` 排列
 * 冻结列和主列区域，确保纵向滚动完全同步。
 * 使用固定 [FrozenColumnTableAttr.rowHeight] 确保行高一致、水平对齐。
 *
 * 使用示例：
 * ```kotlin
 * FrozenColumnTable {
 *     attr {
 *         columns = listOf(
 *             column("ID", key = "id", minWidth = 60f) { it.id.toString() },
 *             column("姓名", flex = 2f) { it.name },
 *             column("部门", flex = 1f) { it.department },
 *             column("邮箱", flex = 3f) { it.email },
 *             column("电话", flex = 2f) { it.phone }
 *         )
 *         data = employees
 *         frozenColumnKeys = listOf("id")
 *         theme = TableTheme.STRIPE
 *     }
 *     event {
 *         rowClick { item, index ->
 *             println("点击: ${item.name}")
 *         }
 *     }
 * }
 * ```
 *
 * @param T 数据行类型
 *
 * @see FrozenColumnTableAttr 冻结列表格属性配置
 * @see FrozenColumnTableEvent 冻结列表格事件回调
 * @see TableColumn 表格列定义
 * @see TableTheme 表格主题
 * @see TableHeaderView 表头子组件
 * @see TableRowView 数据行子组件
 */
class FrozenColumnTableView<T> : ComposeView<FrozenColumnTableAttr<T>, FrozenColumnTableEvent<T>>() {

    @Suppress("UNCHECKED_CAST")
    override fun createAttr(): FrozenColumnTableAttr<T> = FrozenColumnTableAttr()

    @Suppress("UNCHECKED_CAST")
    override fun createEvent(): FrozenColumnTableEvent<T> = FrozenColumnTableEvent()

    // region 响应式状态

    /** 选中行索引集合 */
    var selectedIndices: Set<Int> by observable(emptySet())

    /** 数据版本标记，用于 vbind 触发重建 */
    private var dataVersion: Int by observable(0)

    // endregion

    // region 内部方法

    /**
     * 计算冻结列的总宽度。
     *
     * 若 [FrozenColumnTableAttr.frozenColumnWidth] > 0 则使用该值，
     * 否则对冻结列的 [TableColumn.minWidth] 求和（每列最小 80f）。
     *
     * @param frozenColumns 冻结列定义列表
     * @return 冻结列总宽度（像素）
     */
    private fun calcFrozenWidth(frozenColumns: List<TableColumn<T>>): Float {
        return if (attr.frozenColumnWidth > 0f) {
            attr.frozenColumnWidth
        } else {
            frozenColumns.sumOf { col ->
                col.minWidth.coerceAtLeast(80f).toDouble()
            }.toFloat()
        }
    }

    /**
     * 处理行点击事件。
     *
     * @param item 被点击行的数据项
     * @param index 被点击行的索引
     */
    fun handleRowClick(item: T, index: Int) {
        event.onRowClick?.invoke(item, index)
    }

    /**
     * 处理行长按事件。
     *
     * @param item 被长按行的数据项
     * @param index 被长按行的索引
     */
    fun handleRowLongClick(item: T, index: Int) {
        event.onRowLongClick?.invoke(item, index)
    }

    /**
     * 通知数据已变化，触发 UI 重建。
     *
     * 当外部修改了 [FrozenColumnTableAttr.data] 后调用此方法刷新表格。
     */
    fun notifyDataChanged() {
        dataVersion++
    }

    // endregion

    // region body 布局

    /**
     * 构建冻结列表格的视图层级。
     *
     * 结构：
     * ```
     * [根容器 flexDirectionColumn]
     * ├── [吸顶表头区域] (vif stickyHeader)
     * │   ├── 冻结列表头 View(width=frozenWidth) → TableHeader(frozenColumns)
     * │   ├── 分隔线 View(width=frozenSeparatorWidth)
     * │   └── 主列表头 View(flex=1) → Scroller(horizontal) → TableHeader(scrollColumns)
     * └── [单一 List - 确保纵向滚动同步]
     *     ├── [非吸顶表头] (vif !stickyHeader)
     *     └── vbind(dataVersion) → 每行:
     *         └── View(flexDirectionRow)
     *             ├── 冻结列 View(width=frozenWidth) → TableRow(frozenColumns)
     *             ├── 分隔线
     *             └── 主列 View(flex=1) → Scroller(horizontal) → TableRow(scrollColumns)
     * ```
     *
     * 使用单一 [List] 渲染所有行，每行内部用 flexDirectionRow 排列冻结列和主列区域，
     * 确保纵向滚动完全同步。主列区域使用横向 Scroller 支持横向滚动。
     */
    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                flexDirectionColumn()
                flex(1f)
            }

            // 计算冻结列和主列
            val frozenColumns = ctx.attr.columns.filter { it.key in ctx.attr.frozenColumnKeys }
            val scrollColumns = ctx.attr.columns.filter { it.key !in ctx.attr.frozenColumnKeys }
            val frozenWidth = ctx.calcFrozenWidth(frozenColumns)

            // ===== 吸顶表头区域 =====
            vif({ ctx.attr.showHeader && ctx.attr.stickyHeader }) {
                View {
                    attr {
                        flexDirectionRow()
                        height(ctx.attr.headerHeight)
                        flex(0f)
                    }

                    // 冻结列表头
                    View {
                        attr {
                            width(frozenWidth)
                            flex(0f)
                        }
                        TableHeader {
                            attr {
                                columns = frozenColumns
                                headerHeight = ctx.attr.headerHeight
                                headerBackgroundColor = ctx.attr.headerBackgroundColor
                                headerTextColor = ctx.attr.headerTextColor
                                headerFontSize = ctx.attr.headerFontSize
                                cellPadding = ctx.attr.cellPadding
                                theme = ctx.attr.theme
                                borderColor = ctx.attr.borderColor
                                borderWidth = ctx.attr.borderWidth
                                separatorColor = ctx.attr.separatorColor
                                separatorHeight = ctx.attr.separatorHeight
                            }
                        }
                    }

                    // 冻结列与主列之间的分隔线
                    View {
                        attr {
                            width(ctx.attr.frozenSeparatorWidth)
                            flex(0f)
                            backgroundColor(ctx.attr.frozenSeparatorColor)
                        }
                    }

                    // 主列表头（横向可滚动）
                    View {
                        attr {
                            flex(1f)
                        }
                        Scroller {
                            attr {
                                flexDirectionRow()
                                flex(1f)
                            }
                            View {
                                attr {
                                    flexDirectionRow()
                                    flex(1f)
                                }
                                TableHeader {
                                    attr {
                                        columns = scrollColumns
                                        headerHeight = ctx.attr.headerHeight
                                        headerBackgroundColor = ctx.attr.headerBackgroundColor
                                        headerTextColor = ctx.attr.headerTextColor
                                        headerFontSize = ctx.attr.headerFontSize
                                        cellPadding = ctx.attr.cellPadding
                                        theme = ctx.attr.theme
                                        borderColor = ctx.attr.borderColor
                                        borderWidth = ctx.attr.borderWidth
                                        separatorColor = ctx.attr.separatorColor
                                        separatorHeight = ctx.attr.separatorHeight
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ===== 数据行区域 - 单一 List 确保纵向滚动同步 =====
            List {
                attr {
                    flex(1f)
                }

                // 非吸顶表头（随列表滚动）
                vif({ ctx.attr.showHeader && !ctx.attr.stickyHeader }) {
                    View {
                        attr {
                            flexDirectionRow()
                            height(ctx.attr.headerHeight)
                        }
                        // 冻结列表头
                        View {
                            attr {
                                width(frozenWidth)
                                flex(0f)
                            }
                            TableHeader {
                                attr {
                                    columns = frozenColumns
                                    headerHeight = ctx.attr.headerHeight
                                    headerBackgroundColor = ctx.attr.headerBackgroundColor
                                    headerTextColor = ctx.attr.headerTextColor
                                    headerFontSize = ctx.attr.headerFontSize
                                    cellPadding = ctx.attr.cellPadding
                                    theme = ctx.attr.theme
                                    borderColor = ctx.attr.borderColor
                                    borderWidth = ctx.attr.borderWidth
                                    separatorColor = ctx.attr.separatorColor
                                    separatorHeight = ctx.attr.separatorHeight
                                }
                            }
                        }
                        // 分隔线
                        View {
                            attr {
                                width(ctx.attr.frozenSeparatorWidth)
                                flex(0f)
                                backgroundColor(ctx.attr.frozenSeparatorColor)
                            }
                        }
                        // 主列表头
                        View {
                            attr { flex(1f) }
                            Scroller {
                                attr {
                                    flexDirectionRow()
                                    flex(1f)
                                }
                                View {
                                    attr {
                                        flexDirectionRow()
                                        flex(1f)
                                    }
                                    TableHeader {
                                        attr {
                                            columns = scrollColumns
                                            headerHeight = ctx.attr.headerHeight
                                            headerBackgroundColor = ctx.attr.headerBackgroundColor
                                            headerTextColor = ctx.attr.headerTextColor
                                            headerFontSize = ctx.attr.headerFontSize
                                            cellPadding = ctx.attr.cellPadding
                                            theme = ctx.attr.theme
                                            borderColor = ctx.attr.borderColor
                                            borderWidth = ctx.attr.borderWidth
                                            separatorColor = ctx.attr.separatorColor
                                            separatorHeight = ctx.attr.separatorHeight
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 数据行 - 每行包含冻结列 + 主列，确保纵向滚动同步
                vbind({ ctx.dataVersion.toLong() }) {
                    for ((index, item) in ctx.attr.data.withIndex()) {
                        View {
                            attr {
                                height(ctx.attr.rowHeight)
                                flexDirectionRow()
                            }

                            // 冻结列区域
                            View {
                                attr {
                                    width(frozenWidth)
                                    flex(0f)
                                }
                                TableRow<T> {
                                    attr {
                                        this.item = item
                                        this.index = index
                                        this.columns = frozenColumns
                                        rowHeight = ctx.attr.rowHeight
                                        cellPadding = ctx.attr.cellPadding
                                        cellTextColor = ctx.attr.cellTextColor
                                        cellFontSize = ctx.attr.cellFontSize
                                        rowBackgroundColor = ctx.attr.rowBackgroundColor
                                        stripeRowBackgroundColor = ctx.attr.stripeRowBackgroundColor
                                        selectedColor = ctx.attr.selectedColor
                                        isSelected = ctx.selectedIndices.contains(index)
                                        theme = ctx.attr.theme
                                        separatorColor = ctx.attr.separatorColor
                                        separatorHeight = ctx.attr.separatorHeight
                                        borderColor = ctx.attr.borderColor
                                        borderWidth = ctx.attr.borderWidth
                                    }
                                    event {
                                        onClick = { ctx.handleRowClick(item, index) }
                                        onLongClick = { ctx.handleRowLongClick(item, index) }
                                    }
                                }
                            }

                            // 冻结列与主列之间的分隔线
                            View {
                                attr {
                                    width(ctx.attr.frozenSeparatorWidth)
                                    flex(0f)
                                    backgroundColor(ctx.attr.frozenSeparatorColor)
                                }
                            }

                            // 主列区域（横向可滚动）
                            View {
                                attr {
                                    flex(1f)
                                }
                                Scroller {
                                    attr {
                                        flexDirectionRow()
                                        flex(1f)
                                    }
                                    View {
                                        attr {
                                            flexDirectionRow()
                                            flex(1f)
                                        }
                                        TableRow<T> {
                                            attr {
                                                this.item = item
                                                this.index = index
                                                this.columns = scrollColumns
                                                rowHeight = ctx.attr.rowHeight
                                                cellPadding = ctx.attr.cellPadding
                                                cellTextColor = ctx.attr.cellTextColor
                                                cellFontSize = ctx.attr.cellFontSize
                                                rowBackgroundColor = ctx.attr.rowBackgroundColor
                                                stripeRowBackgroundColor = ctx.attr.stripeRowBackgroundColor
                                                selectedColor = ctx.attr.selectedColor
                                                isSelected = ctx.selectedIndices.contains(index)
                                                theme = ctx.attr.theme
                                                separatorColor = ctx.attr.separatorColor
                                                separatorHeight = ctx.attr.separatorHeight
                                                borderColor = ctx.attr.borderColor
                                                borderWidth = ctx.attr.borderWidth
                                            }
                                            event {
                                                onClick = { ctx.handleRowClick(item, index) }
                                                onLongClick = { ctx.handleRowLongClick(item, index) }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // endregion
}

// endregion

// region 扩展函数注册

/**
 * 冻结列表格组件 DSL 入口。
 *
 * 在 [ViewContainer] 中添加 [FrozenColumnTableView]，支持通过 `attr {}` 和 `event {}` DSL 配置。
 *
 * 使用示例：
 * ```kotlin
 * FrozenColumnTable {
 *     attr {
 *         columns = listOf(
 *             column("ID", key = "id", minWidth = 60f) { it.id.toString() },
 *             column("姓名", flex = 2f) { it.name },
 *             column("部门", flex = 1f) { it.department },
 *             column("邮箱", flex = 3f) { it.email }
 *         )
 *         data = employees
 *         frozenColumnKeys = listOf("id")
 *         rowHeight = 48f
 *         theme = TableTheme.STRIPE
 *     }
 *     event {
 *         rowClick { item, index ->
 *             println("点击: ${item.name}")
 *         }
 *     }
 * }
 * ```
 *
 * @param T 数据行类型
 * @param init 初始化回调，用于设置属性和事件
 */
fun <T> ViewContainer<*, *>.FrozenColumnTable(init: FrozenColumnTableView<T>.() -> Unit) {
    addChild(FrozenColumnTableView<T>(), init)
}

// endregion
