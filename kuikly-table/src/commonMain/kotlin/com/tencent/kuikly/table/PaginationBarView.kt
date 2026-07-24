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
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import kotlin.math.max

/**
 * 分页表格属性配置。
 *
 * 在 [PaginatedTableView] 的 `attr {}` DSL 中使用：
 * ```kotlin
 * PaginatedTable<Student> {
 *     attr {
 *         data = allStudents
 *         columns = studentColumns
 *         pageSize = 10
 *         pageSizeOptions = listOf(10, 20, 50)
 *         tableInit = {
 *             attr { theme = TableTheme.STRIPE }
 *         }
 *     }
 *     event {
 *         pageChanged { page -> println("跳转到第 $page 页") }
 *     }
 * }
 * ```
 *
 * @param T 数据行类型
 */
class PaginatedTableAttr<T> : ComposeAttr() {

    /** 全量数据列表 */
    var data: List<T> = emptyList()

    /** 列定义列表 */
    var columns: List<TableColumn<T>> = emptyList()

    /** 每页条数，默认 20 */
    var pageSize: Int = 20

    /** 可选每页条数列表 */
    var pageSizeOptions: List<Int> = listOf(10, 20, 50, 100)

    /** 额外表格配置回调（可覆盖样式等属性） */
    var tableInit: (TableView<T>.() -> Unit)? = null

    // === 继承自 TableAttr 的样式属性 ===

    /** 数据行高度 */
    var rowHeight: Float = 48f

    /** 表头高度 */
    var headerHeight: Float = 44f

    /** 表格主题 */
    var theme: TableTheme = TableTheme.DEFAULT

    /** 分页栏高度，默认 48f */
    var paginationBarHeight: Float = 48f

    /** 分页栏背景色 */
    var paginationBarBackgroundColor: Color = Color(0xFFFAFAFA)

    /** 分页栏文字颜色 */
    var paginationBarTextColor: Color = Color(0xFF333333)

    /** 分页栏按钮禁用颜色 */
    var paginationBarDisabledColor: Color = Color(0xFFCCCCCC)

    /** 分页栏字号 */
    var paginationBarFontSize: Float = 14f
}

/**
 * 分页表格事件回调。
 *
 * 在 [PaginatedTableView] 的 `event {}` DSL 中使用。
 *
 * @param T 数据行类型
 */
class PaginatedTableEvent<T> : ComposeEvent() {

    /** 页码变化回调，参数为新页码 */
    var onPageChanged: ((Int) -> Unit)? = null

    /** 每页条数变化回调，参数为新的每页条数 */
    var onPageSizeChanged: ((Int) -> Unit)? = null

    /**
     * 设置页码变化回调（DSL 风格）
     *
     * @param handler 回调函数，参数为新页码
     */
    fun pageChanged(handler: (Int) -> Unit) {
        onPageChanged = handler
    }

    /**
     * 设置每页条数变化回调（DSL 风格）
     *
     * @param handler 回调函数，参数为新的每页条数
     */
    fun pageSizeChanged(handler: (Int) -> Unit) {
        onPageSizeChanged = handler
    }
}

/**
 * 带分页控件的表格组件。
 *
 * 通过 [ComposeView] 三件套封装，内部对全量数据进行 subList 切片分页，
 * 并在表格下方渲染分页导航栏（上一页 / 页码 / 下一页 / 总数）。
 *
 * 使用示例：
 * ```kotlin
 * PaginatedTable<Student> {
 *     attr {
 *         data = allStudents
 *         columns = studentColumns
 *         pageSize = 10
 *     }
 *     event {
 *         pageChanged { page -> viewModel.onPageChanged(page) }
 *     }
 * }
 * ```
 *
 * @param T 数据行类型
 *
 * @see PaginatedTableAttr 属性配置
 * @see PaginatedTableEvent 事件回调
 * @see TableView 内部表格组件
 */
class PaginatedTableView<T> : ComposeView<PaginatedTableAttr<T>, PaginatedTableEvent<T>>() {

    @Suppress("UNCHECKED_CAST")
    override fun createAttr(): PaginatedTableAttr<T> = PaginatedTableAttr()

    @Suppress("UNCHECKED_CAST")
    override fun createEvent(): PaginatedTableEvent<T> = PaginatedTableEvent()

    // region 响应式状态

    /** 当前页码（从 1 开始） */
    var currentPage: Int by observable(1)

    /** 当前每页条数 */
    var currentPageSize: Int by observable(20)

