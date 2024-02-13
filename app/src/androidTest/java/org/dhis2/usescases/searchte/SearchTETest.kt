package org.dhis2.usescases.searchte

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResourceTimeoutException
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import dispatch.android.espresso.IdlingDispatcherProvider
import dispatch.android.espresso.IdlingDispatcherProviderRule
import org.dhis2.R
import org.dhis2.bindings.app
import org.dhis2.common.idlingresources.MapIdlingResource
import org.dhis2.ui.dialogs.bottomsheet.SECONDARY_BUTTON_TAG
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.flow.teiFlow.TeiFlowTest
import org.dhis2.usescases.flow.teiFlow.entity.DateRegistrationUIModel
import org.dhis2.usescases.flow.teiFlow.entity.RegisterTEIUIModel
import org.dhis2.usescases.flow.teiFlow.teiFlowRobot
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.searchte.entity.DisplayListFieldsUIModel
import org.dhis2.usescases.searchte.robot.filterRobot
import org.dhis2.usescases.searchte.robot.searchTeiRobot
import org.dhis2.usescases.teidashboard.robot.teiDashboardRobot
import org.junit.After
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import java.util.Date

@RunWith(AndroidJUnit4::class)
class SearchTETest : BaseTest() {

    @get:Rule
    val rule = ActivityTestRule(SearchTEActivity::class.java, false, false)

    private var mapIdlingResource: MapIdlingResource? = null

    private val customDispatcherProvider =
        context.applicationContext.app().appComponent().customDispatcherProvider()

