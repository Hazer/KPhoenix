package io.vithor.kphoenix.facades

import kotlin.reflect.KClass

actual object DefaultJson : JSON {
    override fun <T> stringify(data: T): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any> parse(rawPayload: String, type: KClass<T>): T? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}