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

import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.directives.vbind
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.List
import com.tencent.kuikly.core.views.Scroller
import com.tencent.kuikly.core.views.View

/**
 * 表格主组件，支持排序、行选择、多主题和虚拟化滚动。
 *
 * 通过 ComposeView 三件套封装，内部组合 [TableHeaderView] 和 [TableRowView] 子组件，
 * 管理响应式状态（排序状态、选中行集合），并提供 DSL 风格 API。
 *
 * 使用示例：
 * ```kotlin
 * Table {
 *     attr {
 *         columns = listOf(
 *             column("姓名", flex = 2f) { it.name },
 *             column("分数", flex = 1f) { it.score.toString() }
 *         )
 *         data = students
 *         theme = TableTheme.BORDERED
 *         sortable = true
 *     }
 *     event {
 *         rowClick { item, index ->
 *             println("点击行 $index: $item")
 *         }
 *         sortChanged { state ->
 *             println("排序: ${state.columnKey}")
 *         }
 *     }
 * }
 * ```
 *
 * @param T 数据行类型
 *
 * @see TableAttr 表格属性配置
 * @see TableEvent 表格事件回调
 * @see TableColumn 表格列定义
 * @see TableTheme 表格主题
 * @see SortState 排序状态
 * @see TableHeaderView 表头子组件
 * @see TableRowView 数据行子组件
 */
class TableView<T> : ComposeView<TableAttr<T>, TableEvent<T>>() {

    @Suppress("UNCHECKED_CAST")
    override fun createAttr(): TableAttr<T> = TableAttr()

    @Suppress("UNCHECKED_CAST")
    override fun createEvent(): TableEvent<T> = TableEvent()

    // region 响应式状态

    /** 当前排序状态，null 表示无排序 */
    var sortState: SortState? by observable(null)

    /** 选中行索引集合 */
    var selectedIndices: Set<Int> by observable(emptySet())

    // endregion

    // region 内部缓存

    /** 排序后的数据缓存 */
    private var sortedDataCache: List<T> = emptyList()

    /** 数据版本标记，用于 vbind 触发重建 */
    private var dataVersion: Int by observable(0)

    /** 上一次缓存时的数据 hashCode，用于检测外部 attr.data 变更 */
    private var lastDataHashCode: Int = 0

    // endregion

    // region 生命周期

    /**
     * 组件创建回调，从 [TableAttr] 读取初始状态。
     */
    override fun created() {
        super.created()
        rebuildSortedData()
    }

    // endregion

    // region 排序逻辑

    /**
     * 切换指定列的排序状态。
     *
     * 排序循环：无排序 → 升序 → 降序 → 无排序。
     * 切换后自动触发 [TableEvent.onSortChanged] 回调（有排序时）。
     *
     * @param columnKey 列的唯一标识
     */
    fun toggleSort(columnKey: String) {
        sortState = when {
            sortState == null -> SortState(columnKey, ascending = true)
            sortState?.columnKey == columnKey && sortState?.ascending == true ->
                SortState(columnKey, ascending = false)
            sortState?.columnKey == columnKey && sortState?.ascending == false ->
                null
            else -> SortState(columnKey, ascending = true)
        }
        rebuildSortedData()
        if (sortState != null) {
            event.onSortChanged?.invoke(sortState!!)
        }
    }

    /**
     * 根据当前 [sortState] 对 [TableAttr.data] 进行排序，更新缓存。
     *
     * 优先使用排序列的 [TableColumn.comparator] 进行数值排序，
     * 若无 comparator 则回退到 [TableColumn.textExtractor] 的字符串排序。
     */
    private fun rebuildSortedData() {
        val data = attr.data
        sortedDataCache = if (sortState != null) {
            val state = sortState!!
            val col = attr.columns.firstOrNull { it.key == state.columnKey }
            when {
                col?.comparator != null -> {
                    val cmp = col.comparator!!
                    if (state.ascending) data.sortedWith(cmp) else data.sortedWith(cmp).reversed()
                }
                col?.textExtractor != null -> {
                    val extractor = col.textExtractor
                    if (state.ascending) {
                        data.sortedBy { extractor.invoke(it) }
                    } else {
                        data.sortedByDescending { extractor.invoke(it) }
                    }
                }
                else -> data
            }
        } else {
            data
        }
        lastDataHashCode = data.hashCode()
        dataVersion++
    }

