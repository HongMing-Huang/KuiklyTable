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
 * 无限滚动表格组件，支持滚动到底部自动加载更多数据。
 *
 * 内部包装 [TableView]，在表格底部显示加载中指示器或"没有更多了"提示。
 * 外部通过 [appendData] 方法追加新数据，通过 [checkAndLoadMore] 方法检测
 * 是否需要触发加载更多。
 *
 * 使用示例：
 * ```kotlin
 * InfiniteTable<Student> {
 *     attr {
 *         columns = listOf(
 *             column("姓名", flex = 2f) { it.name },
 *             column("分数", flex = 1f) { it.score.toString() }
 *         )
 *         data = initialStudents
 *         loadThreshold = 5
 *         hasMore = true
 *     }
 *     event {
 *         onLoadMore = {
 *             // 异步加载数据后调用 appendData
 *             loadNextPage()
 *         }
 *     }
 * }
 * ```
 *
 * @param T 数据行类型
 *
 * @see InfiniteTableAttr 无限滚动表格属性配置
 * @see InfiniteTableEvent 无限滚动表格事件回调
 * @see TableView 基础表格组件
 */
class InfiniteTableView<T> : ComposeView<InfiniteTableAttr<T>, InfiniteTableEvent<T>>() {

    @Suppress("UNCHECKED_CAST")
    override fun createAttr(): InfiniteTableAttr<T> = InfiniteTableAttr()

    @Suppress("UNCHECKED_CAST")
    override fun createEvent(): InfiniteTableEvent<T> = InfiniteTableEvent()

    // region 响应式状态

    /** 累积数据列表（初始为 [InfiniteTableAttr.data]，后续通过 [appendData] 追加） */
    var allData: List<T> by observable(emptyList())

    /** 内部加载状态，控制加载中指示器的显示 */
    var loading: Boolean by observable(false)

    /** 已加载的数据总量，用于跟踪加载进度 */
    var totalLoadedItems: Int by observable(0)

    // endregion

    // region 生命周期

    /**
     * 组件创建回调，从 [InfiniteTableAttr] 读取初始数据。
     */
    override fun created() {
        super.created()
        allData = attr.data
        totalLoadedItems = attr.data.size
        loading = attr.isLoading
    }

    // endregion

    // region 数据操作

    /**
     * 追加新数据到表格。
     *
     * 将 [newItems] 拼接到现有 [allData] 末尾，并自动将 [loading] 置为 false，
     * 同时更新 [totalLoadedItems]。
     *
     * @param newItems 新数据项列表
     */
    fun appendData(newItems: List<T>) {
        allData = allData + newItems
        totalLoadedItems = allData.size
        loading = false
    }

    /**
     * 外部调用的便捷加载方法，追加数据并自动检测是否需要继续加载。
     *
     * 与 [appendData] 类似，但追加后会根据当前数据量与阈值的对比
     * 自动判断是否需要再次触发加载。
     *
     * @param items 新加载的数据项列表
     */
    fun loadMore(items: List<T>) {
        appendData(items)
        // 如果追加后数据量仍较少且还有更多数据，自动再次触发加载
        if (attr.hasMore && !loading && allData.size <= attr.loadThreshold * 2) {
            triggerLoadMore()
        }
    }

    /**
     * 检查是否应该触发加载更多。
     *
     * 当满足以下条件时触发 [InfiniteTableEvent.onLoadMore] 回调：
     * - 当前未在加载中（[loading] == false）
     * - 还有更多数据（[InfiniteTableAttr.hasMore] == true）
     * - 当前可见行索引距末尾不足 [InfiniteTableAttr.loadThreshold] 行
     *
     * 同时提供无参重载版本 [checkAndLoadMore]，基于内部数据量自动判断。
     *
     * @param visibleIndex 当前最后可见行索引
     * @param totalItems 总数据条数
     */
    fun checkAndLoadMore(visibleIndex: Int, totalItems: Int) {
        if (!loading && attr.hasMore && totalItems - visibleIndex <= attr.loadThreshold) {
            loading = true
            event.onLoadMore?.invoke()
        }
    }

    /**
     * 无参版本的加载检测，基于内部 [allData] 数据量自动判断。
     *
     * 当数据量小于 [InfiniteTableAttr.loadThreshold] * 2 且还有更多数据时触发加载。
     * 适用于外部无法获取可见索引的场景。
     */
    fun checkAndLoadMore() {
        if (!loading && attr.hasMore && allData.size <= attr.loadThreshold * 2) {
            loading = true
            event.onLoadMore?.invoke()
        }
    }

