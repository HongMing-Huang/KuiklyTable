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
import com.tencent.kuikly.core.datetime.currentTimestamp
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Input
import com.tencent.kuikly.core.views.View

/**
 * 搜索过滤表格属性配置。
 *
 * 在 [SearchFilterTableView] 的 `attr {}` DSL 中使用：
 * ```kotlin
 * FilterableTable<Student> {
 *     attr {
 *         data = allStudents
 *         columns = studentColumns
 *         searchPlaceholder = "搜索学生姓名或学号..."
 *         filterExtractor = { student, keyword ->
 *             student.name.contains(keyword, ignoreCase = true) ||
 *             student.id.contains(keyword)
 *         }
 *     }
 * }
 * ```
 *
 * @param T 数据行类型
 */
class SearchFilterTableAttr<T> : ComposeAttr() {

    /** 全量数据列表 */
    var data: List<T> = emptyList()

    /** 列定义列表 */
    var columns: List<TableColumn<T>> = emptyList()

    /** 搜索框占位文本，默认 "搜索..." */
    var searchPlaceholder: String = "搜索..."

    /**
     * 自定义匹配逻辑。
     *
     * 接收数据项和搜索关键词，返回 true 表示匹配。
     * 为 null 时自动使用所有列的 [TableColumn.textExtractor] 进行匹配。
     */
    var filterExtractor: ((T, String) -> Boolean)? = null

    /** 防抖延迟毫秒数，在该时间内的连续输入会被合并，仅最后一次生效，默认 300 */
    var debounceMs: Long = 300

    /** 额外表格配置回调 */
    var tableInit: (TableView<T>.() -> Unit)? = null

    /** 搜索栏高度，默认 44f */
    var searchBarHeight: Float = 44f

    /** 搜索栏内边距，默认 8f */
    var searchBarPadding: Float = 8f

    /** 搜索栏背景色 */
    var searchBarBackgroundColor: Color = Color(0xFFF5F5F5)
}

/**
 * 搜索过滤表格事件回调。
 *
 * @param T 数据行类型
 */
class SearchFilterTableEvent<T> : ComposeEvent() {

    /** 搜索关键词变化回调，参数为新关键词 */
    var onKeywordChanged: ((String) -> Unit)? = null

    /** 过滤结果数量变化回调，参数为过滤后的数据量 */
    var onFilterResultChanged: ((Int) -> Unit)? = null

    /**
     * 设置搜索关键词变化回调（DSL 风格）
     *
     * @param handler 回调函数，参数为新关键词
     */
    fun keywordChanged(handler: (String) -> Unit) {
        onKeywordChanged = handler
    }

    /**
     * 设置过滤结果数量变化回调（DSL 风格）
     *
     * @param handler 回调函数，参数为过滤后的数据量
     */
    fun filterResultChanged(handler: (Int) -> Unit) {
        onFilterResultChanged = handler
    }
}

/**
 * 带搜索过滤功能的表格组件。
 *
 * 通过 [ComposeView] 三件套封装，顶部渲染搜索输入栏，下方表格自动根据
 * 关键词过滤显示数据。支持自定义匹配逻辑或自动使用列的 [TableColumn.textExtractor]。
 *
 * 使用示例：
 * ```kotlin
 * FilterableTable<Student> {
 *     attr {
 *         data = allStudents
 *         columns = studentColumns
 *         searchPlaceholder = "搜索姓名..."
 *         filterExtractor = { student, kw ->
 *             student.name.contains(kw, ignoreCase = true)
 *         }
 *     }
 *     event {
 *         keywordChanged { kw -> println("搜索: $kw") }
 *     }
 * }
 * ```
 *
 * @param T 数据行类型
 *
 * @see SearchFilterTableAttr 属性配置
 * @see SearchFilterTableEvent 事件回调
 * @see TableView 内部表格组件
 */
class SearchFilterTableView<T> : ComposeView<SearchFilterTableAttr<T>, SearchFilterTableEvent<T>>() {

