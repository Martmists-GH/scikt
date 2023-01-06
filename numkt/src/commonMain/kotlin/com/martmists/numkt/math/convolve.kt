package com.martmists.numkt.math

import com.martmists.numkt.ndarray.NDArray

fun convolve(a: NDArray, v: NDArray, mode: String = "full"): NDArray {
    require(a.shape.size == 1) { "a must be 1-dimensional" }
    require(v.shape.size == 1) { "v must be 1-dimensional" }

    val n = a.shape[0]
    val m = v.shape[0]

    val s1 = if (n >= m) a else v
    val s2 = if (n >= m) v else a

    val calcSize = n + m - 1

    val out = when (mode) {
        "full" -> NDArray.zeros(calcSize)
        "same" -> NDArray.zeros(maxOf(n, m))
        "valid" -> NDArray.zeros(maxOf(n, m) - minOf(n, m) + 1)
        else -> throw IllegalArgumentException("mode must be one of 'full', 'valid', or 'same'")
    }

    val outOffset = when (mode) {
        "full" -> 0
        "same" -> (calcSize - minOf(n, m)) / 2
        "valid" -> calcSize - minOf(n, m)
        else -> throw IllegalArgumentException("mode must be one of 'full', 'valid', or 'same'")
    }
    val outSize = out.shape[0]

    for (i in 0 until s1.shape[0]) {
        for (j in 0 until s2.shape[0]) {
            val idx = i + j - outOffset
            if (idx < 0 || idx >= outSize) {
                continue
            }
            out[idx] += s1[i] * s2[j]
        }
    }

    return out
}
