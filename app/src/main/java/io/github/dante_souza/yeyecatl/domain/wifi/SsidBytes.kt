package io.github.dante_souza.yeyecatl.domain.wifi

class SsidBytes(bytes: ByteArray) {
    val bytes: ByteArray = bytes.copyOf()

    override fun equals(other: Any?): Boolean =
        other is SsidBytes && bytes.contentEquals(other.bytes)

    override fun hashCode(): Int =
        bytes.contentHashCode()
}