    @Suppress("UNCHECKED_CAST")
    override fun createAttr(): SearchFilterTableAttr<T> = SearchFilterTableAttr()

    @Suppress("UNCHECKED_CAST")
    override fun createEvent(): SearchFilterTableEvent<T> = SearchFilterTableEvent()

    // region 响应式状态

    /** 当前搜索关键词 */
    var keyword: String by observable("")

    /** 过滤后的数据列表 */
    var filteredData: List<T> by observable(emptyList())

    /** 上次输入时间戳，用于防抖检测 */
    private var lastInputTime: Long = 0

    /** 输入版本号，每次输入递增，用于判断输入是否稳定 */
    var inputVersion: Int by observable(0)

    // endregion

    // region 生命周期

    /**
     * 组件创建回调，初始化过滤数据。
     */
    override fun created() {
        super.created()
        filteredData = attr.data
    }

    // endregion

    // region 过滤逻辑

    /**
     * 搜索文本变化时调用，更新关键词并触发防抖过滤。
     *
     * 通过时间戳和版本号双重检测实现防抖：
     * - 如果距离上次输入时间小于 [SearchFilterTableAttr.debounceMs]，
     *   仅更新关键词和版本号，不执行过滤
     * - 超过防抖窗口时立即执行过滤
     *
     * @param text 新的搜索文本
     */
    fun onSearchTextChanged(text: String) {
        val now = currentTimestamp()
        val elapsed = now - lastInputTime
        lastInputTime = now
        keyword = text
        inputVersion++
        if (elapsed >= attr.debounceMs) {
            applyFilter()
            event.onKeywordChanged?.invoke(text)
        }
    }

    /**
     * 执行过滤逻辑。
     *
     * 优先使用 [SearchFilterTableAttr.filterExtractor]，
     * 为 null 时遍历所有列的 [TableColumn.textExtractor] 检查是否包含关键词。
     */
    fun applyFilter() {
        val kw = keyword.trim()
        filteredData = if (kw.isEmpty()) {
            attr.data
        } else {
            val extractor = attr.filterExtractor
            if (extractor != null) {
                attr.data.filter { item -> extractor(item, kw) }
            } else {
                // 默认：检查所有列的 textExtractor
                attr.data.filter { item ->
                    attr.columns.any { col ->
                        col.textExtractor?.invoke(item)?.contains(kw, ignoreCase = true) == true
                    }
                }
            }
        }
        event.onFilterResultChanged?.invoke(filteredData.size)
    }

    // endregion

    // region body 布局

    /**
     * 构建搜索过滤表格的视图层级。
     *
     * 结构：
     * 1. 搜索输入栏（固定高度，包含 Input 组件）
     * 2. 表格区域（flex: 1，展示过滤后的数据）
     */
    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                flex(1f)
                flexDirectionColumn()
            }

            // 搜索栏
            View {
                attr {
                    height(ctx.attr.searchBarHeight)
                    padding(ctx.attr.searchBarPadding)
                    backgroundColor(ctx.attr.searchBarBackgroundColor)
                }
                Input {
                    attr {
                        placeholder(ctx.attr.searchPlaceholder)
                    }
                    event {
                        textDidChange { params ->
                            ctx.onSearchTextChanged(params.text)
                        }
                    }
                }
            }

            // 表格区域
            View {
                attr {
                    flex(1f)
                }
                Table<T> {
                    attr {
                        columns = ctx.attr.columns
                        data = ctx.filteredData
                    }
                    ctx.attr.tableInit?.invoke(this)
                }
            }
        }
    }

    // endregion
}

// region DSL 入口

/**
 * 搜索过滤表格组件 DSL 入口。
 *
 * 在 [ViewContainer] 中添加 [SearchFilterTableView]，支持通过 `attr {}` 和 `event {}` DSL 配置。
 *
 * @param T 数据行类型
 * @param init 初始化回调，用于设置属性和事件
 */
fun <T> ViewContainer<*, *>.FilterableTable(init: SearchFilterTableView<T>.() -> Unit) {
    addChild(SearchFilterTableView<T>(), init)
}

// endregion
