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

package com.tencent.kuikly.table.demo

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Scroller
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.tencent.kuikly.table.*

// region 数据模型

/** 学生数据模型 */
data class Student(
    val name: String,
    val age: Int,
    val score: Int,
    val grade: String,
)

/** 产品数据模型，用于自定义渲染演示 */
data class Product(
    val name: String,
    val price: Double,
    val stock: Int,
    val status: String, // "在售", "缺货", "下架"
)

/** 订单数据模型，用于横向滚动演示 */
data class OrderRecord(
    val orderId: String,
    val customer: String,
    val product: String,
    val quantity: Int,
    val unitPrice: Double,
    val total: Double,
    val status: String,
)

// endregion

/**
 * 表格组件演示页面。
 *
 * 展示 KuiklyUI 表格组件的核心功能：
 * 1. 基础表格 — 最简单的使用方式
 * 2. 可排序表格 — 点击表头排序
 * 3. 可选择表格 — 多选行
 * 4. 斑马纹主题 — STRIPE 主题
 * 5. 全边框主题 — BORDERED 主题
 * 6. 自定义单元格渲染 — 嵌入自定义组件
 * 7. 横向滚动表格 — 列数较多时支持横向滚动浏览
 * 8. 极简主题 — MINIMAL 主题
 * 9. 大数据量 — 虚拟化滚动性能演示
 */
@Page("TableDemoPage")
class TableDemoPage : Pager() {

    /** 排序状态描述 */
    private var sortInfo: String by observable("未排序")

    /** 选中行信息 */
    private var selectedInfo: String by observable("已选中 0 行")

    override fun body(): ViewBuilder {
        val page = this
        return {
            attr {
                flexDirectionColumn()
                backgroundColor(Color.WHITE)
                flex(1f)
            }

            // 顶部导航栏
            View {
                attr {
                    flexDirectionRow()
                    justifyContentCenter()
                    alignItemsCenter()
                    height(44f + page.pageData.statusBarHeight)
                    paddingTop(page.pageData.statusBarHeight)
                    backgroundColor(Color.WHITE)
                }
                Text {
                    attr {
                        text("表格组件 Demo")
                        fontSize(17f)
                        fontWeightSemiBold()
                        color(Color(0xFF333333))
                    }
                }
            }

            // 内容区域（可滚动）
            Scroller {
                attr { flex(1f) }

                // === Section 1: 基础表格 ===
                page.buildSectionBasic(this)

                page.buildDivider(this)

                // === Section 2: 可排序表格 ===
                page.buildSectionSortable(this)

                page.buildDivider(this)

                // === Section 3: 可选择表格 ===
                page.buildSectionSelectable(this)

                page.buildDivider(this)

                // === Section 4: 斑马纹主题 ===
                page.buildSectionStripe(this)

                page.buildDivider(this)

                // === Section 5: 全边框主题 ===
                page.buildSectionBordered(this)

                page.buildDivider(this)

                // === Section 6: 自定义单元格渲染 ===
                page.buildSectionCustomRenderer(this)

                page.buildDivider(this)

                // === Section 7: 横向滚动表格 ===
                page.buildSectionHorizontalScroll(this)

                page.buildDivider(this)

                // === Section 8: 极简主题 ===
                page.buildSectionMinimal(this)

                page.buildDivider(this)

                // === Section 9: 大数据量 ===
                page.buildSectionLargeData(this)
            }
        }
    }

    // region Section 1: 基础表格

