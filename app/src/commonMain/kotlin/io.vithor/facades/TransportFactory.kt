package io.vithor.facades

interface TransportFactory {
    val path: String
    operator fun invoke(endPoint: String, longpollerTimeout: Long): Transport?
}
