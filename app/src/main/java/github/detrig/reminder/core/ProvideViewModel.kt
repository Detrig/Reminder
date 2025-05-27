package github.detrig.corporatekanbanboard.core

import android.net.ConnectivityManager
import androidx.lifecycle.ViewModel
import github.detrig.corporatekanbanboard.authentication.presentation.login.LoginViewModel
import github.detrig.corporatekanbanboard.authentication.data.AuthRepository
import github.detrig.corporatekanbanboard.authentication.data.CurrentUserRepositoryImpl
import github.detrig.corporatekanbanboard.authentication.data.FirebaseCurrentDataSource
import github.detrig.corporatekanbanboard.authentication.data.PasswordRepository
import github.detrig.corporatekanbanboard.authentication.data.UserRepository
import github.detrig.corporatekanbanboard.authentication.domain.usecase.ForgotPasswordUseCase
import github.detrig.corporatekanbanboard.authentication.domain.usecase.GetCurrentUserRoleUseCase
import github.detrig.corporatekanbanboard.authentication.domain.usecase.IsLoggedInUseCase
import github.detrig.corporatekanbanboard.authentication.domain.usecase.LoginUseCase
import github.detrig.corporatekanbanboard.authentication.domain.usecase.LogoutUseCase
import github.detrig.corporatekanbanboard.authentication.domain.usecase.RegistrationUseCase
import github.detrig.corporatekanbanboard.authentication.domain.utils.CurrentUserLiveDataWrapper
import github.detrig.corporatekanbanboard.authentication.presentation.forgotpassword.ForgotPasswordUiStateLiveDataWrapper
import github.detrig.corporatekanbanboard.authentication.presentation.login.LoginUiStateLiveDataWrapper
import github.detrig.corporatekanbanboard.authentication.presentation.register.RegisterUiStateLiveDataWrapper
import github.detrig.corporatekanbanboard.authentication.presentation.forgotpassword.ForgotPasswordViewModel
import github.detrig.corporatekanbanboard.authentication.presentation.register.RegisterViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import github.detrig.corporatekanbanboard.data.local.database.AppDatabase
import github.detrig.corporatekanbanboard.data.local.datasource.LocalBoardsDataSourceImpl
import github.detrig.corporatekanbanboard.data.remote.boards.BoardsRepositoryImpl
import github.detrig.corporatekanbanboard.data.remote.boards.RemoteBoardsDataSourceImpl
import github.detrig.corporatekanbanboard.data.remote.chat.ChatRepositoryImpl
import github.detrig.corporatekanbanboard.data.remote.user.RemoteUserBoardDataSourceImpl
import github.detrig.corporatekanbanboard.data.remote.user.UserBoardRepositoryImpl
import github.detrig.corporatekanbanboard.domain.model.Board
import github.detrig.corporatekanbanboard.presentation.boards.BoardsViewModel
import github.detrig.corporatekanbanboard.main.MainViewModel
import github.detrig.corporatekanbanboard.presentation.addBoard.AddBoardViewModel
import github.detrig.corporatekanbanboard.presentation.addtask.AddTaskViewModel
import github.detrig.corporatekanbanboard.presentation.addtask.WorkersLiveDataWrapper
import github.detrig.corporatekanbanboard.presentation.boardMain.BoardMainViewModel
import github.detrig.corporatekanbanboard.presentation.boardMain.ClickedTaskLiveDataWrapper
import github.detrig.corporatekanbanboard.presentation.boardMain.ColumnToAddLiveDataWrapper
import github.detrig.corporatekanbanboard.presentation.boardSettings.BoardSettingsViewModel
import github.detrig.corporatekanbanboard.presentation.boards.ClickedBoardLiveDataWrapper
import github.detrig.corporatekanbanboard.presentation.boards.ClickedBoardUsersLiveDataWrapper
import github.detrig.corporatekanbanboard.presentation.columnInfo.ClickedColumnLiveDataWrapper
import github.detrig.corporatekanbanboard.presentation.columnInfo.ColumnInfoViewModel
import github.detrig.corporatekanbanboard.presentation.globalchat.GlobalChatViewModel
import github.detrig.corporatekanbanboard.presentation.profile.ProfileEditScreen
import github.detrig.corporatekanbanboard.presentation.profile.ProfileEditViewModel
import github.detrig.corporatekanbanboard.presentation.taskInfo.TaskInfoViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

interface ProvideViewModel {

    fun <T : ViewModel> viewModel(viewModelClass: Class<T>): T

