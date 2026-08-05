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

/**
 * 搜索过滤纯逻辑管道。
 *
 * 将关键词过滤算法与 UI 解耦，便于单元测试。
 * 匹配规则：
 * 1. 关键词去除首尾空白，为空时返回原始数据；
 * 2. 配置了自定义 [customExtractor] 时优先使用；
 * 3. 否则遍历所有列的 [TableColumn.textExtractor]，大小写不敏感地检查是否包含关键词。
 */
object FilterPipeline {

    /**
     * 根据关键词过滤数据。
     *
     * @param T 数据行类型
     * @param data 全量数据列表
     * @param keyword 搜索关键词（自动 trim）
     * @param columns 列定义列表，用于默认匹配
     * @param customExtractor 自定义匹配逻辑，null 时使用列 textExtractor 默认匹配
     * @return 过滤后的数据列表
     */
    fun <T> filter(
        data: List<T>,
        keyword: String,
        columns: List<TableColumn<T>>,
        customExtractor: ((T, String) -> Boolean)?,
    ): List<T> {
        val kw = keyword.trim()
        if (kw.isEmpty()) return data
        return if (customExtractor != null) {
            data.filter { item -> customExtractor(item, kw) }
        } else {
            data.filter { item ->
                columns.any { col ->
                    col.textExtractor?.invoke(item)?.contains(kw, ignoreCase = true) == true
                }
            }
        }
    }
}
