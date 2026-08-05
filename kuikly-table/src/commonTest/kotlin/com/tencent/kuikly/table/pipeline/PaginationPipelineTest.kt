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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PaginationPipelineTest {

    private val data = (1..23).toList()

    @Test
    fun totalPagesCalculatesCeilDivision() {
        assertEquals(3, PaginationPipeline.totalPages(23, 10))
        assertEquals(1, PaginationPipeline.totalPages(5, 10))
        assertEquals(1, PaginationPipeline.totalPages(10, 10))
    }

    @Test
    fun totalPagesForEmptyOrInvalidInputIsOne() {
        assertEquals(1, PaginationPipeline.totalPages(0, 10))
        assertEquals(1, PaginationPipeline.totalPages(10, 0))
        assertEquals(1, PaginationPipeline.totalPages(10, -1))
    }

    @Test
    fun pageDataSlicesCorrectRange() {
        assertEquals(listOf(1, 2, 3, 4, 5), PaginationPipeline.pageData(data, pageIndex = 1, pageSize = 5))
        assertEquals(listOf(6, 7, 8, 9, 10), PaginationPipeline.pageData(data, pageIndex = 2, pageSize = 5))
        assertEquals(listOf(21, 22, 23), PaginationPipeline.pageData(data, pageIndex = 5, pageSize = 5))
    }

    @Test
    fun pageDataOutOfRangeReturnsEmpty() {
        assertEquals(emptyList<Int>(), PaginationPipeline.pageData(data, pageIndex = 99, pageSize = 10))
        assertEquals(emptyList<Int>(), PaginationPipeline.pageData(data, pageIndex = 1, pageSize = 0))
        assertEquals(emptyList<Int>(), PaginationPipeline.pageData(emptyList(), pageIndex = 1, pageSize = 10))
    }

    @Test
    fun clampPageKeepsPageInRange() {
        assertEquals(1, PaginationPipeline.clampPage(0, 3))
        assertEquals(3, PaginationPipeline.clampPage(99, 3))
        assertEquals(2, PaginationPipeline.clampPage(2, 3))
    }

    @Test
    fun visiblePagesShowsAllWhenWithinLimit() {
        assertEquals(listOf(1, 2, 3), PaginationPipeline.visiblePages(1, 3, 5))
    }

    @Test
    fun visiblePagesUsesEllipsisForLargePageCount() {
        val pages = PaginationPipeline.visiblePages(currentPage = 5, totalPages = 20, maxVisible = 5)
        assertEquals(listOf(1, -1, 4, 5, 6, -1, 20), pages)
    }

    @Test
    fun visiblePagesAtStartHasTrailingEllipsis() {
        val pages = PaginationPipeline.visiblePages(currentPage = 1, totalPages = 20, maxVisible = 5)
        assertEquals(listOf(1, 2, -1, 20), pages)
        assertTrue(-1 in pages)
    }
}