    /**
     * Section 1: 基础表格，演示最简单的表格使用方式。
     */
    private fun buildSectionBasic(container: ViewContainer<*, *>) {
        container.View {
            attr { flexDirectionColumn(); padding(15f) }

            buildSectionTitle(this, "基础表格")
            buildDescription(this, "最简单的表格使用方式")

            val students = listOf(
                Student("张三", 18, 92, "高三"),
                Student("李四", 17, 85, "高二"),
                Student("王五", 16, 78, "高一"),
                Student("赵六", 18, 95, "高三"),
                Student("孙七", 17, 88, "高二"),
                Student("周八", 16, 72, "高一"),
            )

            View {
                attr { height(340f); marginTop(8f) }
                Table<Student> {
                    attr {
                        columns = listOf(
                            column("姓名", flex = 2f) { it.name },
                            column("年龄", flex = 1f, align = TableCellAlign.CENTER) { it.age.toString() },
                            column("成绩", flex = 1f, align = TableCellAlign.CENTER) { it.score.toString() },
                        )
                        data = students
                        theme = TableTheme.DEFAULT
                        showHeader = true
                    }
                }
            }
        }
    }

    // endregion

    // region Section 2: 可排序表格

    /**
     * Section 2: 可排序表格，演示点击表头排序功能。
     */
    private fun buildSectionSortable(container: ViewContainer<*, *>) {
        val page = this
        container.View {
            attr { flexDirectionColumn(); padding(15f) }

            buildSectionTitle(this, "可排序表格")
            buildDescription(this, "点击表头排序")

            val students = listOf(
                Student("张三", 18, 92, "高三"),
                Student("李四", 17, 85, "高二"),
                Student("王五", 16, 78, "高一"),
                Student("赵六", 18, 95, "高三"),
                Student("孙七", 17, 88, "高二"),
                Student("周八", 16, 72, "高一"),
                Student("吴九", 18, 91, "高三"),
                Student("郑十", 17, 83, "高二"),
            )

            View {
                attr { height(430f); marginTop(8f) }
                Table<Student> {
                    attr {
                        columns = listOf(
                            column("姓名", flex = 2f, sortable = true) { it.name },
                            column("年龄", flex = 1f, align = TableCellAlign.CENTER, sortable = true,
                                comparator = { a, b -> a.age.compareTo(b.age) }
                            ) { it.age.toString() },
                            column("成绩", flex = 1f, align = TableCellAlign.CENTER, sortable = true,
                                comparator = { a, b -> a.score.compareTo(b.score) }
                            ) { it.score.toString() },
                        )
                        data = students
                        sortable = true
                        theme = TableTheme.DEFAULT
                    }
                    event {
                        sortChanged { state ->
                            page.sortInfo = "排序: ${state.columnKey} ${if (state.ascending) "升序" else "降序"}"
                        }
                    }
                }
            }

            // 排序状态显示
            buildInfoBox(this, page.sortInfo)
        }
    }

    // endregion

    // region Section 3: 可选择表格

    /**
     * Section 3: 可选择表格，演示多选行功能。
     */
    private fun buildSectionSelectable(container: ViewContainer<*, *>) {
        val page = this
        container.View {
            attr { flexDirectionColumn(); padding(15f) }

            buildSectionTitle(this, "可选择表格")
            buildDescription(this, "多选行")

            val students = listOf(
                Student("张三", 18, 92, "高三"),
                Student("李四", 17, 85, "高二"),
                Student("王五", 16, 78, "高一"),
                Student("赵六", 18, 95, "高三"),
                Student("孙七", 17, 88, "高二"),
                Student("周八", 16, 72, "高一"),
            )

            View {
                attr { height(340f); marginTop(8f) }
                Table<Student> {
                    attr {
                        columns = listOf(
                            column("姓名", flex = 2f) { it.name },
                            column("年级", flex = 1f) { it.grade },
                            column("成绩", flex = 1f, align = TableCellAlign.CENTER) { it.score.toString() },
                        )
                        data = students
                        selectable = true
                        theme = TableTheme.DEFAULT
                    }
                    event {
                        selectionChanged { indices ->
                            page.selectedInfo = "已选中 ${indices.size} 行"
                        }
                    }
                }
            }

            // 选中数量显示
            buildInfoBox(this, page.selectedInfo)
        }
    }

    // endregion

    // region Section 4: 斑马纹主题

