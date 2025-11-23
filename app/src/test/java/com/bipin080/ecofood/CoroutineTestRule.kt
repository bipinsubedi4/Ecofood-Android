package com.bipin080.ecofood

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*

import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class CoroutineTestRule(
    val dispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {

    val scheduler: TestCoroutineScheduler = dispatcher.scheduler

    val scope = TestScope(scheduler)

    override fun starting(description: Description?) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description?) {
        Dispatchers.resetMain()
    }
}
