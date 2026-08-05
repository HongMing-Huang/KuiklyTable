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

package com.tencent.kuikly.table.pipeline

import com.tencent.kuikly.table.FlatTreeNode
import com.tencent.kuikly.table.TreeNode

/**
 * 树形展平纯逻辑管道。
 *
 * 将树结构递归展平为线性列表，与 UI 解耦，便于单元测试。
 * 规则：
 * 1. 每个节点生成一个 [FlatTreeNode]（含深度/是否有子节点/是否展开）；
 * 2. 仅展开 [expandedIds] 中存在的节点子树，叶子节点始终包含；
 * 3. 节点 ID 生成：优先 [TreeNode.id]，其次 [nodeKeyExtractor]，
 *    均缺失时使用路径索引（如 "0/1/2"）避免空 ID 碰撞。
 */
object TreeFlattenPipeline {

    /**
     * 递归展平树结构为线性列表。
     *
     * @param T 原始数据类型
     * @param nodes 当前层级的树节点列表
     * @param expandedIds 当前展开的节点 ID 集合
     * @param nodeKeyExtractor 节点 key 提取器，[TreeNode.id] 为空时使用
     * @param depth 当前深度，根节点层为 0
     * @param parentPath 父节点路径，用于生成无 ID 节点的唯一 key
     * @return 展平后的 [FlatTreeNode] 列表
     */
    fun <T> flatten(
        nodes: List<TreeNode<T>>,
        expandedIds: Set<String>,
        nodeKeyExtractor: ((T) -> String)?,
        depth: Int = 0,
        parentPath: String = "",
    ): List<FlatTreeNode<T>> {
        val result = mutableListOf<FlatTreeNode<T>>()
        for ((childIndex, node) in nodes.withIndex()) {
            val hasChildren = !node.children.isNullOrEmpty()
            val nodeId = node.id.ifEmpty {
                nodeKeyExtractor?.invoke(node.data) ?: "$parentPath/$childIndex"
            }
            val isExpanded = nodeId in expandedIds
            result.add(FlatTreeNode(node, depth, hasChildren, isExpanded))
            if (hasChildren && isExpanded) {
                result.addAll(flatten(node.children!!, expandedIds, nodeKeyExtractor, depth + 1, nodeId))
            }
        }
        return result
    }
}
