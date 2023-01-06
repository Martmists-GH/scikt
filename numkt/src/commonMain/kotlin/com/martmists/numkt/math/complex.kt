package com.martmists.numkt.math

import com.martmists.numkt.complex.Complex
import com.martmists.numkt.ndarray.NDArray
import com.martmists.unions.Type
import com.martmists.unions.UnionMethod
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.sqrt

@UnionMethod(
    "angle",
    Type(Any::class, "T"),  // parameters
    Type(Any::class, "T"),  // return type
)
internal fun angle(x: Complex): Complex = Complex(atan2(x.imag, x.real))

@UnionMethod("angle")
internal fun angle(x: NDArray): NDArray = x.transform(::angle)

@UnionMethod("angle")
internal fun angle(x: Float): Float = atan2(x, 0f)

@UnionMethod("angle")
internal fun angle(x: Double): Double = atan2(x, 0.0)

@UnionMethod(
    "nkabs",
    Type(Any::class, "T"),  // parameters
    Type(Any::class, "T"),  // return type
)
internal fun nkabs(x: Float): Float = abs(x)

@UnionMethod("nkabs")
internal fun nkabs(x: Double): Double = abs(x)

@UnionMethod("nkabs")
internal fun nkabs(x: Complex): Complex = Complex(hypot(x.real, x.imag))

@UnionMethod("nkabs")
internal fun nkabs(x: NDArray): NDArray = x.transform(::nkabs)
