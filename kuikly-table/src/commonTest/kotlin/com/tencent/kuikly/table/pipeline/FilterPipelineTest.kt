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

import com.tencent.kuikly.table.TableColumn
import com.tencent.kuikly.table.column
import kotlin.test.Test
import kotlin.test.assertEquals

class FilterPipelineTest {

    private data class User(val name: String, val dept: String)

    private val users = listOf(
        User("Alice", "研发部"),
        User("Bob", "市场部"),
        User("Carol", "研发部"),
    )

    private val columns: List<TableColumn<User>> = listOf(
        column("姓名", flex = 2f) { it.name },
        column("部门", flex = 1f) { it.dept },
    )

    @Test
    fun emptyKeywordReturnsAllData() {
        assertEquals(users, FilterPipeline.filter(users, "  ", columns, null))
    }

    @Test
    fun defaultMatchUsesAllColumnExtractors() {
        assertEquals(listOf(users[0], users[2]), FilterPipeline.filter(users, "研发", columns, null))
        assertEquals(listOf(users[1]), FilterPipeline.filter(users, "bob", columns, null))
    }

    @Test
    fun defaultMatchIsCaseInsensitive() {
        assertEquals(listOf(users[0]), FilterPipeline.filter(users, "alice", columns, null))
        assertEquals(listOf(users[0]), FilterPipeline.filter(users, "ALICE", columns, null))
    }

    @Test
    fun noMatchReturnsEmpty() {
        assertEquals(emptyList<User>(), FilterPipeline.filter(users, "不存在", columns, null))
    }

    @Test
    fun customExtractorTakesPriority() {
        val result = FilterPipeline.filter(users, "x", columns) { item, kw ->
            item.name.contains(kw, ignoreCase = true)
        }
        assertEquals(emptyList<User>(), result)
    }
}