    /**
     * 返回排序后的数据列表。若外部修改了 [TableAttr.data]，会自动检测并重建缓存。
     *
     * @return 排序后的数据列表
     */
    fun getSortedData(): List<T> {
        if (attr.data.hashCode() != lastDataHashCode) {
            rebuildSortedData()
        }
        return sortedDataCache
    }

    /**
     * 返回用于 vbind 的数据标识键。
     *
     * 当数据或排序状态变化时，该键值随之变化，驱动 UI 重建。
     *
     * @return 数据版本标识
     */
    fun getDataKey(): Int = dataVersion

    // endregion

    // region 选择逻辑

    /**
     * 切换指定行的选中状态。
     *
     * 切换后自动触发 [TableEvent.onSelectionChanged] 回调。
     *
     * @param index 行索引
     */
    fun toggleSelection(index: Int) {
        val newSet = selectedIndices.toMutableSet()
        if (newSet.contains(index)) {
            newSet.remove(index)
        } else {
            newSet.add(index)
        }
        selectedIndices = newSet
        event.onSelectionChanged?.invoke(selectedIndices)
    }

    /**
     * 处理行点击事件，根据配置触发选择或点击回调。
     *
     * @param item 被点击行的数据项
     * @param index 被点击行的索引
     */
    fun handleRowClick(item: T, index: Int) {
        if (attr.selectable) {
            toggleSelection(index)
        }
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

    // endregion

    // region body 布局

    /**
     * 构建表格组件的视图层级。
     *
     * 结构：
     * 1. 吸顶表头（在 List 外面，不随列表滚动）
     * 2. 数据行区域（使用 List 组件实现垂直虚拟化）
     *
     * 当 [TableAttr.horizontalScroll] 为 true 时，表头和数据行会被包裹在一个横向 Scroller 中，
     * 以支持列总宽度超出容器时的横向滚动。
     *
     * 使用 [vbind] 监听 [dataVersion] 变化，在数据或排序变化时重建行列表。
     */
    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                flexDirectionColumn()
                flex(1f)
            }

            // 吸顶表头（在 List 外面，不随列表滚动，不参与横向滚动）
            vif({ ctx.attr.showHeader && ctx.attr.stickyHeader && !ctx.attr.horizontalScroll }) {
                TableHeader {
                    attr {
                        columns = ctx.attr.columns
                        headerHeight = ctx.attr.headerHeight
                        headerBackgroundColor = ctx.attr.headerBackgroundColor
                        headerTextColor = ctx.attr.headerTextColor
                        headerFontSize = ctx.attr.headerFontSize
                        cellPadding = ctx.attr.cellPadding
                        sortState = ctx.sortState
                        sortable = ctx.attr.sortable
                        sortIndicatorColor = ctx.attr.sortIndicatorColor
                        theme = ctx.attr.theme
                        borderColor = ctx.attr.borderColor
                        borderWidth = ctx.attr.borderWidth
                        separatorColor = ctx.attr.separatorColor
                        separatorHeight = ctx.attr.separatorHeight
                    }
                    event {
                        onSortToggle = { key -> ctx.toggleSort(key) }
                    }
                }
            }

