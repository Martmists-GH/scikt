package com.martmists.numkt.math

import com.martmists.numkt.complex.Complex
import com.martmists.numkt.ndarray.NDArray
import kotlin.math.abs

fun isClose(a: Float, b: Float, rtol: Float = 1e-5f, atol: Float = 1e-8f): Boolean {
    return abs(a - b) <= (atol + rtol * abs(b))
}

fun isClose(a: Double, b: Double, rtol: Double = 1e-5, atol: Double = 1e-8): Boolean {
    return abs(a - b) <= (atol + rtol * abs(b))
}

fun isClose(a: Complex, b: Complex, rtol: Double = 1e-5, atol: Double = 1e-8): Boolean {
    return isClose(a.real, b.real, rtol, atol) && isClose(a.imag, b.imag, rtol, atol)
}

fun isClose(a: NDArray, b: NDArray, rtol: Double = 1e-5, atol: Double = 1e-8): NDArray {
    require(a.shape == b.shape) { "Arrays must have the same shape" }
    return NDArray(a.shape) { if (isClose(a.getAs1D(it), b.getAs1D(it), rtol, atol)) 1 else 0 }
}
