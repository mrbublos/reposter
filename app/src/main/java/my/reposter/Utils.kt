package my.reposter

import dev.whyoleg.ktd.TelegramClient
import dev.whyoleg.ktd.api.TdApi.*
import dev.whyoleg.ktd.api.authentication.setAuthenticationPhoneNumber
import dev.whyoleg.ktd.api.check.checkAuthenticationCode
import dev.whyoleg.ktd.api.database.setDatabaseEncryptionKey
import dev.whyoleg.ktd.api.tdlib.setTdlibParameters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.mapNotNull

fun tdlibParameters(dbPath: String) = TdlibParameters(
    useTestDc = false,
    databaseDirectory = dbPath,
    filesDirectory = dbPath,
    useFileDatabase = true,
    useChatInfoDatabase = true,
    useMessageDatabase = true,
    apiId = 0,//provide id
    apiHash = "", //provide hash
    systemLanguageCode = "en",
    deviceModel = "Mobile",
    systemVersion = "1",
    applicationVersion = "1",
    enableStorageOptimizer = false,
    ignoreFileNames = false,
    useSecretChats = false
)

val TelegramClient.authorizationStateUpdates: Flow<AuthorizationState>
    get() = updates.filterIsInstance<UpdateAuthorizationState>().mapNotNull { it.authorizationState }

suspend fun TelegramClient.autoHandleAuthState(state: AuthorizationState, dbPath: String = "flow"): Boolean {
    when (state) {
        is AuthorizationStateWaitTdlibParameters -> setTdlibParameters(tdlibParameters(dbPath))
        is AuthorizationStateWaitPhoneNumber -> setAuthenticationPhoneNumber("+")
        is AuthorizationStateWaitEncryptionKey   -> setDatabaseEncryptionKey(ByteArray(DEFAULT_BUFFER_SIZE).apply { fill(1) })
        else                                     -> return false
    }
    return true
}

suspend fun TelegramClient.handlePhoneAuthorization(state: AuthorizationState, phone: String = "", code: String = ""): Boolean {
    when (state) {
        is AuthorizationStateWaitPhoneNumber -> {
            setAuthenticationPhoneNumber(
                phoneNumber = phone,
                settings = PhoneNumberAuthenticationSettings(
                    allowFlashCall = false,
                    isCurrentPhoneNumber = false,
                    allowSmsRetrieverApi = false
                )
            )
        }
        is AuthorizationStateWaitCode        -> checkAuthenticationCode(code)
        else                                 -> return false
    }
    return true
}

object AuthComplete : Throwable()

fun Flow<AuthorizationState>.onAuthReady(block: suspend () -> Unit = {}): Flow<AuthorizationState> = catch {
    if (it is AuthComplete) block() else throw it
}