            // 横向滚动模式：表头 + List 包裹在横向 Scroller 中
            vif({ ctx.attr.horizontalScroll }) {
                Scroller {
                    attr {
                        flexDirectionRow()
                        flex(1f)
                        if (ctx.attr.minTableWidth > 0) {
                            minWidth(ctx.attr.minTableWidth)
                        }
                    }
                    View {
                        attr {
                            flexDirectionColumn()
                            flex(1f)
                            if (ctx.attr.minTableWidth > 0) {
                                minWidth(ctx.attr.minTableWidth)
                            }
                        }

                        // 吸顶表头（横向滚动时随横向滚动，但不随纵向滚动）
                        vif({ ctx.attr.showHeader && ctx.attr.stickyHeader }) {
                            TableHeader {
                                attr {
                                    columns = ctx.attr.columns
                                    headerHeight = ctx.attr.headerHeight
                                    headerBackgroundColor = ctx.attr.headerBackgroundColor
                                    headerTextColor = ctx.attr.headerTextColor
                                    headerFontSize = ctx.attr.headerFontSize
                                    cellPadding = ctx.attr.cellPadding
                                    sortState = ctx.sortState
                                    sortable = ctx.attr.sortable
                                    sortIndicatorColor = ctx.attr.sortIndicatorColor
                                    theme = ctx.attr.theme
                                    borderColor = ctx.attr.borderColor
                                    borderWidth = ctx.attr.borderWidth
                                    separatorColor = ctx.attr.separatorColor
                                    separatorHeight = ctx.attr.separatorHeight
                                }
                                event {
                                    onSortToggle = { key -> ctx.toggleSort(key) }
                                }
                            }
                        }

                        // 数据行区域
                        List {
                            attr {
                                flex(1f)
                            }

                            // 非吸顶表头
                            vif({ ctx.attr.showHeader && !ctx.attr.stickyHeader }) {
                                TableHeader {
                                    attr {
                                        columns = ctx.attr.columns
                                        headerHeight = ctx.attr.headerHeight
                                        headerBackgroundColor = ctx.attr.headerBackgroundColor
                                        headerTextColor = ctx.attr.headerTextColor
                                        headerFontSize = ctx.attr.headerFontSize
                                        cellPadding = ctx.attr.cellPadding
                                        sortState = ctx.sortState
                                        sortable = ctx.attr.sortable
                                        sortIndicatorColor = ctx.attr.sortIndicatorColor
                                        theme = ctx.attr.theme
                                        borderColor = ctx.attr.borderColor
                                        borderWidth = ctx.attr.borderWidth
                                        separatorColor = ctx.attr.separatorColor
                                        separatorHeight = ctx.attr.separatorHeight
                                    }
                                    event {
                                        onSortToggle = { key -> ctx.toggleSort(key) }
                                    }
                                }
                            }

                            // 数据行
                            vbind({ ctx.dataVersion.toLong() * 1000000007L + ctx.selectedIndices.hashCode().toLong() }) {
                                for ((index, item) in ctx.getSortedData().withIndex()) {
                                    TableRow<T> {
                                        attr {
                                            this.item = item
                                            this.index = index
                                            this.columns = ctx.attr.columns
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

            // 非横向滚动模式：常规布局
            vif({ !ctx.attr.horizontalScroll }) {
                // 数据行区域 - 使用 List 组件实现垂直虚拟化
                List {
                    attr {
                        flex(1f)
                    }

                    // 非吸顶表头（随列表滚动）
                    vif({ ctx.attr.showHeader && !ctx.attr.stickyHeader }) {
                        TableHeader {
                            attr {
                                columns = ctx.attr.columns
                                headerHeight = ctx.attr.headerHeight
                                headerBackgroundColor = ctx.attr.headerBackgroundColor
                                headerTextColor = ctx.attr.headerTextColor
                                headerFontSize = ctx.attr.headerFontSize
                                cellPadding = ctx.attr.cellPadding
                                sortState = ctx.sortState
                                sortable = ctx.attr.sortable
                                sortIndicatorColor = ctx.attr.sortIndicatorColor
                                theme = ctx.attr.theme
                                borderColor = ctx.attr.borderColor
                                borderWidth = ctx.attr.borderWidth
                                separatorColor = ctx.attr.separatorColor
                                separatorHeight = ctx.attr.separatorHeight
                            }
                            event {
                                onSortToggle = { key -> ctx.toggleSort(key) }
                            }
                        }
                    }

                    // 使用 vbind 监听数据和选中状态变化驱动行重建
                    vbind({ ctx.dataVersion.toLong() * 1000000007L + ctx.selectedIndices.hashCode().toLong() }) {
                        for ((index, item) in ctx.getSortedData().withIndex()) {
                            TableRow<T> {
                                attr {
                                    this.item = item
                                    this.index = index
                                    this.columns = ctx.attr.columns
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

    // endregion
}

// region 扩展函数注册

/**
 * 表格组件 DSL 入口。
 *
 * 在 [ViewContainer] 中添加 [TableView]，支持通过 `attr {}` 和 `event {}` DSL 配置。
 *
 * @param T 数据行类型
 * @param init 初始化回调，用于设置属性和事件
 */
fun <T> ViewContainer<*, *>.Table(init: TableView<T>.() -> Unit) {
    addChild(TableView<T>(), init)
}

// endregion
