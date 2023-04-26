package moe.nea.sky.platform.core.apis.chat

import moe.nea.sky.platform.core.apis.chat.Text

interface ChatAPI {
    fun sendToPlayer(text: Text)

    // TODO: make use of brigadier for this one as well
    fun registerCommand(label: String, handler: (List<String>) -> Unit)
}