    class Base(
        private val clear: ClearViewModel,
        private val appDatabase: AppDatabase
    ) : ProvideViewModel {
        private val fireBaseAuth = Firebase.auth
        private val fireBaseFirestore = Firebase.firestore
        private val firebaseDatabase = Firebase.database
        private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        private val navigation = Navigation.Base()

        //Auth
        private val authRepository = AuthRepository.Base(fireBaseAuth, fireBaseFirestore)
        private val passwordRepository = PasswordRepository.Base(fireBaseAuth)
        private val userRepository = UserRepository.Base(fireBaseAuth, fireBaseFirestore)

        //Login
        private val loginUiStateLiveDataWrapper = LoginUiStateLiveDataWrapper.Base()
        private val loginUseCase = LoginUseCase(authRepository)
        private val isLoggedInUseCase = IsLoggedInUseCase(userRepository)

        //Logout
        private val logoutUseCase = LogoutUseCase(authRepository)

        //Register
        private val registerUiStateLiveDataWrapper = RegisterUiStateLiveDataWrapper.Base()
        private val registerUseCase = RegistrationUseCase(authRepository)

        //Reset password
        private val forgotPasswordUiStateLiveDataWrapper =
            ForgotPasswordUiStateLiveDataWrapper.Base()
        private val forgotPasswordUseCase = ForgotPasswordUseCase(passwordRepository)

        //CurrentUser
        private val currentUserDataSource =
            FirebaseCurrentDataSource(fireBaseAuth, fireBaseFirestore)
        private val currentUserRepository = CurrentUserRepositoryImpl(currentUserDataSource)
        private val currentUserLiveDataWrapper = CurrentUserLiveDataWrapper.Base()
        private val getCurrentUserRoleUseCase = GetCurrentUserRoleUseCase(currentUserRepository)

        //User
        private val userBoardDataSource = RemoteUserBoardDataSourceImpl(fireBaseFirestore)
        private val userBoardRepository = UserBoardRepositoryImpl(userBoardDataSource)
        private val clickedBoardUserLiveDataWrapper = ClickedBoardUsersLiveDataWrapper.Base()

        //Boards
        //private val boardsLocalRepo
        private val boardsCommunication = BaseCommunication<Board>()
        private val localBoardDataSource = LocalBoardsDataSourceImpl(appDatabase.boardsDao())
        private val remoteBoardDataSource = RemoteBoardsDataSourceImpl(fireBaseFirestore)
        private val boardsRepository =
            BoardsRepositoryImpl(localBoardDataSource, remoteBoardDataSource, userBoardDataSource)
        private val clickedBoardLiveDataWrapper = ClickedBoardLiveDataWrapper.Base()

        //Tasks
        private val clickedTaskLiveDataWrapper = ClickedTaskLiveDataWrapper.Base()

        //Column
        private val columnToAddLiveDataWrapper = ColumnToAddLiveDataWrapper.Base()
        private val clickedColumnLiveDataWrapper = ClickedColumnLiveDataWrapper.Base()

        //Chat
        private val chatRepository = ChatRepositoryImpl(firebaseDatabase)

        //Workers
        private val workersLiveDataWrapper = WorkersLiveDataWrapper.Base()

        override fun <T : ViewModel> viewModel(viewModelClass: Class<T>): T {
            return when (viewModelClass) {

                LoginViewModel::class.java -> LoginViewModel(
                    navigation,
                    loginUiStateLiveDataWrapper,
                    currentUserLiveDataWrapper,
                    loginUseCase,
                    getCurrentUserRoleUseCase,
                    viewModelScope
                )

                MainViewModel::class.java -> MainViewModel(
                    navigation,
                    currentUserLiveDataWrapper,
                    getCurrentUserRoleUseCase,
                    viewModelScope
                )

                RegisterViewModel::class.java -> RegisterViewModel(
                    navigation,
                    registerUiStateLiveDataWrapper,
                    registerUseCase,
                    viewModelScope
                )

                ForgotPasswordViewModel::class.java -> ForgotPasswordViewModel(
                    navigation,
                    clear,
                    forgotPasswordUiStateLiveDataWrapper,
                    forgotPasswordUseCase,
                    viewModelScope
                )

                BoardsViewModel::class.java -> BoardsViewModel(
                    navigation,
                    boardsCommunication,
                    currentUserLiveDataWrapper,
                    clickedBoardLiveDataWrapper,
                    clickedBoardUserLiveDataWrapper,
                    boardsRepository,
                    userBoardRepository,
                    viewModelScope
                )

                AddBoardViewModel::class.java -> AddBoardViewModel(
                    navigation,
                    boardsRepository,
                    boardsCommunication,
                    currentUserLiveDataWrapper,
                    viewModelScope
                )

                BoardMainViewModel::class.java -> BoardMainViewModel(
                    navigation,
                    boardsRepository,
                    clickedBoardLiveDataWrapper,
                    clickedColumnLiveDataWrapper,
                    clickedTaskLiveDataWrapper,
                    columnToAddLiveDataWrapper,
                    viewModelScope
                )

                AddTaskViewModel::class.java -> AddTaskViewModel(
                    navigation,
                    boardsRepository,
                    clickedBoardLiveDataWrapper,
                    clickedBoardUserLiveDataWrapper,
                    workersLiveDataWrapper,
                    columnToAddLiveDataWrapper,
                    viewModelScope
                )

                TaskInfoViewModel::class.java -> TaskInfoViewModel(
                    navigation,
                    boardsRepository,
                    clickedBoardLiveDataWrapper,
                    clickedTaskLiveDataWrapper,
                    clickedBoardUserLiveDataWrapper,
                    viewModelScope
                )

                BoardSettingsViewModel::class.java -> BoardSettingsViewModel(
                    navigation,
                    boardsRepository,
                    userBoardRepository,
                    clickedBoardLiveDataWrapper,
                    clickedBoardUserLiveDataWrapper,
                    viewModelScope
                )

                GlobalChatViewModel::class.java -> GlobalChatViewModel(
                    chatRepository,
                    firebaseDatabase,
                    viewModelScope
                )

                ProfileEditViewModel::class.java -> ProfileEditViewModel(
                    navigation,
                    logoutUseCase,
                    currentUserRepository,
                    currentUserLiveDataWrapper,
                    viewModelScope
                )

                ColumnInfoViewModel::class.java -> ColumnInfoViewModel(
                    navigation,
                    boardsRepository,
                    clickedColumnLiveDataWrapper,
                    clickedBoardLiveDataWrapper,
                    clickedBoardUserLiveDataWrapper,
                    viewModelScope
                )
                else -> throw IllegalStateException("unknown viewModelClass $viewModelClass")
            } as T
        }
    }
}