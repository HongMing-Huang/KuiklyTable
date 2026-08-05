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

/**
 * 表格排序纯逻辑管道。
 *
 * 将排序算法与 UI 解耦，便于单元测试与复用。
 * 排序规则：
 * 1. [SortState] 为 null 时返回原始数据顺序；
 * 2. 优先使用排序列的 [TableColumn.comparator] 进行数值/自定义排序；
 * 3. 无 comparator 时回退到 [TableColumn.textExtractor] 的字符串排序；
 * 4. 无排序列或无提取器时保持原始顺序。
 */
object TableSortPipeline {

    /**
     * 根据排序状态对数据排序。
     *
     * @param T 数据行类型
     * @param data 原始数据列表
     * @param sortState 当前排序状态，null 表示不排序
     * @param columns 列定义列表，用于定位排序列
     * @return 排序后的数据列表
     */
    fun <T> sort(
        data: List<T>,
        sortState: SortState?,
        columns: List<TableColumn<T>>,
    ): List<T> {
        if (sortState == null) return data
        val col = columns.firstOrNull { it.key == sortState.columnKey } ?: return data
        return when {
            col.comparator != null -> {
                val cmp = col.comparator!!
                if (sortState.ascending) data.sortedWith(cmp) else data.sortedWith(cmp).reversed()
            }
            col.textExtractor != null -> {
                val extractor = col.textExtractor!!
                if (sortState.ascending) {
                    data.sortedBy { extractor.invoke(it) }
                } else {
                    data.sortedByDescending { extractor.invoke(it) }
                }
            }
            else -> data
        }
    }
}
