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

import com.tencent.kuikly.table.TreeNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TreeFlattenPipelineTest {

    private data class Node(val name: String)

    private val tree = listOf(
        TreeNode(Node("总公司"), id = "root", children = listOf(
            TreeNode(Node("技术部"), id = "tech", children = listOf(
                TreeNode(Node("前端组"), id = "fe"),
                TreeNode(Node("后端组"), id = "be"),
            )),
            TreeNode(Node("市场部"), id = "mkt"),
        )),
    )

    @Test
    fun collapsedRootFlattensOnlyRoot() {
        val flat = TreeFlattenPipeline.flatten(tree, emptySet(), null)
        assertEquals(1, flat.size)
        assertEquals("总公司", flat[0].node.data.name)
        assertEquals(0, flat[0].depth)
        assertTrue(flat[0].hasChildren)
        assertFalse(flat[0].isExpanded)
    }

    @Test
    fun expandedRootIncludesChildrenWithDepth() {
        val flat = TreeFlattenPipeline.flatten(tree, setOf("root"), null)
        assertEquals(3, flat.size)
        assertEquals("技术部", flat[1].node.data.name)
        assertEquals(1, flat[1].depth)
        assertEquals("市场部", flat[2].node.data.name)
    }

    @Test
    fun nestedExpansionIncludesGrandchildren() {
        val flat = TreeFlattenPipeline.flatten(tree, setOf("root", "tech"), null)
        assertEquals(5, flat.size)
        assertEquals("前端组", flat[2].node.data.name)
        assertEquals(2, flat[2].depth)
        assertEquals("后端组", flat[3].node.data.name)
    }

    @Test
    fun nodeKeyExtractorFillsMissingId() {
        val noIdTree = listOf(
            TreeNode(Node("A"), children = listOf(
                TreeNode(Node("B")),
            )),
        )
        // 无 id 无 extractor：路径索引
        val flat1 = TreeFlattenPipeline.flatten(noIdTree, emptySet(), null)
        assertEquals(1, flat1.size)
        // 无 id 有 extractor：可依据 extractor 生成 key 展开
        val flat2 = TreeFlattenPipeline.flatten(noIdTree, setOf("A"), { it.name })
        assertEquals(2, flat2.size)
        assertEquals("B", flat2[1].node.data.name)
    }

    @Test
    fun leafNodeHasNoChildrenAndIsNotExpanded() {
        val flat = TreeFlattenPipeline.flatten(tree, setOf("root", "tech"), null)
        val fe = flat.first { it.node.id == "fe" }
        assertFalse(fe.hasChildren)
        assertFalse(fe.isExpanded)
    }
}
