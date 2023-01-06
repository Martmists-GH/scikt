package com.martmists.numkt.complex

import kotlin.math.hypot
import kotlin.math.roundToInt

class Complex(val real: Double, val imag: Double = 0.0) : Number() {
    // Type conversion

    override fun toByte(): Byte {
        return toInt().toByte()
    }

    override fun toChar(): Char {
        return toInt().toChar()
    }

    override fun toDouble(): Double {
        return hypot(real, imag)
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
        return if (other is Complex) {
            Complex(real + other.real, imag + other.imag)
        } else {
            Complex(real + other.toDouble(), imag)
        }
    }

    operator fun minus(other: Number): Complex {
        return if (other is Complex) {
            Complex(real - other.real, imag - other.imag)
        } else {
            Complex(real - other.toDouble(), imag)
        }
    }

    operator fun unaryMinus(): Complex {
        return Complex(-real, -imag)
    }

    operator fun times(other: Number): Complex {
        return if (other is Complex) {
            Complex(real * other.real - imag * other.imag, real * other.imag + imag * other.real)
        } else {
            Complex(real * other.toDouble(), imag * other.toDouble())
        }
    }

    operator fun div(other: Number): Complex {
        return if (other is Complex) {
            val denom = other.real * other.real + other.imag * other.imag
            Complex((real * other.real + imag * other.imag) / denom, (imag * other.real - real * other.imag) / denom)
        } else {
            Complex(real / other.toDouble(), imag / other.toDouble())
        }
    }

    operator fun rem(other: Number): Complex {
        if (imag != 0.0 || (other is Complex && other.imag != 0.0)) {
            throw UnsupportedOperationException("Complex numbers cannot be used in remainder operations")
        }
        return (real % other.toDouble()).toComplex()
    }

    fun conjugate(): Complex {
        return Complex(real, -imag)
    }
}

infix fun Number.j(imag: Number) = Complex(this.toDouble(), imag.toDouble())
val Number.j: Complex
    get() = Complex(0.0, this.toDouble())