    // endregion

    // region 生命周期

    /**
     * 组件创建回调，从属性中同步初始每页条数。
     */
    override fun created() {
        super.created()
        currentPageSize = attr.pageSize
    }

    // endregion

    // region 分页逻辑

    /** 总页数，至少为 1 */
    val totalPages: Int
        get() {
            val total = attr.data.size
            return if (total == 0) 1 else max(1, (total + currentPageSize - 1) / currentPageSize)
        }

    /**
     * 返回当前页的数据切片。
     *
     * 根据 [currentPage] 和 [currentPageSize] 对 [PaginatedTableAttr.data] 做 subList。
     *
     * @return 当前页数据列表
     */
    fun getPageData(): List<T> {
        val data = attr.data
        if (data.isEmpty()) return emptyList()
        val start = (currentPage - 1) * currentPageSize
        if (start >= data.size) return emptyList()
        val end = minOf(start + currentPageSize, data.size)
        return data.subList(start, end)
    }

    /**
     * 跳转到上一页。已到首页时无效。
     */
    fun prevPage() {
        if (currentPage > 1) {
            currentPage -= 1
            event.onPageChanged?.invoke(currentPage)
        }
    }

    /**
     * 跳转到下一页。已到末页时无效。
     */
    fun nextPage() {
        if (currentPage < totalPages) {
            currentPage += 1
            event.onPageChanged?.invoke(currentPage)
        }
    }

    /**
     * 跳转到指定页码。
     *
     * @param page 目标页码，自动 clamp 到合法范围
     */
    fun goToPage(page: Int) {
        val target = page.coerceIn(1, totalPages)
        if (target != currentPage) {
            currentPage = target
            event.onPageChanged?.invoke(currentPage)
        }
    }

    // endregion

    // region body 布局

    /**
     * 构建分页表格的视图层级。
     *
     * 结构：
     * 1. 表格区域（flex: 1，占满剩余空间）
     * 2. 分页导航栏（固定高度，包含上一页 / 页码 / 下一页 / 总数）
     */
    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                flex(1f)
                flexDirectionColumn()
            }

            // 表格区域
            View {
                attr {
                    flex(1f)
                }
                Table<T> {
                    attr {
                        columns = ctx.attr.columns
                        data = ctx.getPageData()
                        rowHeight = ctx.attr.rowHeight
                        headerHeight = ctx.attr.headerHeight
                        theme = ctx.attr.theme
                    }
                    ctx.attr.tableInit?.invoke(this)
                }
            }

            // 分页导航栏
            View {
                attr {
                    height(ctx.attr.paginationBarHeight)
                    flexDirectionRow()
                    justifyContentCenter()
                    alignItemsCenter()
                    backgroundColor(ctx.attr.paginationBarBackgroundColor)
                }

                // 上一页按钮
                Text {
                    attr {
                        text("‹ 上一页")
                        color(ctx.attr.paginationBarTextColor)
                        fontSize(ctx.attr.paginationBarFontSize)
                        padding(8f)
                    }
                    event {
                        click { ctx.prevPage() }
                    }
                }

                // 页码指示器
                Text {
                    attr {
                        text("${ctx.currentPage} / ${ctx.totalPages}")
                        color(ctx.attr.paginationBarTextColor)
                        fontSize(ctx.attr.paginationBarFontSize)
                        padding(12f)
                    }
                }

                // 下一页按钮
                Text {
                    attr {
                        text("下一页 ›")
                        color(ctx.attr.paginationBarTextColor)
                        fontSize(ctx.attr.paginationBarFontSize)
                        padding(8f)
                    }
                    event {
                        click { ctx.nextPage() }
                    }
                }

                // 总条数
                Text {
                    attr {
                        text("共 ${ctx.attr.data.size} 条")
                        color(ctx.attr.paginationBarTextColor)
                        fontSize(ctx.attr.paginationBarFontSize)
                        padding(8f)
                    }
                }
            }
        }
    }

    // endregion
}

// region DSL 入口

/**
 * 分页表格组件 DSL 入口。
 *
 * 在 [ViewContainer] 中添加 [PaginatedTableView]，支持通过 `attr {}` 和 `event {}` DSL 配置。
 *
 * @param T 数据行类型
 * @param init 初始化回调，用于设置属性和事件
 */
fun <T> ViewContainer<*, *>.PaginatedTable(init: PaginatedTableView<T>.() -> Unit) {
    addChild(PaginatedTableView<T>(), init)
}

// endregion
