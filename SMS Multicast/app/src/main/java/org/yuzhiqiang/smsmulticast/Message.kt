package org.yuzhiqiang.smsmulticast

import java.io.Serializable
import java.util.*

class Message(var destination: String, var content: String) : Serializable {
    private val uuid: UUID
    var status: Int = 0

    val isSentOut: Boolean
        get() = status > 0

    init {
        this.uuid = UUID.randomUUID()
        this.status = NEW
    }

    override fun equals(o: Any?): Boolean {
        return (o as Message).uuid == this.uuid
    }

    companion object {
        val NEW = 0
        val PENDING = 1
        val SENDING = 2
        val SENT = 3
        val FAILED = -1
    }
}
