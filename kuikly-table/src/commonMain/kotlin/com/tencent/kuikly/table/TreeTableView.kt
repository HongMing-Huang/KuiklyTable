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
import com.tencent.kuikly.core.views.List
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.tencent.kuikly.table.pipeline.TreeFlattenPipeline

/**
 * 展平后的树节点，包含深度和展开状态信息，供 [TreeTableView] 内部渲染使用。
 *
 * @param T 原始数据类型
 * @param node 原始 [TreeNode] 节点
 * @param depth 节点在树中的深度（0 表示根节点层）
 * @param hasChildren 是否有子节点
 * @param isExpanded 当前是否展开
 */
data class FlatTreeNode<T>(
    val node: TreeNode<T>,
    val depth: Int,
    val hasChildren: Boolean,
    val isExpanded: Boolean,
)

/**
 * 树形表格组件，以树形结构展示层级数据，支持节点的展开/折叠操作。
 *
 * 将 [TreeNode] 树形数据展平为线性列表，在第一列添加缩进和展开/折叠图标，
 * 复用 [TableHeader] 渲染表头，使用 [List] 组件实现虚拟化滚动。
 *
 * 使用示例：
 * ```kotlin
 * TreeTable<FileItem> {
 *     attr {
 *         columns = listOf(
 *             column("名称", flex = 3f) { it.name },
 *             column("大小", flex = 1f) { it.sizeStr }
 *         )
 *         treeData = listOf(
 *             TreeNode(FileItem("src", ""), children = listOf(
 *                 TreeNode(FileItem("main.kt", "2KB"), id = "main"),
 *             ), id = "src"),
 *         )
 *         initialExpandedIds = setOf("src")
 *     }
 *     event {
 *         nodeExpand { node, expanded ->
 *             println("${node.data.name} ${if (expanded) "展开" else "折叠"}")
 *         }
 *         nodeClick { node, index ->
 *             println("点击节点: ${node.data.name}")
 *         }
 *     }
 * }
 * ```
 *
 * @param T 数据行类型
 *
 * @see TreeTableAttr 树形表格属性配置
 * @see TreeTableEvent 树形表格事件回调
 * @see TreeNode 树节点数据模型
 * @see FlatTreeNode 展平后的树节点
 * @see TableHeaderView 表头子组件
 */
class TreeTableView<T> : ComposeView<TreeTableAttr<T>, TreeTableEvent<T>>() {

    @Suppress("UNCHECKED_CAST")
    override fun createAttr(): TreeTableAttr<T> = TreeTableAttr()

    @Suppress("UNCHECKED_CAST")
    override fun createEvent(): TreeTableEvent<T> = TreeTableEvent()

    // region 响应式状态

    /** 当前展开的节点 ID 集合 */
    var expandedIds: Set<String> by observable(emptySet())

    /** 展平后的树节点列表，用于线性渲染 */
    var flatData: List<FlatTreeNode<T>> by observable(emptyList())

    /** 数据版本标记，用于 vbind 触发重建 */
    private var dataVersion: Int by observable(0)

    // endregion

    // region 生命周期

    /**
     * 组件创建回调，从 [TreeTableAttr] 读取初始展开状态并构建展平数据。
     */
    override fun created() {
        super.created()
        expandedIds = attr.initialExpandedIds
        rebuildFlatData()
    }

    // endregion

    // region 树操作

    /**
     * 计算节点的唯一 ID。
     *
     * 优先使用 [TreeNode.id]，为空时尝试 [TreeTableAttr.nodeKeyExtractor]，
     * 均为空时通过路径索引方式在树中查找并生成路径 ID。
     *
     * @param node 目标树节点
     * @return 节点唯一 ID
     */
    private fun resolveNodeId(node: TreeNode<T>): String {
        if (node.id.isNotEmpty()) return node.id
        attr.nodeKeyExtractor?.invoke(node.data)?.let { return it }
        // 回退：在树中查找节点的路径索引
        return findNodePath(attr.treeData, node, "") ?: ""
    }

    /**
     * 在树中递归查找目标节点的路径 ID。
     */
    private fun findNodePath(nodes: List<TreeNode<T>>, target: TreeNode<T>, parentPath: String): String? {
        for ((childIndex, node) in nodes.withIndex()) {
            val path = "$parentPath/$childIndex"
            if (node === target) return path
            if (!node.children.isNullOrEmpty()) {
                findNodePath(node.children!!, target, path)?.let { return it }
            }
        }
        return null
    }

