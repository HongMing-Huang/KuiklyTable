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
 * 合并表头表格组件，在标准表格上方渲染一层分组表头。
 *
 * 分组表头的每个单元格根据 [HeaderGroup.childColumnKeys] 计算 flex 总和，
 * 实现跨列合并效果。下方是正常的 [TableView] 组件（含原始列头）。
 *
 * 使用示例：
 * ```kotlin
 * GroupedHeaderTable {
 *     attr {
 *         columns = allColumns
 *         data = students
 *         headerGroups = listOf(
 *             HeaderGroup("基本信息", listOf("姓名", "年龄")),
 *             HeaderGroup("成绩", listOf("语文", "数学"))
 *         )
 *     }
 * }
 * ```
 *
 * @param T 数据行类型
 *
 * @see MergedHeaderAttr 合并表头属性配置
 * @see HeaderGroup 表头分组定义
 */
class MergedHeaderView<T> : ComposeView<MergedHeaderAttr<T>, ComposeEvent>() {

    @Suppress("UNCHECKED_CAST")
    override fun createAttr(): MergedHeaderAttr<T> = MergedHeaderAttr()

    override fun createEvent(): ComposeEvent = ComposeEvent()

    /**
     * 计算分组的总 flex 值。
     *
     * 将 [HeaderGroup.childColumnKeys] 中每个 key 对应的列 flex 求和，
     * 若找不到对应列则默认按 1f 计算。
     *
     * @param group 表头分组定义
     * @return 分组的总 flex 值
     */
    private fun calculateGroupFlex(group: HeaderGroup): Float {
        return group.childColumnKeys.sumOf { key ->
            (attr.columns.firstOrNull { it.key == key }?.flex ?: 1f).toDouble()
        }.toFloat()
    }

    /**
     * 计算分组的总 minWidth 值。
     *
     * 将 [HeaderGroup.childColumnKeys] 中每个 key 对应的列 minWidth 求和。
     *
     * @param group 表头分组定义
     * @return 分组的总 minWidth 值
     */
    private fun calculateGroupMinWidth(group: HeaderGroup): Float {
        return group.childColumnKeys.sumOf { key ->
            (attr.columns.firstOrNull { it.key == key }?.minWidth ?: 0f).toDouble()
        }.toFloat()
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                flex(1f)
                flexDirectionColumn()
            }

            // 分组表头行
            View {
                attr {
                    height(ctx.attr.groupHeaderHeight)
                    flexDirectionRow()
                }
                for (group in ctx.attr.headerGroups) {
                    View {
                        val groupFlex = ctx.calculateGroupFlex(group)
                        val groupMinWidth = ctx.calculateGroupMinWidth(group)
                        attr {
                            flex(groupFlex)
                            if (groupMinWidth > 0f) minWidth(groupMinWidth)
                            backgroundColor(ctx.attr.groupHeaderBackgroundColor)
                            justifyContentCenter()
                            alignItemsCenter()
                            flexDirectionRow()
                        }
                        Text {
                            attr {
                                text(group.title)
                                color(ctx.attr.groupHeaderTextColor)
                                fontSize(ctx.attr.groupHeaderFontSize)
                                fontWeightSemiBold()
                            }
                        }
                    }
                }
            }

            // 分隔线
            View {
                attr {
                    height(0.5f)
                    backgroundColor(Color(0xFFDDDDDD))
                }
            }

            // 正常表格
            View {
                attr {
                    flex(1f)
                }
                Table<T> {
                    attr {
                        columns = ctx.attr.columns
                        data = ctx.attr.data
                    }
                    ctx.attr.tableInit?.invoke(this)
                }
            }
        }
    }
}

/**
 * 合并表头表格属性配置。
 *
 * @param T 数据行类型
 */
class MergedHeaderAttr<T> : ComposeAttr() {

    /** 数据列表 */
    var data: List<T> = emptyList()

    /** 列定义列表 */
    var columns: List<TableColumn<T>> = emptyList()

    /** 表头分组定义列表 */
    var headerGroups: List<HeaderGroup> = emptyList()

    /** 分组表头行高，默认 36f */
    var groupHeaderHeight: Float = 36f

    /** 分组表头背景色，默认浅灰 */
    var groupHeaderBackgroundColor: Color = Color(0xFFF0F0F0)

    /** 分组表头文字颜色，默认深灰 */
    var groupHeaderTextColor: Color = Color(0xFF333333)

    /** 分组表头文字大小，默认 14f */
    var groupHeaderFontSize: Float = 14f

    /** 额外表格配置回调，用于设置 Table 的其他属性或事件 */
    var tableInit: (TableView<T>.() -> Unit)? = null
}

/**
 * 合并表头表格 DSL 入口。
 *
 * 在 [ViewContainer] 中添加 [MergedHeaderView]，支持通过 `attr {}` 配置。
 *
 * @param T 数据行类型
 * @param init 初始化回调，用于设置属性
 */
fun <T> ViewContainer<*, *>.GroupedHeaderTable(init: MergedHeaderView<T>.() -> Unit) {
    addChild(MergedHeaderView<T>(), init)
}
