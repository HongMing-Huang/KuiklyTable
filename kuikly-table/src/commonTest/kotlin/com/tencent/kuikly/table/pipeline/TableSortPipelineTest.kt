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

import com.tencent.kuikly.table.SortState
import com.tencent.kuikly.table.TableColumn
import com.tencent.kuikly.table.column
import kotlin.test.Test
import kotlin.test.assertEquals

class TableSortPipelineTest {

    private data class Row(val name: String, val score: Int)

    private val rows = listOf(
        Row("张三", 92),
        Row("李四", 85),
        Row("王五", 78),
        Row("赵六", 95),
    )

    private val scoreColumns: List<TableColumn<Row>> = listOf(
        column("姓名", flex = 2f) { it.name },
        column("成绩", flex = 1f, sortable = true, comparator = { a, b -> a.score.compareTo(b.score) }) { it.score.toString() },
    )

    @Test
    fun nullSortStateKeepsOriginalOrder() {
        assertEquals(rows, TableSortPipeline.sort(rows, null, scoreColumns))
    }

    @Test
    fun comparatorAscendingSortsByNumericValue() {
        val sorted = TableSortPipeline.sort(rows, SortState("成绩", ascending = true), scoreColumns)
        assertEquals(listOf(78, 85, 92, 95), sorted.map { it.score })
        assertEquals(listOf("王五", "李四", "张三", "赵六"), sorted.map { it.name })
    }

    @Test
    fun comparatorDescendingSortsReversed() {
        val sorted = TableSortPipeline.sort(rows, SortState("成绩", ascending = false), scoreColumns)
        assertEquals(listOf(95, 92, 85, 78), sorted.map { it.score })
    }

    @Test
    fun textExtractorFallbackSortsByString() {
        val textColumns: List<TableColumn<Row>> = listOf(
            column("姓名", flex = 2f) { it.name },
        )
        val sorted = TableSortPipeline.sort(rows, SortState("姓名", ascending = true), textColumns)
        assertEquals(listOf("张三", "李四", "王五", "赵六"), sorted.map { it.name })
    }

    @Test
    fun unknownColumnKeyKeepsOriginalOrder() {
        assertEquals(rows, TableSortPipeline.sort(rows, SortState("不存在的列"), scoreColumns))
    }
}
