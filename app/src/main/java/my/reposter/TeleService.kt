package my.reposter

import dev.whyoleg.ktd.Telegram
import dev.whyoleg.ktd.api.TdApi
import dev.whyoleg.ktd.api.chat.chat
import dev.whyoleg.ktd.api.chat.getChat
import dev.whyoleg.ktd.api.message.getChatHistory
import dev.whyoleg.ktd.api.message.sendMessage
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

object TeleService {

    val tg = Telegram()
    val client = tg.client()

    suspend fun getChats(limit: Int): List<Chat> {
        val chatIds = client.chat(TdApi.GetChats(limit = limit)).chatIds.toTypedArray()
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
        val messages = client.getChatHistory(chatId = chatId, fromMessageId = messageId)
        return messages.messages
    }

    suspend fun sendMessage(chatId: Long, message: TdApi.Message) {
        val inputMessage = when (val content = message.content) {
            is TdApi.MessageText -> TdApi.InputMessageText(content.text)
            is TdApi.MessageDocument -> {
                val inputFile = TdApi.InputFileRemote(content.document.document.remote.uniqueId)
                val thumbnailFile = TdApi.InputFileRemote(content.document.thumbnail?.photo?.remote?.uniqueId ?: "")
                val thumbnail = TdApi.InputThumbnail(thumbnailFile)
                TdApi.InputMessageDocument(inputFile, thumbnail, content.caption)
            }
            else -> return
        }
        client.sendMessage(chatId = chatId, inputMessageContent = inputMessage)
    }

    @InternalCoroutinesApi
    suspend fun auth(dbPath: String) {
        client.authorizationStateUpdates
            .onEach {
                when {
                    client.autoHandleAuthState(it, dbPath)      -> Unit
                    client.handlePhoneAuthorization(it) -> Unit
                    it is TdApi.AuthorizationStateReady -> throw AuthComplete
                }
            }
            .onAuthReady { println("Authorization completed") }
            .collect()
    }
}
