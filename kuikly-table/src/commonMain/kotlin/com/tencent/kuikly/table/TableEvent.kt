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

import com.tencent.kuikly.core.base.ComposeEvent

/**
 * 表格组件事件回调类。
 *
 * 通过 DSL 风格注册事件回调：
 * ```kotlin
 * Table {
 *     event {
 *         rowClick { item, index ->
 *             println("点击行 $index: $item")
 *         }
 *         sortChanged { state ->
 *             println("排序: ${state.columnKey} ${if (state.ascending) "升序" else "降序"}")
 *         }
 *     }
 * }
 * ```
 */
class TableEvent<T> : ComposeEvent() {

    /** 行点击事件回调 */
    var onRowClick: ((T, Int) -> Unit)? = null

    /** 行长按事件回调 */
    var onRowLongClick: ((T, Int) -> Unit)? = null

    /** 排序变化事件回调 */
    var onSortChanged: ((SortState) -> Unit)? = null

    /** 选中行变化事件回调 */
    var onSelectionChanged: ((Set<Int>) -> Unit)? = null

    /**
     * 设置行点击回调（DSL 风格）
     *
     * @param handler 回调函数，参数为 (数据项, 行索引)
     */
    fun rowClick(handler: (T, Int) -> Unit) {
        onRowClick = handler
    }

    /**
     * 设置行长按回调（DSL 风格）
     *
     * @param handler 回调函数，参数为 (数据项, 行索引)
     */
    fun rowLongClick(handler: (T, Int) -> Unit) {
        onRowLongClick = handler
    }

    /**
     * 设置排序变化回调（DSL 风格）
     *
     * @param handler 回调函数，参数为 [SortState]
     */
    fun sortChanged(handler: (SortState) -> Unit) {
        onSortChanged = handler
    }

    /**
     * 设置选中行变化回调（DSL 风格）
     *
     * @param handler 回调函数，参数为选中行索引集合
     */
    fun selectionChanged(handler: (Set<Int>) -> Unit) {
        onSelectionChanged = handler
    }
}