    /**
     * 手动触发加载更多。
     *
     * 将 [loading] 置为 true 并触发 [InfiniteTableEvent.onLoadMore] 回调。
     * 适用于外部主动调用场景。
     */
    fun triggerLoadMore() {
        if (!loading && attr.hasMore) {
            loading = true
            event.onLoadMore?.invoke()
        }
    }

    /**
     * 手动设置加载状态。
     *
     * @param isLoading 是否正在加载
     */
    fun setLoading(isLoading: Boolean) {
        loading = isLoading
    }

    // endregion

    // region body 布局

    /**
     * 构建无限滚动表格的视图层级。
     *
     * 结构：
     * 1. 表格主体区域（flex: 1，填充可用空间）
     * 2. 加载中指示器（条件显示：loading && hasMore）
     * 3. 无更多数据提示（条件显示：!hasMore && allData.isNotEmpty()）
     */
    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                flex(1f)
                flexDirectionColumn()
            }

            // 表格主体区域
            View {
                attr {
                    flex(1f)
                }
                Table<T> {
                    attr {
                        columns = ctx.attr.columns
                        data = ctx.allData
                    }
                    // 通过行点击事件估算滚动位置，自动触发加载
                    event {
                        rowClick { _, clickedIndex ->
                            val totalItems = ctx.allData.size
                            ctx.checkAndLoadMore(clickedIndex, totalItems)
                        }
                    }
                    ctx.attr.tableInit?.invoke(this)
                }
            }

            // 加载中指示器
            vif({ ctx.loading && ctx.attr.hasMore }) {
                View {
                    attr {
                        height(48f)
                        justifyContentCenter()
                        alignItemsCenter()
                    }
                    Text {
                        attr {
                            text(ctx.attr.loadingText)
                            color(Color(0xFF999999))
                            fontSize(14f)
                        }
                    }
                }
            }

            // 无更多数据提示
            vif({ !ctx.attr.hasMore && ctx.allData.isNotEmpty() }) {
                View {
                    attr {
                        height(36f)
                        justifyContentCenter()
                        alignItemsCenter()
                    }
                    Text {
                        attr {
                            text(ctx.attr.noMoreText)
                            color(Color(0xFFCCCCCC))
                            fontSize(12f)
                        }
                    }
                }
            }
        }
    }

    // endregion
}

/**
 * 无限滚动表格属性配置类。
 *
 * 通过 DSL 风格设置无限滚动表格的各种属性：
 * ```kotlin
 * InfiniteTable<Student> {
 *     attr {
 *         columns = listOf(
 *             column("姓名", flex = 2f) { it.name },
 *             column("分数", flex = 1f) { it.score.toString() }
 *         )
 *         data = initialStudents
 *         loadThreshold = 5
 *         loadingText = "加载中..."
 *         noMoreText = "没有更多了"
 *         hasMore = true
 *     }
 * }
 * ```
 *
 * @param T 数据行类型
 */
class InfiniteTableAttr<T> : ComposeAttr() {

    /** 初始数据列表 */
    var data: List<T> = emptyList()

    /** 列定义列表 */
    var columns: List<TableColumn<T>> = emptyList()

    /** 距离底部多少行时触发加载，默认 5 */
    var loadThreshold: Int = 5

    /** 加载中提示文案，默认 "加载中..." */
    var loadingText: String = "加载中..."

    /** 无更多数据提示文案，默认 "没有更多了" */
    var noMoreText: String = "没有更多了"

    /** 外部控制加载状态，默认 false */
    var isLoading: Boolean = false

    /** 是否还有更多数据，默认 true */
    var hasMore: Boolean = true

    /** 额外表格配置回调，用于设置 [TableView] 的其他属性 */
    var tableInit: (TableView<T>.() -> Unit)? = null
}

/**
 * 无限滚动表格事件回调类。
 *
 * 通过 DSL 风格注册事件回调：
 * ```kotlin
 * InfiniteTable<Student> {
 *     event {
 *         onLoadMore = {
 *             // 加载更多数据
 *             fetchNextPage()
 *         }
 *     }
 * }
 * ```
 *
 * @param T 数据行类型
 */
class InfiniteTableEvent<T> : ComposeEvent() {

    /** 加载更多回调，当滚动到底部或手动触发时调用 */
    var onLoadMore: (() -> Unit)? = null
}

// region 扩展函数注册

/**
 * 无限滚动表格组件 DSL 入口。
 *
 * 在 [ViewContainer] 中添加 [InfiniteTableView]，支持通过 `attr {}` 和 `event {}` DSL 配置。
 *
 * @param T 数据行类型
 * @param init 初始化回调，用于设置属性和事件
 */
fun <T> ViewContainer<*, *>.InfiniteTable(init: InfiniteTableView<T>.() -> Unit) {
    addChild(InfiniteTableView<T>(), init)
}

// endregion