    /**
     * 切换指定节点的展开/折叠状态。
     *
     * 更新 [expandedIds] 后自动重建展平数据，并触发
     * [TreeTableEvent.onNodeExpand] 回调。
     *
     * @param node 要切换的树节点
     */
    fun toggleNode(node: TreeNode<T>) {
        val nodeId = resolveNodeId(node)
        expandedIds = if (nodeId in expandedIds) {
            expandedIds - nodeId
        } else {
            expandedIds + nodeId
        }
        rebuildFlatData()
        event.onNodeExpand?.invoke(node, nodeId in expandedIds)
    }

    /**
     * 展开指定节点（若已展开则无操作）。
     *
     * @param node 要展开的树节点
     */
    fun expandNode(node: TreeNode<T>) {
        val nodeId = resolveNodeId(node)
        if (nodeId !in expandedIds) {
            expandedIds = expandedIds + nodeId
            rebuildFlatData()
            event.onNodeExpand?.invoke(node, true)
        }
    }

    /**
     * 折叠指定节点（若已折叠则无操作）。
     *
     * @param node 要折叠的树节点
     */
    fun collapseNode(node: TreeNode<T>) {
        val nodeId = resolveNodeId(node)
        if (nodeId in expandedIds) {
            expandedIds = expandedIds - nodeId
            rebuildFlatData()
            event.onNodeExpand?.invoke(node, false)
        }
    }

    /**
     * 递归展平树结构为线性列表。
     *
     * 展平算法委托给 [TreeFlattenPipeline]（纯逻辑，可单元测试）。
     *
     * @param nodes 当前层级的树节点列表
     * @param depth 当前深度，根节点层为 0
     * @param parentPath 父节点路径，用于生成无 ID 节点的唯一 key
     * @return 展平后的 [FlatTreeNode] 列表
     */
    private fun flattenTree(nodes: List<TreeNode<T>>, depth: Int = 0, parentPath: String = ""): List<FlatTreeNode<T>> =
        TreeFlattenPipeline.flatten(nodes, expandedIds, attr.nodeKeyExtractor, depth, parentPath)

    /**
     * 重建展平数据并递增版本号，驱动 UI 刷新。
     */
    private fun rebuildFlatData() {
        flatData = flattenTree(attr.treeData)
        dataVersion++
    }

    // endregion

    // region 列构建

    /**
     * 构建包含展开/折叠图标和缩进的树形列定义。
     *
     * 第一列为树形列（缩进 + 图标 + 原始第一列内容），
     * 其余列透传原始列定义（cellRenderer/textExtractor 适配为 FlatTreeNode 版本）。
     *
     * @return 树形列定义列表
     */
    private fun buildTreeColumns(): List<TableColumn<FlatTreeNode<T>>> {
        val self = this
        val firstOriginalCol = attr.columns.firstOrNull()

        // 第一列：缩进 + 展开/折叠图标 + 原始第一列内容
        val treeFirstCol = columnRenderer<FlatTreeNode<T>>(
            title = firstOriginalCol?.title ?: "",
            flex = firstOriginalCol?.flex ?: 1f,
            key = firstOriginalCol?.key ?: "__tree_node__",
        ) { flatNode ->
            View {
                attr {
                    flexDirectionRow()
                    alignItemsCenter()
                }

                // 缩进占位
                if (flatNode.depth > 0) {
                    View {
                        attr {
                            width(self.attr.indentWidth * flatNode.depth)
                        }
                    }
                }

                // 展开/折叠图标（叶子节点显示占位符）
                Text {
                    attr {
                        val iconText = when {
                            !flatNode.hasChildren -> self.attr.leafIcon
                            flatNode.isExpanded -> self.attr.collapseIcon
                            else -> self.attr.expandIcon
                        }
                        text(iconText)
                        width(self.attr.indentWidth)
                        fontSize(12f)
                        color(Color(0xFF666666))
                        textAlignCenter()
                    }
                }

                // 原始第一列内容
                if (firstOriginalCol?.cellRenderer != null) {
                    firstOriginalCol.cellRenderer.invoke(this, flatNode.node.data)
                } else if (firstOriginalCol?.textExtractor != null) {
                    Text {
                        attr {
                            text(firstOriginalCol.textExtractor.invoke(flatNode.node.data))
                            fontSize(14f)
                            color(Color(0xFF333333))
                        }
                    }
                }
            }
        }

        // 剩余列：将 TableColumn<T> 适配为 TableColumn<FlatTreeNode<T>>
        val remainingCols = attr.columns.drop(1).map { col ->
            val adaptedRenderer: (ViewContainer<*, *>.(FlatTreeNode<T>) -> Unit)? =
                col.cellRenderer?.let { renderer ->
                    { flatNode: FlatTreeNode<T> ->
                        // this 是 ViewContainer<*, *> 扩展接收器，由行渲染时提供
                        renderer.invoke(this, flatNode.node.data)
                    }
                }
            TableColumn<FlatTreeNode<T>>(
                key = col.key,
                title = col.title,
                flex = col.flex,
                minWidth = col.minWidth,
                align = col.align,
                sortable = col.sortable,
                textExtractor = col.textExtractor?.let { ext ->
                    { flatNode: FlatTreeNode<T> -> ext(flatNode.node.data) }
                },
                cellRenderer = adaptedRenderer,
            )
        }

        return listOf(treeFirstCol) + remainingCols
    }

