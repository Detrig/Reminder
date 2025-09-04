package github.detrig.reminder

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import github.detrig.reminder.audio.VoiceInputPage
import github.detrig.reminder.presentation.main.MainActivity

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Before
import org.junit.Rule

@RunWith(AndroidJUnit4::class)
class ScrenarioTest {
    @get:Rule
    val activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    //private lateinit var addTaskPage: AddTaskPage

    @Before
    fun setup() {
    }

    /** Testcase 1 (No internet)
     * 1. click voice button (addTaskScreen)
     * -> no internet connection message
     * 2. click retry
     * -> check loadingState
     * -> check UnsuccessfulLoadingState (no internet connection message)
     */
    @Test
    fun caseNoInternetVoice() {
        val voicePage = VoiceInputPage("Отсутствует соединение с интернетом")

        voicePage.assertNoInternetState()
        voicePage.clickRetry()
        voicePage.assertLoadingState()

        voicePage.waitForLoadingToComplete()
        voicePage.assertNoInternetState()
    }


    /** Testcase 2 (Internet so so!)
     * 1. click voice button (addTaskScreen)
     * -> check ErrorState (no internet connection message)
     * 2. click retry
     * -> check LoadingState
     * -> check InitialState
     * 3. click recording button
     * -> check RecordingState
     * 4. click 2nd time recording button
     * -> check AudioReadyState
     */
    @Test
    fun caseSuccessAudioRecord() {
        val voicePage = VoiceInputPage("Отсутствует соединение с интернетом")

        voicePage.assertNoInternetState()
        voicePage.clickRetry()
        voicePage.assertLoadingState()

        voicePage.waitForLoadingToComplete(5000)
        voicePage.assertInitialState()

        voicePage.clickStartRecording()
        voicePage.waitForRecordingToStart(1000)
        voicePage.assertRecordingState()

        voicePage.clickStartRecording()
        voicePage.waitForRecordingToStop(1000)
        voicePage.assertAudioReadyState()
    }

}