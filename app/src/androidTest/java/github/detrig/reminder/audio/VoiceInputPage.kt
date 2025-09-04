package github.detrig.reminder.audio

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import github.detrig.reminder.R
import org.hamcrest.Matcher

class VoiceInputPage(private val errorText: String) {

    private val containerIdMatcher: Matcher<View> = withParent(withId(R.id.rootLayout))
    private val containerClassTypeMatcher: Matcher<View> =
        withParent(isAssignableFrom(ConstraintLayout::class.java))

    private val microphoneButton = LottieAnimationUi(
        id = R.id.microphoneButton,
        containerIdMatcher = containerIdMatcher,
        containerClassTypeMatcher = containerClassTypeMatcher
    )

    private val progressBar = ProgressBarUi(
        id = R.id.progressBar,
        containerIdMatcher = containerIdMatcher,
        containerClassTypeMatcher = containerClassTypeMatcher
    )

    private val noInternetConnectionLayout = createBaseUi(
        id = R.id.noInternetConnectionMessage,
        containerIdMatcher = containerIdMatcher,
        containerClassTypeMatcher = containerClassTypeMatcher
    )

    private val noInternetAnimation = LottieAnimationUi(
        id = R.id.noInternetConnectionAnimation,
        containerIdMatcher = withParent(withId(R.id.noInternetConnectionMessage)),
        containerClassTypeMatcher = containerClassTypeMatcher
    )

    private val noInternetText = TextViewUi(
        id = R.id.errorTextView,
        containerIdMatcher = withParent(withId(R.id.noInternetConnectionMessage)),
        text = "Отсутствует соединение с интернетом",
        containerClassTypeMatcher = containerClassTypeMatcher
    )

    private val retryButton = ButtonUi(
        id = R.id.retryButton,
        textResId = R.string.retry,
        containerIdMatcher = withParent(withId(R.id.noInternetConnectionMessage)),
        containerClassTypeMatcher = containerClassTypeMatcher
    )

    private val readyLayout = createBaseUi(
        id = R.id.readyLayout,
        containerIdMatcher = containerIdMatcher,
        containerClassTypeMatcher = containerClassTypeMatcher
    )

    private val audioTimeText = TextViewUi(
        id = R.id.audioTimeText,
        text = "",
        containerIdMatcher = withParent(withId(R.id.readyLayout)),
        containerClassTypeMatcher = containerClassTypeMatcher
    )

    private val doneButton = ButtonUi(
        id = R.id.doneButton,
        textResId = R.string.done,
        containerIdMatcher = withParent(withId(R.id.readyLayout)),
        containerClassTypeMatcher = containerClassTypeMatcher
    )

    fun assertInitialState() {
        microphoneButton.assertVisible()
        microphoneButton.assertNotPlaying()
        progressBar.assertNotVisible()
        noInternetConnectionLayout.assertNotVisible()
        readyLayout.assertNotVisible()
    }

    fun assertNoInternetState() {
        noInternetConnectionLayout.assertVisible()
        noInternetAnimation.assertVisible()
        noInternetAnimation.assertPlaying()
        noInternetText.assertTextEquals(errorText)
        retryButton.assertVisible()
        microphoneButton.assertNotVisible()
        progressBar.assertNotVisible()
        readyLayout.assertNotVisible()
        audioTimeText.assertNotVisible()
        doneButton.assertNotVisible()
    }

    fun assertLoadingState() {
        noInternetConnectionLayout.assertNotVisible()
        noInternetAnimation.assertNotVisible()
        noInternetText.assertTextEquals(errorText)
        noInternetText.assertNotVisible()
        retryButton.assertNotVisible()
        microphoneButton.assertNotVisible()
        progressBar.assertVisible()
        readyLayout.assertNotVisible()
        audioTimeText.assertNotVisible()
        doneButton.assertNotVisible()
    }

    fun clickRetry() {
        retryButton.click()
    }

    fun waitForLoadingToComplete(timeMillis: Long = 5000) {
        // Ждем, когда progressBar исчезнет
        progressBar.waitUntilNotVisible(timeMillis)

        // Ждем, когда появится микрофон (начальное состояние)
        microphoneButton.waitUntilVisible(timeMillis)

        // Дополнительно проверяем, что анимация не играет
        microphoneButton.assertNotPlaying()

        // Убеждаемся, что нет сообщения об ошибке
        noInternetConnectionLayout.assertNotVisible()
    }

    fun waitForRecordingToStart(timeout: Long = 5000) {
        microphoneButton.waitUntilPlaying(timeout)
    }

    fun waitForRecordingToStop(timeout: Long = 5000) {
        microphoneButton.waitUntilNotPlaying(timeout)
    }

    fun clickStartRecording() {
        microphoneButton.click()
    }

    fun assertRecordingState() {
        noInternetConnectionLayout.assertNotVisible()
        noInternetAnimation.assertNotVisible()
        noInternetText.assertTextEquals(errorText)
        noInternetText.assertNotVisible()
        retryButton.assertNotVisible()
        microphoneButton.assertVisible()
        microphoneButton.assertPlaying()
        progressBar.assertNotVisible()
        readyLayout.assertNotVisible()
        audioTimeText.assertNotVisible()
        doneButton.assertNotVisible()
    }

    fun assertAudioReadyState() {
        noInternetConnectionLayout.assertNotVisible()
        noInternetAnimation.assertNotVisible()
        noInternetText.assertTextEquals(errorText)
        noInternetText.assertNotVisible()
        retryButton.assertNotVisible()
        microphoneButton.assertVisible()
        microphoneButton.assertNotPlaying()
        progressBar.assertNotVisible()
        readyLayout.assertVisible()
        audioTimeText.assertVisible()
        doneButton.assertVisible()
    }
}