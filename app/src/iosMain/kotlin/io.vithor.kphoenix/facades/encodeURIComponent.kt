package io.vithor.kphoenix.facades

/**
 * Encodes the passed String as UTF-8 using an algorithm that's compatible
 * with JavaScript's `encodeURIComponent` function. Returns
 * `null` if the String is `null`.
 *
 * @param value The String to be encoded
 * @return the encoded String
 */
actual fun encodeURIComponent(value: String): String {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

/**
 * Decodes the passed UTF-8 String using an algorithm that's compatible with
 * JavaScript's `decodeURIComponent` function. Returns
 * `null` if the String is `null`.
 *
 * @param value The UTF-8 encoded String to be decoded
 * @return the decoded String
 */
actual fun decodeURIComponent(value: String?): String? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}