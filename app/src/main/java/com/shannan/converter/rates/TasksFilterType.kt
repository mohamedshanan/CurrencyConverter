/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.shannan.converter.rates

/**
 * Used with the filter spinner in the rates list.
 */
enum class TasksFilterType {
    /**
     * Do not filter rates.
     */
    ALL_TASKS,

    /**
     * Filters only the active (not completed yet) rates.
     */
    ACTIVE_TASKS,

    /**
     * Filters only the completed rates.
     */
    COMPLETED_TASKS
}
