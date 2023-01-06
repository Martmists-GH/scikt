package com.martmists.numkt.ndarray

fun broadcastShape(a: List<Int>, b: List<Int>): List<Int>? {
    if (a == b) {
        return a
    }

    val shapeA = a.toMutableList()
    val shapeB = b.toMutableList()

    while (shapeA.size < shapeB.size) shapeA.add(0, 1)
    while (shapeB.size < shapeA.size) shapeB.add(0, 1)

    val new = mutableListOf<Int>()
    for ((s1, s2) in shapeA.zip(shapeB)) {
        if (s1 == s2 || s1 == 1 || s2 == 1) {
            new.add(maxOf(s1, s2))
        } else {
            return null
        }
    }

    return new
}

fun broadcast(a: NDArray, b: NDArray): Pair<NDArray, NDArray> {
    val targetShape = broadcastShape(a.shape, b.shape) ?: throw IllegalArgumentException("Cannot broadcast shapes ${a.shape} and ${b.shape}")
    return a.broadcastTo(targetShape) to b.broadcastTo(targetShape)
}

fun broadcastTo(a: NDArray, shape: List<Int>): NDArray {
    val targetShape = broadcastShape(a.shape, shape) ?: throw IllegalArgumentException("Cannot broadcast shape ${a.shape} to $shape")
    return a.broadcastTo(targetShape)
}
