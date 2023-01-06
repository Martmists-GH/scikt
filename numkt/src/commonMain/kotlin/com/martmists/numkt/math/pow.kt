package com.martmists.numkt.math

import com.martmists.numkt.E
import com.martmists.numkt.complex.Complex
import com.martmists.numkt.complex.toComplex
import com.martmists.numkt.ndarray.NDArray
import com.martmists.unions.Type
import com.martmists.unions.UnionMethod
import kotlin.math.pow

// TODO: Support complex numbers as exponent
@UnionMethod(
    "nkpow",
    Type(Any::class, "T"), Type(Number::class),  // parameters
    Type(Any::class, "T"),  // return type
)
internal fun nkpow(x: Float, y: Number): Float = x.pow(y.toFloat())

@UnionMethod("nkpow")
internal fun nkpow(x: Double, y: Number): Double = x.pow(y.toDouble())

@UnionMethod("nkpow")
internal fun nkpow(x: Complex, y: Number): Complex {
    val r = nkabs(x).toDouble()
    val theta = angle(x).toDouble()
    val c = y.toComplex()
    return Complex(r.pow(c.real) * E.pow(theta * c.imag), theta * c.real)
}

@UnionMethod("nkpow")
internal fun nkpow(x: NDArray, y: Number): NDArray = x.transform { nkpow(it, y) }
