package io.vithor

interface TransportFactory {

    operator fun invoke(endPoint: String): Transport?
}
