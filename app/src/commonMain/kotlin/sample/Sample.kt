package sample

import io.vithor.kphoenix.facades.Platform

expect class Sample() {
    fun checkMe(): Int
}


fun hello(): String = "Hello from ${Platform.name}"

class Proxy {
    fun proxyHello() = hello()
}

fun main() {
    println(hello())
}