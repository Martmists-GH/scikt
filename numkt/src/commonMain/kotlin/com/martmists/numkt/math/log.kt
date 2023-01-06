package com.martmists.numkt.math

import com.martmists.numkt.E
import com.martmists.numkt.complex.*
import com.martmists.numkt.math.union.nklog
import com.martmists.numkt.ndarray.NDArray
import com.martmists.unions.Type
import com.martmists.unions.UnionMethod
import kotlin.math.atan2
import kotlin.math.log

@UnionMethod(
    "nklog",
    Type(Any::class, "T"),  // parameters
    Type(Any::class, "T"),  // return type
)
internal fun nklog(x: Float): Float = log(x, E.toFloat())

@UnionMethod("nklog")
internal fun nklog(x: Double): Double = log(x, E)

@UnionMethod("nklog")
internal fun nklog(x: Complex): Complex = log(nkabs(x).real, E) j angle(x)

@UnionMethod("nklog")
internal fun nklog(x: NDArray): NDArray = x.transform(::nklog)

@UnionMethod(
    "nklogBase",
    Type(Any::class), Type(Number::class),  // parameters
    Type(Any::class),  // return type
)
internal fun nklogBase(x: Float, base: Complex): Complex = nklog(x) / nklog(base)

@UnionMethod("nklogBase")
internal fun nklogBase(x: Float, base: Number): Float = log(x, base.toFloat())

@UnionMethod("nklogBase")
internal fun nklogBase(x: Double, base: Complex): Complex = nklog(x) / nklog(base)

@UnionMethod("nklogBase")
internal fun nklogBase(x: Double, base: Number): Double = log(x, base.toDouble())

@UnionMethod("nklogBase")
internal fun nklogBase(x: Complex, base: Number): Complex = nklog(x) / nklog(base)

@UnionMethod("nklogBase")
internal fun nklogBase(x: NDArray, base: Number): NDArray {
    val div = nklog(base)
    return x.transform { nklog(it) / div }
}
