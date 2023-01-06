package com.martmists.numkt.math

import com.martmists.numkt.E
import com.martmists.numkt.complex.Complex
import com.martmists.numkt.complex.*
import com.martmists.numkt.ndarray.NDArray
import com.martmists.unions.Type
import com.martmists.unions.UnionMethod
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sin

@UnionMethod(
    "nkpow",
    Type(Any::class), Type(Number::class),  // parameters
    Type(Any::class),  // return type
)
internal fun nkpow(x: Float, y: Complex): Complex {
    val len = nkpow(x, y.real)
    val phase = y.imag * nklog(x)
    return Complex(len * cos(phase), len * sin(phase))
}

@UnionMethod("nkpow")
internal fun nkpow(x: Float, y: Number): Float = x.pow(y.toFloat())

@UnionMethod("nkpow")
internal fun nkpow(x: Double, y: Complex): Complex {
    val len = nkpow(x, y.real)
    val phase = y.imag * nklog(x)
    return Complex(len * cos(phase), len * sin(phase))
}

@UnionMethod("nkpow")
internal fun nkpow(x: Double, y: Number): Double = x.pow(y.toDouble())

@UnionMethod("nkpow")
internal fun nkpow(x: Complex, y: Complex): Complex {
    val vabs = nkabs(x).toDouble()
    val at = angle(x).toDouble()
    val len = nkpow(vabs, y.real) / nkpow(E, y.imag * at)
    val phase = at * y.real + y.imag * nklog(vabs)
    return Complex(len * cos(phase), len * sin(phase))
}

@UnionMethod("nkpow")
internal fun nkpow(x: Complex, y: Number): Complex {
    val vabs = nkabs(x).toDouble()
    val at = angle(x).toDouble()
    val len = nkpow(vabs, y.toDouble())
    val phase = at * y.toDouble()
    return Complex(len * cos(phase), len * sin(phase))
}

@UnionMethod("nkpow")
internal fun nkpow(x: NDArray, y: Number): NDArray {
    return x.transform { nkpow(it, y) }
}