    // endregion

    // region body 布局

    /**
     * 构建树形表格的视图层级。
     *
     * 结构：
     * 1. 表头（使用 [TableHeader] 渲染树形列标题）
     * 2. 数据行区域（使用 [List] 组件实现虚拟化滚动，每行按深度缩进）
     *
     * 使用 [vbind] 监听 [dataVersion] 变化，在展开/折叠操作后重建行列表。
     */
    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                flex(1f)
                flexDirectionColumn()
            }

            // 表头
            TableHeader {
                attr {
                    columns = ctx.buildTreeColumns()
                    headerHeight = ctx.attr.headerHeight
                    headerBackgroundColor = ctx.attr.headerBackgroundColor
                    headerTextColor = ctx.attr.headerTextColor
                    headerFontSize = ctx.attr.headerFontSize
                    cellPadding = ctx.attr.cellPadding
                }
            }

            // 数据行区域 - 使用 List 组件实现虚拟化滚动
            List {
                attr {
                    flex(1f)
                }

                vbind({ ctx.dataVersion.toLong() }) {
                    val treeColumns = ctx.buildTreeColumns()

                    for ((index, flatNode) in ctx.flatData.withIndex()) {
                        View {
                            attr {
                                flexDirectionRow()
                                height(ctx.attr.rowHeight)
                                backgroundColor(
                                    if (index % 2 == 1) ctx.attr.stripeRowBackgroundColor
                                    else ctx.attr.rowBackgroundColor
                                )
                            }

                            for (col in treeColumns) {
                                View {
                                    attr {
                                        flex(col.flex)
                                        if (col.minWidth > 0f) minWidth(col.minWidth)
                                        flexDirectionRow()
                                        alignItemsCenter()
                                        padding(0f, ctx.attr.cellPadding, 0f, ctx.attr.cellPadding)

                                        when (col.align) {
                                            TableCellAlign.LEFT -> justifyContentFlexStart()
                                            TableCellAlign.CENTER -> justifyContentCenter()
                                            TableCellAlign.RIGHT -> justifyContentFlexEnd()
                                        }
                                    }

                                    if (col.cellRenderer != null) {
                                        col.cellRenderer.invoke(this, flatNode)
                                    } else if (col.textExtractor != null) {
                                        Text {
                                            attr {
                                                text(col.textExtractor.invoke(flatNode))
                                                fontSize(ctx.attr.cellFontSize)
                                                color(ctx.attr.cellTextColor)

                                                when (col.align) {
                                                    TableCellAlign.LEFT -> textAlignLeft()
                                                    TableCellAlign.CENTER -> textAlignCenter()
                                                    TableCellAlign.RIGHT -> textAlignRight()
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 行点击事件：有子节点时自动切换展开/折叠
                            event {
                                click {
                                    if (flatNode.hasChildren) {
                                        ctx.toggleNode(flatNode.node)
                                    }
                                    ctx.event.onNodeClick?.invoke(flatNode.node, index)
                                }
                            }

                            // 底部分隔线
                            View {
                                attr {
                                    positionAbsolute()
                                    bottom(0f)
                                    left(0f)
                                    right(0f)
                                    height(ctx.attr.separatorHeight)
                                    backgroundColor(ctx.attr.separatorColor)
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

/**
 * 树形表格属性配置类。
 *
 * 通过 DSL 风格设置树形表格的各种属性：
 * ```kotlin
 * TreeTable<FileItem> {
 *     attr {
 *         columns = listOf(
 *             column("名称", flex = 3f) { it.name },
 *             column("大小", flex = 1f) { it.sizeStr }
 *         )
 *         treeData = fileTree
 *         indentWidth = 24f
 *         expandIcon = "▶"
 *         collapseIcon = "▼"
 *         initialExpandedIds = setOf("root")
 *     }
 * }
 * ```
 *
 * @param T 数据行类型
 */
class TreeTableAttr<T> : ComposeAttr() {

    /** 树形数据列表 */
    var treeData: List<TreeNode<T>> = emptyList()

    /** 列定义列表（第一列会被包装为树形列） */
    var columns: List<TableColumn<T>> = emptyList()

    /** 每层缩进宽度（像素），默认 24f */
    var indentWidth: Float = 24f

    /** 折叠态图标文本，默认 "▶" */
    var expandIcon: String = "▶"

    /** 展开态图标文本，默认 "▼" */
    var collapseIcon: String = "▼"

    /** 叶子节点占位文本，默认空格 */
    var leafIcon: String = " "

    /** 节点 key 提取器，当 [TreeNode.id] 为空时使用 */
    var nodeKeyExtractor: ((T) -> String)? = null

    /** 初始展开的节点 ID 集合 */
    var initialExpandedIds: Set<String> = emptySet()

    // === 布局与样式配置 ===

    /** 表头高度，默认 44f */
    var headerHeight: Float = 44f

    /** 表头背景色 */
    var headerBackgroundColor: Color = Color(0xFFFAFAFA)

    /** 表头文字颜色 */
    var headerTextColor: Color = Color(0xFF333333)

    /** 表头文字大小 */
    var headerFontSize: Float = 14f

    /** 数据行高度，默认 48f */
    var rowHeight: Float = 48f

    /** 单元格内边距（左右），默认 12f */
    var cellPadding: Float = 12f

    /** 单元格文字颜色 */
    var cellTextColor: Color = Color(0xFF333333)

    /** 单元格文字大小 */
    var cellFontSize: Float = 14f

    /** 默认行背景色 */
    var rowBackgroundColor: Color = Color.WHITE

    /** 斑马纹偶数行背景色 */
    var stripeRowBackgroundColor: Color = Color(0xFFF9F9F9)

    /** 分隔线颜色 */
    var separatorColor: Color = Color(0xFFEEEEEE)

    /** 分隔线高度，默认 0.5f */
    var separatorHeight: Float = 0.5f
}

/**
 * 树形表格事件回调类。
 *
 * 通过 DSL 风格注册事件回调：
 * ```kotlin
 * TreeTable<FileItem> {
 *     event {
 *         nodeExpand { node, expanded ->
 *             println("${node.data.name} ${if (expanded) "展开" else "折叠"}")
 *         }
 *         nodeClick { node, index ->
 *             println("点击节点: ${node.data.name} at $index")
 *         }
 *     }
 * }
 * ```
 *
 * @param T 数据行类型
 */
class TreeTableEvent<T> : ComposeEvent() {

    /** 节点展开/折叠回调，参数为 (节点, 是否展开) */
    var onNodeExpand: ((TreeNode<T>, Boolean) -> Unit)? = null

    /** 节点点击回调，参数为 (节点, 展平后行索引) */
    var onNodeClick: ((TreeNode<T>, Int) -> Unit)? = null

    /**
     * 设置节点展开/折叠回调（DSL 风格）。
     *
     * @param handler 回调函数，参数为 (节点, 是否展开)
     */
    fun nodeExpand(handler: (TreeNode<T>, Boolean) -> Unit) {
        onNodeExpand = handler
    }

    /**
     * 设置节点点击回调（DSL 风格）。
     *
     * @param handler 回调函数，参数为 (节点, 展平后行索引)
     */
    fun nodeClick(handler: (TreeNode<T>, Int) -> Unit) {
        onNodeClick = handler
    }
}

// region 扩展函数注册

/**
 * 树形表格组件 DSL 入口。
 *
 * 在 [ViewContainer] 中添加 [TreeTableView]，支持通过 `attr {}` 和 `event {}` DSL 配置。
 *
 * @param T 数据行类型
 * @param init 初始化回调，用于设置属性和事件
 */
fun <T> ViewContainer<*, *>.TreeTable(init: TreeTableView<T>.() -> Unit) {
    addChild(TreeTableView<T>(), init)
}

// endregion
