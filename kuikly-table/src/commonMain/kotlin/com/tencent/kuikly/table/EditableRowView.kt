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
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Input
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

/**
 * 编辑触发方式枚举。
 */
enum class EditTrigger {
    /** 单击触发编辑 */
    CLICK,
    /** 双击触发编辑 */
    DOUBLE_CLICK,
}

/**
 * 内联编辑行表格组件，支持对指定列进行单元格内联编辑。
 *
 * 对 [EditableTableAttr.editableColumns] 中的列，将单元格渲染器包装为可切换的
 * 显示态/编辑态。点击或双击行（由 [EditTrigger] 控制）进入编辑态后，
 * 显示 Input 输入框及确认/取消按钮。
 *
 * 使用示例：
 * ```kotlin
 * EditableTable {
 *     attr {
 *         columns = columns
 *         data = students
 *         editableColumns = setOf("姓名", "分数")
 *         editTrigger = EditTrigger.CLICK
 *         onSave = { item, index, columnKey, newValue ->
 *             item.copy(name = if (columnKey == "姓名") newValue else item.name)
 *         }
 *     }
 * }
 * ```
 *
 * @param T 数据行类型
 *
 * @see EditableTableAttr 内联编辑行属性配置
 * @see EditableTableEvent 内联编辑行事件回调
 * @see EditTrigger 编辑触发方式
 */
class EditableTableView<T> : ComposeView<EditableTableAttr<T>, EditableTableEvent<T>>() {

    @Suppress("UNCHECKED_CAST")
    override fun createAttr(): EditableTableAttr<T> = EditableTableAttr()

    @Suppress("UNCHECKED_CAST")
    override fun createEvent(): EditableTableEvent<T> = EditableTableEvent()

    // region 响应式状态

    /** 当前编辑的单元格，(rowIndex, columnKey)，null 表示无编辑 */
    var editingCell: Pair<Int, String>? by observable(null)

    /** 当前编辑值 */
    var editingValue: String by observable("")

    // endregion

    /**
     * 开始编辑指定单元格。
     *
     * @param index 行索引
     * @param columnKey 列 key
     * @param currentValue 当前单元格的显示值
     */
    fun startEdit(index: Int, columnKey: String, currentValue: String) {
        editingCell = Pair(index, columnKey)
        editingValue = currentValue
        event.onEditStart?.invoke(index, columnKey)
    }

    /**
     * 保存当前编辑内容。
     *
     * 调用 [EditableTableAttr.onSave] 回调，并结束编辑态。
     *
     * @param item 当前行数据项
     * @param index 行索引
     */
    fun saveEdit(item: T, index: Int) {
        val cell = editingCell ?: return
        val columnKey = cell.second
        attr.onSave?.invoke(item, index, columnKey, editingValue)
        event.onEditEnd?.invoke(index, columnKey)
        editingCell = null
        editingValue = ""
    }

    /**
     * 取消当前编辑，恢复原始值。
     */
    fun cancelEdit() {
        val cell = editingCell
        if (cell != null) {
            event.onEditEnd?.invoke(cell.first, cell.second)
        }
        editingCell = null
        editingValue = ""
    }