    @JvmField
    @Rule
    val idlingRule = IdlingDispatcherProviderRule {
        IdlingDispatcherProvider(customDispatcherProvider)
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shouldSuccessfullySearchByName() {
        val firstName = "Tim"
        val firstNamePosition = 0
        val orgUnit = "Ngelehun CHC"

        prepareChildProgrammeIntentAndLaunchActivity(rule)

        searchTeiRobot {
            clickOnOpenSearch()
            typeAttributeAtPosition(firstName, firstNamePosition)
            clickOnSearch(composeTestRule)
            checkListOfSearchTEI(firstName, orgUnit)
        }
    }

    @Test
    fun shouldShowErrorWhenCanNotFindSearchResult() {
        val firstName = "asdssds"
        val firstNamePosition = 1

        prepareTestProgramRulesProgrammeIntentAndLaunchActivity(rule)

        searchTeiRobot {
            clickOnOpenSearch()
            typeAttributeAtPosition(firstName, firstNamePosition)
            clickOnSearch(composeTestRule)
            checkNoSearchResult()
        }
    }

    @Test
    fun shouldSuccessfullySearchUsingMoreThanOneField() {
        val firstName = "Anna"
        val firstNamePosition = 0
        val lastName = "Jones"
        val lastNamePosition = 1

        prepareChildProgrammeIntentAndLaunchActivity(rule)

        searchTeiRobot {
            clickOnOpenSearch()
            typeAttributeAtPosition(firstName, firstNamePosition)
            typeAttributeAtPosition(lastName, lastNamePosition)
            clickOnSearch(composeTestRule)
            checkListOfSearchTEI(firstName, lastName)
        }
    }

    @Test
    @Ignore("Actions are being performed, but the test fails upon selecting the option in the spinner")
    fun shouldSuccessfullyChangeBetweenPrograms() {
        val tbProgram = "TB program"

        prepareChildProgrammeIntentAndLaunchActivity(rule)

        searchTeiRobot {
            clickOnProgramSpinner()
            selectAProgram(tbProgram)
            checkProgramHasChanged(tbProgram)
        }
    }

    @Test
    fun shouldCheckDisplayInList() {
        val birthdaySearch = createDateOfBirthSearch()
        val displayInListData = createDisplayListFields()
        val namePosition = 0
        val lastNamePosition = 1

        setDatePicker()
        prepareTestAdultWomanProgrammeIntentAndLaunchActivity(rule)

        searchTeiRobot {
            typeAttributeAtPosition(displayInListData.name, namePosition)
            typeAttributeAtPosition(displayInListData.lastName, lastNamePosition)
            clickOnDateField()
            selectSpecificDate(birthdaySearch.year, birthdaySearch.month, birthdaySearch.day)
            acceptDate()
            clickOnSearch(composeTestRule)
            checkFieldsFromDisplayList(
                composeTestRule,
                displayInListData,
            )
        }
    }

    @Test
    fun shouldSuccessfullyFilterByEnrollmentStatusActive() {
        val enrollmentStatusFilter = context.getString(R.string.filters_title_enrollment_status)
            .format(context.resources.getQuantityString(R.plurals.enrollment, 1).capitalize(Locale.current))
        val totalFilterCount = "2"
        val filterCount = "1"

        prepareChildProgrammeIntentAndLaunchActivity(rule)

        filterRobot {
            clickOnFilter()
            clickOnFilterBy(enrollmentStatusFilter)
            clickOnFilterActiveOption()
            clickOnSortByField(enrollmentStatusFilter)
            checkFilterCounter(totalFilterCount)
            checkCountAtFilter(enrollmentStatusFilter, filterCount)
            clickOnFilter()
            checkTEIsAreOpen()
        }
    }

    @Test
    fun shouldSuccessfullyFilterByEventStatusOverdue() {
        val eventStatusFilter = context.getString(R.string.filters_title_event_status)
        val totalCount = "1"

        val programStage = "PNC Visit"
        val orgUnit = "Ngelehun CHC"
        val registerTeiDetails = createRegisterTEI()
        val overdueDate = createOverdueDate()

        setDatePicker()
        prepareTestAdultWomanProgrammeIntentAndLaunchActivity(rule)

        teiFlowRobot {
            registerTEI(registerTeiDetails, composeTestRule)
            changeDueDate(overdueDate, programStage, orgUnit, composeTestRule)
            pressBack()
            composeTestRule.onNodeWithTag(SECONDARY_BUTTON_TAG).performClick()
            pressBack()
        }

        filterRobot {
            clickOnFilter()
            clickOnFilterBy(eventStatusFilter)
            clickOnFilterOverdueOption()
            closeFilterRowAtField(eventStatusFilter)
            checkFilterCounter(totalCount)
            checkCountAtFilter(eventStatusFilter, totalCount)
            clickOnFilter()
            checkEventsAreOverdue()
        }
    }

    @Test
    fun shouldSuccessfullyFilterByOrgUnitAndUseSort() {
        val orgUnitFilter = "ORG. UNIT"
        val orgUnitNgelehun = "Ngelehun CHC"
        val totalCount = "2"
        val filterCount = "1"
        prepareChildProgrammeIntentAndLaunchActivity(rule)

        filterRobot {
            clickOnFilter()
            clickOnFilterBy(orgUnitFilter)
            clickOnSortByField(orgUnitFilter)
            typeOrgUnitField(orgUnitNgelehun)
            checkFilterCounter(totalCount)
            checkCountAtFilter(orgUnitFilter, filterCount)
            clickOnFilter()
            checkTEIWithOrgUnit(orgUnitNgelehun)
        }
    }

    @Test
    fun shouldSuccessfullyFilterByEnrollmentDateAndSort() {
        val enrollmentDate = "DATE OF ENROLLMENT"
        val enrollmentDateFrom = createFromEnrollmentDate()
        val enrollmentDateTo = createToEnrollmentDate()
        val startDate = "2021-05-01"
        val endDate = "2021-05-31"
        val totalFilterCount = "2"
        val filterCount = "1"

        setDatePicker()
        prepareChildProgrammeIntentAndLaunchActivity(rule)

        filterRobot {
            clickOnFilter()
            clickOnFilterBy(enrollmentDate)
            clickOnFromToDate()
            chooseDate(enrollmentDateFrom.year, enrollmentDateFrom.month, enrollmentDateFrom.day)
            chooseDate(enrollmentDateTo.year, enrollmentDateTo.month, enrollmentDateTo.day)
            clickOnSortByField(enrollmentDate)
            checkFilterCounter(totalFilterCount)
            checkCountAtFilter(enrollmentDate, filterCount)
            clickOnFilter()
            checkDateIsInRange(startDate, endDate)
        }
    }

    @Test
    fun shouldSuccessfullyFilterByEventDateAndSort() {
        val eventDate = context.getString(R.string.filters_title_event_date)
        val eventDateFrom = createFromEventDate()
        val eventDateTo = createToEventDate()
        val startDate = "2020-05-01"
        val endDate = "2020-05-31"
        val totalCount = "2"
        val filterCount = "1"

        setDatePicker()
        prepareChildProgrammeIntentAndLaunchActivity(rule)

        filterRobot {
            clickOnFilter()
            clickOnFilterBy(eventDate)
            clickOnFromToDate()
            chooseDate(eventDateFrom.year, eventDateFrom.month, eventDateFrom.day)
            chooseDate(eventDateTo.year, eventDateTo.month, eventDateTo.day)
            clickOnSortByField(eventDate)
            checkFilterCounter(totalCount)
            checkCountAtFilter(eventDate, filterCount)
            clickOnFilter()
            checkDateIsInRange(startDate, endDate)
        }
    }

    @Test
    fun shouldSuccessfullyFilterBySync() {
        val teiName = "Frank"
        val teiLastName = "Fjordsen"
        val firstNamePosition = 0
        val lastNamePosition = 1
        val syncFilter = context.getString(R.string.action_sync)
        val totalCount = "1"
        prepareChildProgrammeIntentAndLaunchActivity(rule)

        searchTeiRobot {
            clickOnOpenSearch()
            typeAttributeAtPosition(teiName, firstNamePosition)
            typeAttributeAtPosition(teiLastName, lastNamePosition)
            clickOnSearch(composeTestRule)
            clickOnTEI(teiName, teiLastName)
        }

        teiDashboardRobot {
            clickOnMenuMoreOptions()
            clickOnMenuReOpen()
            pressBack()
        }

        filterRobot {
            clickOnFilter()
            clickOnFilterBy(syncFilter)
            clickOnNotSync()
            checkFilterCounter(totalCount)
            checkCountAtFilter(syncFilter, totalCount)
            clickOnFilter()
            checkTEINotSync()
        }
    }

    @Test
    fun shouldSuccessfullySearchAndFilter() {
        val name = "Anna"
        val lastName = "Jones"
        val namePosition = 0
        val enrollmentStatus = context.getString(R.string.filters_title_enrollment_status)
            .format(context.resources.getQuantityString(R.plurals.enrollment, 1).capitalize(Locale.current))
        val totalCount = "2"
        val totalFilterCount = "1"

        prepareChildProgrammeIntentAndLaunchActivity(rule)

        searchTeiRobot {
            clickOnOpenSearch()
            typeAttributeAtPosition(name, namePosition)
            clickOnSearch(composeTestRule)
        }

        filterRobot {
            clickOnFilter()
            clickOnFilterBy(enrollmentStatus)
            clickOnFilterActiveOption()
            clickOnSortByField(enrollmentStatus)
            checkFilterCounter(totalCount)
            checkCountAtFilter(enrollmentStatus, totalFilterCount)
            clickOnFilter()
            checkTEIsAreOpen()
        }

        searchTeiRobot {
            checkListOfSearchTEI(name, lastName)
        }
    }

    @Test
    fun shouldSuccessfullyShowMapAndTeiCard() {
        val firstName = "Filona"

        prepareChildProgrammeIntentAndLaunchActivity(rule)

        searchTeiRobot {
            clickOnShowMap()
            try {
                val device = UiDevice.getInstance(getInstrumentation())
                device.wait(Until.hasObject(By.desc(MAP_LOADED)), 6000)
                checkCarouselTEICardInfo(firstName)
            } catch (ex: IdlingResourceTimeoutException) {
                throw RuntimeException("Could not start test")
            }
        }
    }

    @After
    fun unregisterIdlingResource() {
        if (mapIdlingResource != null) {
            IdlingRegistry.getInstance().unregister(mapIdlingResource)
        }
    }

    private fun createDateOfBirthSearch() = DateRegistrationUIModel(
        2001,
        1,
        1
    )

    private fun createDisplayListFields() = DisplayListFieldsUIModel(
        "Sarah",
        "Thompson",
        "2001-01-01",
        "sarah@gmail.com",
        "Main street 1",
        "56",
        "167"
    )

    private fun createFromEnrollmentDate() = DateRegistrationUIModel(
        2021,
        5,
        1
    )

    private fun createToEnrollmentDate() = DateRegistrationUIModel(
        2021,
        5,
        31
    )

    private fun createFromEventDate() = DateRegistrationUIModel(
        2020,
        5,
        1
    )

    private fun createToEventDate() = DateRegistrationUIModel(
        2020,
        5,
        31
    )

    private fun createRegisterTEI() = RegisterTEIUIModel(
        "ADRIANNA",
        "ROBERTS",
        dateRegistration,
        dateEnrollment
    )

    private fun createFirstSpecificDate() = DateRegistrationUIModel(
        2000,
        6,
        30
    )

    private fun createEnrollmentDate() = DateRegistrationUIModel(
        2020,
        10,
        30
    )

    private fun getSplitCurrentDate(): DateRegistrationUIModel {
        val sdf = SimpleDateFormat(TeiFlowTest.DATE_FORMAT)
        val dateFormat = sdf.format(Date())
        val splitDate: Array<String> = dateFormat.removePrefix("0").split("/").toTypedArray()
        val day = splitDate[0].toInt()
        val month = splitDate[1].toInt()
        val year = splitDate[2].toInt()
        return DateRegistrationUIModel(year, month, day)
    }

    private fun createOverdueDate() = DateRegistrationUIModel(
        currentDate.year,
        currentDate.month - 1,
        currentDate.day
    )

    private val dateRegistration = createFirstSpecificDate()
    private val dateEnrollment = createEnrollmentDate()
    private val currentDate = getSplitCurrentDate()

    companion object {
        const val PROGRAM_UID = "PROGRAM_UID"
        const val CHILD_PROGRAM_UID_VALUE = "IpHINAT79UW"

        const val CHILD_TE_TYPE_VALUE = "nEenWmSyUEp"
        const val CHILD_TE_TYPE = "TRACKED_ENTITY_UID"
        const val MAP_LOADED = "LOADED"
    }
}
