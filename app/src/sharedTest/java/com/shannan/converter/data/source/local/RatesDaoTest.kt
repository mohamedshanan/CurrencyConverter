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

package com.shannan.converter.data.source.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.shannan.converter.MainCoroutineRule
import com.shannan.converter.data.RatesResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RatesDaoTest {

    private lateinit var database: RatesDatabase

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        // using an in-memory database because the information stored here disappears when the
        // process is killed
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RatesDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertTaskAndGetById() = runBlockingTest {
        // GIVEN - insert a task
        val task = RatesResponse("title", "description")
        database.ratesDao().insertTask(task)

        // WHEN - Get the task by id from the database
        val loaded = database.ratesDao().getTaskById(task.id)

        // THEN - The loaded data contains the expected values
        assertThat<RatesResponse>(loaded as RatesResponse, notNullValue())
        assertThat(loaded.id, `is`(task.id))
        assertThat(loaded.title, `is`(task.title))
        assertThat(loaded.description, `is`(task.description))
        assertThat(loaded.isCompleted, `is`(task.isCompleted))
    }

    @Test
    fun insertTaskReplacesOnConflict() = runBlockingTest {
        // Given that a task is inserted
        val task = RatesResponse("title", "description")
        database.ratesDao().insertTask(task)

        // When a task with the same id is inserted
        val newTask = RatesResponse("title2", "description2", true, task.id)
        database.ratesDao().insertTask(newTask)

        // THEN - The loaded data contains the expected values
        val loaded = database.ratesDao().getTaskById(task.id)
        assertThat(loaded?.id, `is`(task.id))
        assertThat(loaded?.title, `is`("title2"))
        assertThat(loaded?.description, `is`("description2"))
        assertThat(loaded?.isCompleted, `is`(true))
    }

    @Test
    fun insertTaskAndGetTasks() = runBlockingTest {
        // GIVEN - insert a task
        val task = RatesResponse("title", "description")
        database.ratesDao().insertTask(task)

        // WHEN - Get rates from the database
        val tasks = database.ratesDao().getTasks()

        // THEN - There is only 1 task in the database, and contains the expected values
        assertThat(tasks.size, `is`(1))
        assertThat(tasks[0].id, `is`(task.id))
        assertThat(tasks[0].title, `is`(task.title))
        assertThat(tasks[0].description, `is`(task.description))
        assertThat(tasks[0].isCompleted, `is`(task.isCompleted))
    }

    @Test
    fun updateTaskAndGetById() = runBlockingTest {
        // When inserting a task
        val originalTask = RatesResponse("title", "description")
        database.ratesDao().insertTask(originalTask)

        // When the task is updated
        val updatedTask = RatesResponse("new title", "new description", true, originalTask.id)
        database.ratesDao().updateTask(updatedTask)

        // THEN - The loaded data contains the expected values
        val loaded = database.ratesDao().getTaskById(originalTask.id)
        assertThat(loaded?.id, `is`(originalTask.id))
        assertThat(loaded?.title, `is`("new title"))
        assertThat(loaded?.description, `is`("new description"))
        assertThat(loaded?.isCompleted, `is`(true))
    }

    @Test
    fun updateCompletedAndGetById() = runBlockingTest {
        // When inserting a task
        val task = RatesResponse("title", "description", true)
        database.ratesDao().insertTask(task)

        // When the task is updated
        database.ratesDao().updateCompleted(task.id, false)

        // THEN - The loaded data contains the expected values
        val loaded = database.ratesDao().getTaskById(task.id)
        assertThat(loaded?.id, `is`(task.id))
        assertThat(loaded?.title, `is`(task.title))
        assertThat(loaded?.description, `is`(task.description))
        assertThat(loaded?.isCompleted, `is`(false))
    }

    @Test
    fun deleteTaskByIdAndGettingTasks() = runBlockingTest {
        // Given a task inserted
        val task = RatesResponse("title", "description")
        database.ratesDao().insertTask(task)

        // When deleting a task by id
        database.ratesDao().deleteTaskById(task.id)

        // THEN - The list is empty
        val tasks = database.ratesDao().getTasks()
        assertThat(tasks.isEmpty(), `is`(true))
    }

    @Test
    fun deleteTasksAndGettingTasks() = runBlockingTest {
        // Given a task inserted
        database.ratesDao().insertTask(RatesResponse("title", "description"))

        // When deleting all rates
        database.ratesDao().deleteTasks()

        // THEN - The list is empty
        val tasks = database.ratesDao().getTasks()
        assertThat(tasks.isEmpty(), `is`(true))
    }

    @Test
    fun deleteCompletedTasksAndGettingTasks() = runBlockingTest {
        // Given a completed task inserted
        database.ratesDao().insertTask(RatesResponse("completed", "task", true))

        // When deleting completed rates
        database.ratesDao().deleteCompletedTasks()

        // THEN - The list is empty
        val tasks = database.ratesDao().getTasks()
        assertThat(tasks.isEmpty(), `is`(true))
    }
}
