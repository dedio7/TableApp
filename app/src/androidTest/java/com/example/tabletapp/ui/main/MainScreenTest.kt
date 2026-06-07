package com.example.tabletapp.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** UI tests for [com.example.tabletapp.ui.main.MainScreen]. */
class MainScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setup() {
    // Fixed: MainScreen expects (NavKey) -> Unit as the first argument.
    composeTestRule.setContent { MainScreen(onItemClick = {}) }
  }

  @Test
  fun batteryWidget_isDisplayed() {
    // BatteryWidget shows "100%" and "Batteria" by default in its initial state
    composeTestRule.onNodeWithText("100%").assertExists()
    composeTestRule.onNodeWithText("Batteria").assertExists()
  }
}
