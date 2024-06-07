package org.dhis2.usescases.teidashboard.robot

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.dhis2.R
import org.dhis2.common.BaseRobot
import org.dhis2.common.matchers.hasCompletedPercentage
import org.dhis2.ui.dialogs.bottomsheet.MAIN_BUTTON_TAG
import org.dhis2.ui.dialogs.bottomsheet.SECONDARY_BUTTON_TAG

fun eventRobot(
    composeTestRule: ComposeTestRule,
    eventRobot: EventRobot.() -> Unit
) {
    EventRobot(composeTestRule).apply {
        eventRobot()
    }
}

class EventRobot(val composeTestRule: ComposeTestRule) : BaseRobot() {

    fun clickOnFormFabButton() {
        waitForView(withId(R.id.actionButton)).perform(click())
    }

    fun clickOnNotNow() {
        composeTestRule.onNodeWithTag(SECONDARY_BUTTON_TAG).performClick()
    }

    fun clickOnCompleteButton() {
        composeTestRule.onNodeWithTag(MAIN_BUTTON_TAG).performClick()
    }

    fun checkSecondaryButtonNotVisible() {
        composeTestRule.onNodeWithTag(SECONDARY_BUTTON_TAG).assertDoesNotExist()
    }

    fun clickOnReopen() {
        composeTestRule.onNodeWithTag("REOPEN_BUTTON").performClick()
    }

    fun acceptUpdateEventDate() {
        composeTestRule.onNodeWithText("OK", true).performClick()
    }

    fun openMenuMoreOptions() {
        onView(withId(R.id.moreOptions)).perform(click())
    }

    fun clickOnDelete() {
        onView(withText(R.string.delete)).perform(click())
    }

    fun clickOnDeleteDialog() {
        onView(withId(R.id.possitive)).perform(click())
    }

    fun clickOnEventReportDate() {
        composeTestRule.onNode(
            hasTestTag("INPUT_DATE_TIME_ACTION_BUTTON") and hasAnySibling(
                hasText("Report date")
            )
        ).assertIsDisplayed().performClick()

    }

    fun selectSpecificDate(date: String) {
        composeTestRule.onNodeWithTag("DATE_PICKER").assertIsDisplayed()
        composeTestRule.onNode(hasText(date, true)).performClick()
    }

    fun typeOnDateParameter(dateValue: String) {
        composeTestRule.apply {
            onNodeWithTag("INPUT_DATE_TIME_TEXT_FIELD").performClick()
            onNodeWithTag("INPUT_DATE_TIME_TEXT_FIELD").performTextInput(dateValue)
        }
    }

    fun checkEventDetails(eventDate: String, eventOrgUnit: String) {
        onView(withId(R.id.completion)).check(matches(hasCompletedPercentage(100)))
        val formattedDate = formatStoredDateToUI(eventDate)
        composeTestRule.onNodeWithText(formattedDate).assertIsDisplayed()
        composeTestRule.onNodeWithText(eventOrgUnit).assertIsDisplayed()
    }

    fun openEventDetailsSection() {
        composeTestRule.onNodeWithText("Event details").performClick()
    }

    fun checkEventIsOpen() {
        composeTestRule.onNodeWithTag("REOPEN_BUTTON").assertDoesNotExist()
    }

    private fun formatStoredDateToUI(dateValue: String): String {
        val components = dateValue.split("/")

        val year = components[2]
        val month = if (components[1].length == 1) {
            "0${components[1]}"
        } else {
            components[1]
        }
        val day = if (components[0].length == 1) {
            "0${components[0]}"
        } else {
            components[0]
        }

        return "$day/$month/$year"
    }
}
