package github.detrig.corporatekanbanboard.core

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import com.google.firebase.firestore.PersistentCacheSettings
import github.detrig.corporatekanbanboard.data.local.database.AppDatabase
import java.util.concurrent.TimeUnit

class App : Application(), ProvideViewModel {

    private lateinit var factory: ViewModelFactory
    private lateinit var appDatabase: AppDatabase

    private val clear: ClearViewModel = object : ClearViewModel {
        override fun clearViewModel(viewModelClass: Class<out ViewModel>) {
            factory.clearViewModel(viewModelClass)
        }
    }

    override fun onCreate() {
        super.onCreate()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        FirebaseApp.initializeApp(this)
        appDatabase = AppDatabase.getInstance(this)

        FirebaseFirestore.getInstance()
//        val settings = FirebaseFirestoreSettings.Builder()
//            .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build()) // Включение кэша в памяти
//            .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build()) // Включение дискового кэша
//            .build()
//        firestore.firestoreSettings = settings

        val provideViewModel = ProvideViewModel.Base(clear, appDatabase)
        factory = ViewModelFactory.Base(provideViewModel)
    }


    override fun <T : ViewModel> viewModel(viewModelClass: Class<T>): T =
        factory.viewModel(viewModelClass)

    companion object {
        var currentUserId: String = ""
        var currentUserEmail: String = ""
        var currentUserName: String = ""
    }
}