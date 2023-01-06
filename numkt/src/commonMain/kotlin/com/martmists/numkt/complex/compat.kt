package com.martmists.numkt.complex

import com.martmists.commons.extensions.roundTo

operator fun Number.plus(other: Complex) = this.toComplex() + other
operator fun Number.minus(other: Complex) = this.toComplex() - other
operator fun Number.times(other: Complex) = other * this
operator fun Number.div(other: Complex) = this.toComplex() / other

fun Number.toComplex() = if (this is Complex) this else Complex(this.toDouble())
fun Complex.roundTo(precision: Int) = Complex(real.roundTo(precision), imag.roundTo(precision))
