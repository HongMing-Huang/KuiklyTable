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

import kotlin.math.max

/**
 * 分页纯逻辑管道。
 *
 * 将分页算法（总页数、切片、页码 clamp、可见页码）与 UI 解耦，便于单元测试。
 * 页码约定：从 1 开始。
 */
object PaginationPipeline {

    /**
     * 计算总页数。
     *
     * 空数据或非法 pageSize 时返回 1（与分页栏"至少一页"约定一致）。
     *
     * @param totalItems 总数据条数
     * @param pageSize 每页条数
     * @return 总页数
     */
    fun totalPages(totalItems: Int, pageSize: Int): Int =
        if (totalItems <= 0 || pageSize <= 0) 1 else (totalItems + pageSize - 1) / pageSize

    /**
     * 返回指定页的数据切片。
     *
     * @param T 数据行类型
     * @param data 全量数据列表
     * @param pageIndex 目标页码（从 1 开始），越界时返回空列表
     * @param pageSize 每页条数
     * @return 当前页数据列表
     */
    fun <T> pageData(data: List<T>, pageIndex: Int, pageSize: Int): List<T> {
        if (data.isEmpty() || pageSize <= 0) return emptyList()
        val start = (pageIndex - 1) * pageSize
        if (start >= data.size) return emptyList()
        val end = minOf(start + pageSize, data.size)
        return data.subList(start, end)
    }

    /**
     * 将页码 clamp 到合法范围 [1, totalPages]。
     *
     * @param page 目标页码
     * @param totalPages 总页数
     * @return clamp 后的页码
     */
    fun clampPage(page: Int, totalPages: Int): Int = page.coerceIn(1, totalPages)

    /**
     * 计算当前可见的页码列表。
     *
     * 返回列表中的元素：
     * - 正整数表示页码
     * - -1 表示省略号位置
     *
     * 例如：[1, -1, 4, 5, 6, 7, 8, -1, 20]
     *
     * @param currentPage 当前页码
     * @param totalPages 总页数
     * @param maxVisible 可见的最大页码按钮数量
     * @return 可见页码列表
     */
    fun visiblePages(currentPage: Int, totalPages: Int, maxVisible: Int): List<Int> {
        if (totalPages <= 0 || maxVisible <= 0) return emptyList()
        // 总页数小于等于可见数量，全部显示
        if (totalPages <= maxVisible) {
            return (1..totalPages).toList()
        }

        val pages = mutableListOf<Int>()
        // 始终显示第一页
        pages.add(1)

        // 计算中间页码范围（去掉首尾页后的半宽）
        val halfRange = (maxVisible - 2) / 2
        val rangeStart = max(2, currentPage - halfRange)
        val rangeEnd = minOf(totalPages - 1, currentPage + halfRange)

        // 如果范围起点不紧跟第一页，加省略号
        if (rangeStart > 2) {
            pages.add(-1)
        }

        // 添加中间页码
        for (p in rangeStart..rangeEnd) {
            pages.add(p)
        }

        // 如果范围终点不紧跟末页，加省略号
        if (rangeEnd < totalPages - 1) {
            pages.add(-1)
        }

        // 始终显示最后一页
        pages.add(totalPages)

        return pages
    }
}