    /**
     * Section 4: 斑马纹主题演示。
     */
    private fun buildSectionStripe(container: ViewContainer<*, *>) {
        container.View {
            attr { flexDirectionColumn(); padding(15f) }

            buildSectionTitle(this, "斑马纹主题 (STRIPE)")

            val students = listOf(
                Student("张三", 18, 92, "高三"),
                Student("李四", 17, 85, "高二"),
                Student("王五", 16, 78, "高一"),
                Student("赵六", 18, 95, "高三"),
                Student("孙七", 17, 88, "高二"),
                Student("周八", 16, 72, "高一"),
                Student("吴九", 18, 91, "高三"),
                Student("郑十", 17, 83, "高二"),
            )

            View {
                attr { height(430f); marginTop(8f) }
                Table<Student> {
                    attr {
                        columns = listOf(
                            column("姓名", flex = 2f) { it.name },
                            column("年龄", flex = 1f, align = TableCellAlign.CENTER) { it.age.toString() },
                            column("年级", flex = 1f) { it.grade },
                        )
                        data = students
                        theme = TableTheme.STRIPE
                    }
                }
            }
        }
    }

    // endregion

    // region Section 5: 全边框主题

    /**
     * Section 5: 全边框主题演示。
     */
    private fun buildSectionBordered(container: ViewContainer<*, *>) {
        container.View {
            attr { flexDirectionColumn(); padding(15f) }

            buildSectionTitle(this, "全边框主题 (BORDERED)")

            val students = listOf(
                Student("张三", 18, 92, "高三"),
                Student("李四", 17, 85, "高二"),
                Student("王五", 16, 78, "高一"),
                Student("赵六", 18, 95, "高三"),
                Student("孙七", 17, 88, "高二"),
                Student("周八", 16, 72, "高一"),
            )

            View {
                attr { height(340f); marginTop(8f) }
                Table<Student> {
                    attr {
                        columns = listOf(
                            column("姓名", flex = 2f) { it.name },
                            column("年龄", flex = 1f, align = TableCellAlign.CENTER) { it.age.toString() },
                            column("成绩", flex = 1f, align = TableCellAlign.CENTER) { it.score.toString() },
                            column("年级", flex = 1f) { it.grade },
                        )
                        data = students
                        theme = TableTheme.BORDERED
                    }
                }
            }
        }
    }

    // endregion

    // region Section 6: 自定义单元格渲染

