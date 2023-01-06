package com.martmists.numkt.complex

import kotlin.math.roundToInt

class Complex(val real: Double, val imag: Double = 0.0): Number() {
    // Type conversion

    override fun toByte(): Byte {
        return toInt().toByte()
    }

    override fun toChar(): Char {
        return toInt().toChar()
    }

    override fun toDouble(): Double {
        return real
    }

    override fun toFloat(): Float {
        return toDouble().toFloat()
    }

    override fun toInt(): Int {
        return toDouble().toInt()
    }

    override fun toLong(): Long {
        return toInt().toLong()
    }

    override fun toShort(): Short {
        return toInt().toShort()
    }

    override fun toString(): String {
        val realString = if (real == real.roundToInt().toDouble()) "${real.roundToInt()}" else "$real"
        val imagString = if (imag == imag.roundToInt().toDouble()) "${imag.roundToInt()}j" else "${imag}j"

        return if (imag == 0.0) {
            realString
        } else if (real == 0.0) {
            imagString
        } else {
            "($realString+$imagString)"
        }
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is Complex -> real == other.real && imag == other.imag
            is Number -> real == other.toDouble() && imag == 0.0
            else -> false
        }
    }

    // Basic operators

    operator fun plus(other: Number): Complex {
        return Complex(real + other.toDouble(), imag)
    }

    operator fun plus(other: Complex): Complex {
        return Complex(real + other.real, imag + other.imag)
    }

    operator fun minus(other: Number): Complex {
        return Complex(real - other.toDouble(), imag)
    }

    operator fun minus(other: Complex): Complex {
        return Complex(real - other.real, imag - other.imag)
    }

    operator fun unaryMinus(): Complex {
        return Complex(-real, -imag)
    }

    operator fun times(other: Number): Complex {
        return Complex(real * other.toDouble(), imag * other.toDouble())
    }

    operator fun times(other: Complex): Complex {
        return Complex(real * other.real - imag * other.imag, real * other.imag + imag * other.real)
    }

    operator fun div(other: Number): Complex {
        return Complex(real / other.toDouble(), imag / other.toDouble())
    }

    operator fun div(other: Complex): Complex {
        val denom = other.real * other.real + other.imag * other.imag
        return Complex((real * other.real + imag * other.imag) / denom, (imag * other.real - real * other.imag) / denom)
    }

    operator fun rem(other: Number): Complex {
        return Complex(real % other.toDouble(), imag % other.toDouble())
    }

    operator fun rem(other: Complex): Complex {
        return Complex(real % other.real, imag % other.imag)
    }

    fun conjugate(): Complex {
        return Complex(real, -imag)
    }
}

infix fun Number.j(imag: Number) = Complex(this.toDouble(), imag.toDouble())
val Number.j: Complex
    get() = Complex(0.0, this.toDouble())
