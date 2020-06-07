package my.reposter

import dev.whyoleg.ktd.Telegram
import dev.whyoleg.ktd.api.TdApi
import dev.whyoleg.ktd.api.chat.chat
import dev.whyoleg.ktd.api.chat.getChat
import dev.whyoleg.ktd.api.check.checkAuthenticationCode
import dev.whyoleg.ktd.api.check.checkAuthenticationPassword
import dev.whyoleg.ktd.api.log.setLogVerbosityLevel
import dev.whyoleg.ktd.api.message.getChatHistory
import dev.whyoleg.ktd.api.message.sendMessage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object TeleService {

    val tag = "reposter.log"

    val tg = Telegram()
    val client = tg.client().apply {
        GlobalScope.launch { setLogVerbosityLevel(1) }
    }

    suspend fun getChats(limit: Int): List<Chat> {
        val chatIds = client.chat(TdApi.GetChats(limit = limit,
            chatList = TdApi.ChatListMain(),
            offsetOrder = Long.MAX_VALUE
        )).chatIds.toTypedArray()
        return coroutineScope {
            chatIds.map { chatId ->
                async {
                    client.getChat(chatId)
                }
            }
        }
            .awaitAll()
            .map {
                Chat(it.title, it.id, false)
            }
    }

    suspend fun getMessages(chatId: Long, messageId: Long = 0L): Array<TdApi.Message> {
        val messages = client.getChatHistory(chatId = chatId, fromMessageId = messageId, limit = 100, offset = -99)
        return messages.messages
    }

    suspend fun sendMessage(chatId: Long, message: TdApi.Message) {
        val inputMessage = when (val content = message.content) {
            is TdApi.MessageText -> TdApi.InputMessageText(content.text)
            is TdApi.MessagePhoto -> {
                val photo = content.photo.sizes[0]
                val thumbnail = TdApi.InputThumbnail(TdApi.InputFileRemote(photo.photo.remote.id!!), photo.width, photo.height)
                TdApi.InputMessagePhoto(
                    photo = TdApi.InputFileRemote(photo.photo.remote.id!!),
                    caption = content.caption,
                    thumbnail = thumbnail,
                    height = photo.height,
                    width = photo.width
                )
            }
            is TdApi.MessageDocument -> {
                val inputFile = TdApi.InputFileRemote(content.document.document.remote.id!!)
                val thumbnailFile = TdApi.InputFileRemote(content.document.thumbnail?.photo?.remote?.uniqueId ?: "")
                val thumbnail = TdApi.InputThumbnail(thumbnailFile)
                TdApi.InputMessageDocument(inputFile, thumbnail, content.caption)
            }
            else -> return
        }
        client.sendMessage(chatId = chatId, inputMessageContent = inputMessage)
    }

    suspend fun checkCode(code: String) = client.checkAuthenticationCode(code)
    suspend fun checkPassword(password: String) = client.checkAuthenticationPassword(password)

    suspend fun auth(dbPath: String): Flow<State> {
        return client.authorizationStateUpdates
            .map {
                when {
                    client.autoHandleAuthState(it, dbPath)      -> State.SETUP
                    client.handlePhoneAuthorization(it, SecretSettings.PHONE) -> State.SETUP
                    it is TdApi.AuthorizationStateWaitCode -> State.CODE
                    it is TdApi.AuthorizationStateWaitPassword -> State.PASSWORD
                    it is TdApi.AuthorizationStateReady -> State.AUTHORIZED
                    else -> State.SETUP
                }
            }
    }
}

enum class State { SETUP, CODE, PASSWORD, AUTHORIZED }