    /**
     * Section 6: 自定义单元格渲染，演示在单元格中嵌入自定义组件。
     */
    private fun buildSectionCustomRenderer(container: ViewContainer<*, *>) {
        container.View {
            attr { flexDirectionColumn(); padding(15f) }

            buildSectionTitle(this, "自定义单元格渲染")
            buildDescription(this, "在单元格中嵌入自定义组件")

            val products = listOf(
                Product("iPhone 15 Pro", 8999.0, 156, "在售"),
                Product("MacBook Air M3", 9499.0, 42, "在售"),
                Product("AirPods Pro 2", 1899.0, 0, "缺货"),
                Product("iPad mini 6", 3799.0, 88, "在售"),
                Product("Apple Watch S9", 2999.0, 0, "下架"),
                Product("HomePod mini", 749.0, 23, "在售"),
            )

            View {
                attr { height(340f); marginTop(8f) }
                Table<Product> {
                    attr {
                        columns = listOf(
                            // 产品名称 — 普通文本
                            column("产品名称", flex = 2f) { it.name },
                            // 价格 — 自定义渲染：红色/绿色文本 + ¥ 前缀
                            columnRenderer<Product>(
                                title = "价格",
                                flex = 1f,
                            ) { product ->
                                Text {
                                    attr {
                                        text("¥${product.price.toInt()}")
                                        fontSize(13f)
                                        color(
                                            if (product.price > 5000) Color(0xFFE53935) else Color(0xFF43A047)
                                        )
                                    }
                                }
                            },
                            // 库存 — 自定义渲染：进度条样式的库存指示器
                            columnRenderer<Product>(
                                title = "库存",
                                flex = 1f,
                            ) { product ->
                                View {
                                    attr {
                                        flexDirectionRow()
                                        alignItemsCenter()
                                    }
                                    // 进度条背景
                                    View {
                                        attr {
                                            flex(1f)
                                            height(6f)
                                            backgroundColor(Color(0xFFEEEEEE))
                                            borderRadius(3f)
                                            marginRight(4f)
                                        }
                                        // 进度条填充
                                        View {
                                            val ratio = (product.stock.coerceAtMost(200).toFloat() / 200f)
                                                .coerceIn(0f, 1f)
                                            attr {
                                                height(6f)
                                                flex(ratio)
                                                borderRadius(3f)
                                                backgroundColor(
                                                    when {
                                                        product.stock == 0 -> Color(0xFFE53935)
                                                        product.stock < 50 -> Color(0xFFFFA726)
                                                        else -> Color(0xFF43A047)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    Text {
                                        attr {
                                            text(product.stock.toString())
                                            fontSize(11f)
                                            color(Color(0xFF999999))
                                        }
                                    }
                                }
                            },
                            // 状态 — 自定义渲染：彩色标签
                            columnRenderer<Product>(
                                title = "状态",
                                flex = 1f,
                            ) { product ->
                                View {
                                    val (bgColor, textColor) = when (product.status) {
                                        "在售" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
                                        "缺货" -> Color(0xFFFFF3E0) to Color(0xFFE65100)
                                        "下架" -> Color(0xFFEEEEEE) to Color(0xFF757575)
                                        else -> Color(0xFFF5F5F5) to Color(0xFF333333)
                                    }
                                    attr {
                                        backgroundColor(bgColor)
                                        borderRadius(4f)
                                        padding(4f, 2f, 4f, 2f)
                                        alignSelfFlexStart()
                                    }
                                    Text {
                                        attr {
                                            text(product.status)
                                            fontSize(12f)
                                            color(textColor)
                                        }
                                    }
                                }
                            },
                        )
                        data = products
                        theme = TableTheme.DEFAULT
                    }
                }
            }
        }
    }

    // endregion

    // region Section 7: 横向滚动表格

    /**
     * Section 7: 横向滚动表格，演示列数较多时支持横向滚动浏览。
     */
    private fun buildSectionHorizontalScroll(container: ViewContainer<*, *>) {
        container.View {
            attr { flexDirectionColumn(); padding(15f) }

            buildSectionTitle(this, "横向滚动表格")
            buildDescription(this, "列数较多时支持横向滚动浏览")

            val orders = listOf(
                OrderRecord("ORD-001", "张三", "MacBook Pro 14", 1, 14999.0, 14999.0, "已发货"),
                OrderRecord("ORD-002", "李四", "iPhone 15 Pro Max", 2, 9999.0, 19998.0, "待付款"),
                OrderRecord("ORD-003", "王五", "AirPods Pro 2", 3, 1899.0, 5697.0, "已完成"),
                OrderRecord("ORD-004", "赵六", "iPad Air M2", 1, 4799.0, 4799.0, "已发货"),
                OrderRecord("ORD-005", "孙七", "Apple Watch Ultra 2", 1, 6499.0, 6499.0, "待发货"),
                OrderRecord("ORD-006", "周八", "Studio Display", 1, 11499.0, 11499.0, "已完成"),
            )

            View {
                attr { height(340f); marginTop(8f) }
                Table<OrderRecord> {
                    attr {
                        columns = listOf(
                            column("订单号", flex = 1f, minWidth = 100f) { it.orderId },
                            column("客户", flex = 1f, minWidth = 80f) { it.customer },
                            column("商品", flex = 2f, minWidth = 160f) { it.product },
                            column("数量", flex = 1f, minWidth = 60f, align = TableCellAlign.CENTER) { it.quantity.toString() },
                            column("单价", flex = 1f, minWidth = 100f, align = TableCellAlign.RIGHT) { "¥${it.unitPrice.toInt()}" },
                            column("总价", flex = 1f, minWidth = 100f, align = TableCellAlign.RIGHT) { "¥${it.total.toInt()}" },
                            column("状态", flex = 1f, minWidth = 80f, align = TableCellAlign.CENTER) { it.status },
                        )
                        data = orders
                        horizontalScroll = true
                        minTableWidth = 800f
                        theme = TableTheme.BORDERED
                        showHeader = true
                    }
                }
            }
        }
    }

    // endregion

    // region Section 8: 极简主题

    /**
     * Section 8: 极简主题，演示 MINIMAL 主题样式。
     *
     * MINIMAL 主题为极简配置：无额外装饰、无背景色、无分隔线、无边框。
     */
    private fun buildSectionMinimal(container: ViewContainer<*, *>) {
        container.View {
            attr { flexDirectionColumn(); padding(15f) }

            buildSectionTitle(this, "极简主题 (MINIMAL)")
            buildDescription(this, "无额外装饰，无背景色，无分隔线")

            val students = listOf(
                Student("张三", 18, 92, "高三"),
                Student("李四", 17, 85, "高二"),
                Student("王五", 16, 78, "高一"),
                Student("赵六", 18, 95, "高三"),
                Student("孙七", 17, 88, "高二"),
            )

            View {
                attr { height(300f); marginTop(8f) }
                Table<Student> {
                    attr {
                        columns = listOf(
                            column("姓名", flex = 2f) { it.name },
                            column("年龄", flex = 1f, align = TableCellAlign.CENTER) { it.age.toString() },
                            column("成绩", flex = 1f, align = TableCellAlign.CENTER) { it.score.toString() },
                            column("年级", flex = 1f) { it.grade },
                        )
                        data = students
                        theme = TableTheme.MINIMAL
                    }
                }
            }
        }
    }

    // endregion

    // region Section 9: 大数据量

    /**
     * Section 9: 大数据量演示，展示虚拟化滚动性能。
     */
    private fun buildSectionLargeData(container: ViewContainer<*, *>) {
        container.View {
            attr { flexDirectionColumn(); padding(15f); marginBottom(30f) }

            buildSectionTitle(this, "大数据量 (100行)")
            buildDescription(this, "展示虚拟化滚动性能")

            val largeData = (1..100).map { i ->
                Triple(i, "项目 #$i", (i * 37) % 1000)
            }

            View {
                attr { height(500f); marginTop(8f) }
                Table<Triple<Int, String, Int>> {
                    attr {
                        columns = listOf(
                            column("ID", flex = 1f) { it.first.toString() },
                            column("名称", flex = 2f) { it.second },
                            column("数值", flex = 1f, align = TableCellAlign.CENTER) { it.third.toString() },
                        )
                        data = largeData
                        theme = TableTheme.STRIPE
                    }
                }
            }
        }
    }

    // endregion

    // region 辅助方法

    /**
     * 构建 Section 标题。
     */
    private fun buildSectionTitle(container: ViewContainer<*, *>, title: String) {
        container.Text {
            attr {
                text(title)
                fontSize(14f)
                fontWeightBold()
                color(Color(0xFF333333))
                marginBottom(4f)
            }
        }
    }

    /**
     * 构建描述文本。
     */
    private fun buildDescription(container: ViewContainer<*, *>, text: String) {
        container.Text {
            attr {
                text(text)
                fontSize(12f)
                color(Color(0xFF999999))
                marginBottom(4f)
            }
        }
    }

    /**
     * 构建灰色分隔线。
     */
    private fun buildDivider(container: ViewContainer<*, *>) {
        container.View {
            attr { height(8f); backgroundColor(Color(0xFFF5F5F5)) }
        }
    }

    /**
     * 构建信息展示框（浅灰背景圆角框）。
     */
    private fun buildInfoBox(container: ViewContainer<*, *>, text: String) {
        container.View {
            attr {
                marginTop(8f)
                padding(12f)
                backgroundColor(Color(0xFFF5F5F5))
                borderRadius(8f)
            }
            Text {
                attr {
                    text(text)
                    fontSize(13f)
                    color(Color(0xFF666666))
                }
            }
        }
    }

    // endregion
}
