package com.martmists.numkt.complex

import com.martmists.commons.extensions.roundTo

fun Number.plus(other: Complex) = Complex(this.toDouble() + other.real, other.imag)
fun Number.minus(other: Complex) = Complex(this.toDouble() - other.real, -other.imag)
fun Number.times(other: Complex) = other * this
fun Number.div(other: Complex) = Complex(this.toDouble()) / other

fun Number.toComplex() = if (this is Complex) this else Complex(this.toDouble())
fun Complex.roundTo(precision: Int) = Complex(real.roundTo(precision), imag.roundTo(precision))