    /**
     * 处理行点击事件，根据 [EditTrigger] 触发编辑。
     *
     * @param item 被点击行的数据项
     * @param index 被点击行的索引
     */
    fun onRowClick(item: T, index: Int) {
        if (attr.editTrigger == EditTrigger.CLICK) {
            val firstEditableKey = attr.editableColumns.firstOrNull()
            if (firstEditableKey != null) {
                val col = attr.columns.firstOrNull { it.key == firstEditableKey }
                val currentValue = col?.textExtractor?.invoke(item) ?: ""
                startEdit(index, firstEditableKey, currentValue)
            }
        }
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                flex(1f)
            }
            Table<T> {
                attr {
                    // 包装可编辑列
                    val wrappedColumns = ctx.attr.columns.map { col ->
                        if (col.key in ctx.attr.editableColumns) {
                            col.copy(cellRenderer = { item ->
                                val index = ctx.attr.data.indexOf(item)
                                // 使用 vbind 监听编辑态变化，驱动单元格重渲染
                                vbind({ (ctx.editingCell.hashCode() * 31 + ctx.editingValue.hashCode()).toLong() }) {
                                    val isEditing = ctx.editingCell == Pair(index, col.key)

                                    if (isEditing) {
                                    // 编辑态：Input + 确认 + 取消
                                    View {
                                        attr {
                                            flex(1f)
                                            flexDirectionRow()
                                            alignItemsCenter()
                                        }
                                        Input {
                                            attr {
                                                text(ctx.editingValue)
                                                fontSize(ctx.attr.cellFontSize)
                                                color(ctx.attr.cellTextColor)
                                            }
                                            event {
                                                textDidChange(isSyncEdit = true) { params ->
                                                    ctx.editingValue = params.text
                                                }
                                            }
                                        }
                                        // 确认按钮
                                        Text {
                                            attr {
                                                text("✓")
                                                color(Color(0xFF4CAF50))
                                                fontSize(16f)
                                                marginLeft(4f)
                                            }
                                            event {
                                                click { ctx.saveEdit(item, index) }
                                            }
                                        }
                                        // 取消按钮
                                        Text {
                                            attr {
                                                text("✗")
                                                color(Color(0xFFFF4444))
                                                fontSize(16f)
                                                marginLeft(4f)
                                            }
                                            event {
                                                click { ctx.cancelEdit() }
                                            }
                                        }
                                    }
                                    } else {
                                        // 显示态：原始渲染
                                        if (col.cellRenderer != null) {
                                            col.cellRenderer.invoke(this, item)
                                        } else if (col.textExtractor != null) {
                                            Text {
                                                attr {
                                                    text(col.textExtractor.invoke(item))
                                                    fontSize(ctx.attr.cellFontSize)
                                                    color(ctx.attr.cellTextColor)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            })
                        } else {
                            col
                        }
                    }
                    columns = wrappedColumns
                    data = ctx.attr.data
                }
                event {
                    rowClick { item, index -> ctx.onRowClick(item, index) }
                }
                ctx.attr.tableInit?.invoke(this)
            }
        }
    }
}

/**
 * 内联编辑行表格属性配置。
 *
 * @param T 数据行类型
 */
class EditableTableAttr<T> : ComposeAttr() {

    /** 数据列表 */
    var data: List<T> = emptyList()

    /** 列定义列表 */
    var columns: List<TableColumn<T>> = emptyList()

    /** 可编辑的列 key 集合 */
    var editableColumns: Set<String> = emptySet()

    /** 编辑触发方式，默认双击 */
    var editTrigger: EditTrigger = EditTrigger.DOUBLE_CLICK

    /** 单元格文字颜色 */
    var cellTextColor: Color = Color(0xFF333333)

    /** 单元格文字大小 */
    var cellFontSize: Float = 14f

    /**
     * 保存回调。
     *
     * @param item 当前行数据项
     * @param index 行索引
     * @param columnKey 编辑的列 key
     * @param newValue 新输入的值
     * @return 更新后的数据项
     */
    var onSave: ((item: T, index: Int, columnKey: String, newValue: String) -> T)? = null

    /** 额外表格配置回调 */
    var tableInit: (TableView<T>.() -> Unit)? = null
}

/**
 * 内联编辑行表格事件回调。
 *
 * @param T 数据行类型
 */
class EditableTableEvent<T> : ComposeEvent() {

    /** 开始编辑回调，参数为 (行索引, 列key) */
    var onEditStart: ((Int, String) -> Unit)? = null

    /** 结束编辑回调，参数为 (行索引, 列key) */
    var onEditEnd: ((Int, String) -> Unit)? = null

    /**
     * 设置开始编辑回调（DSL 风格）。
     *
     * @param handler 回调函数，参数为 (行索引, 列key)
     */
    fun editStart(handler: (Int, String) -> Unit) {
        onEditStart = handler
    }

    /**
     * 设置结束编辑回调（DSL 风格）。
     *
     * @param handler 回调函数，参数为 (行索引, 列key)
     */
    fun editEnd(handler: (Int, String) -> Unit) {
        onEditEnd = handler
    }
}

/**
 * 内联编辑行表格 DSL 入口。
 *
 * 在 [ViewContainer] 中添加 [EditableTableView]，支持通过 `attr {}` 和 `event {}` 配置。
 *
 * @param T 数据行类型
 * @param init 初始化回调，用于设置属性和事件
 */
fun <T> ViewContainer<*, *>.EditableTable(init: EditableTableView<T>.() -> Unit) {
    addChild(EditableTableView<T>(), init)
}
