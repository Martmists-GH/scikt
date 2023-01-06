package com.martmists.numkt.ndarray

import com.martmists.commons.extensions.roundTo
import com.martmists.commons.functions.product
import com.martmists.numkt.complex.Complex
import com.martmists.numkt.complex.j
import com.martmists.numkt.complex.toComplex
import kotlin.math.abs
import kotlin.math.floor
import kotlin.random.Random

class NDArray(val shape: List<Int>, private val offset: Int = 0, strides: List<Int>? = null, data: Pair<DoubleArray, DoubleArray>? = null, initializer: ((Int) -> Number)? = null) {
    constructor(vararg shape: Int) : this(shape.toList())
    constructor(vararg shape: Int, initializer: (Int) -> Number) : this(shape.toList(), initializer = initializer)

    val strides = strides ?: let {
        val stride = mutableListOf<Int>()
        var strideSize = 1
        for (element in shape.asReversed()) {
            stride.add(strideSize)
            strideSize *= element
        }
        stride
    }.asReversed()
    private val real = data?.first ?: DoubleArray(shape.reduce { acc, i -> acc * i })
    private val imag = data?.second ?: DoubleArray(shape.reduce { acc, i -> acc * i })

    private val indices = product(
        *shape.zip(this.strides).map { (sh, st) -> (0 until sh).map { it * st } }.toTypedArray()
    ).map {
        it.sum()
    }.toList().toIntArray()
    private val indicesForTransform = indices.toSet().toIntArray()

    init {
        if (data == null && initializer != null) {
            for (i in real.indices) {
                val x = initializer(i).toComplex()
                real[i] = x.real
                imag[i] = x.imag
            }
        }
    }

    // Basic array operations

    private fun getArrayIndex(index: IntArray): Int {
        if (index.size != shape.size) throw IllegalArgumentException("Index ${index.toList()} does not match shape ${shape.toList()}")

        var arrayIndex = offset
        for (i in index.indices) {
            if (index[i] < 0 || index[i] >= shape[i]) throw IllegalArgumentException("Index ${index.toList()} is out of bounds for shape ${shape.toList()}")
            arrayIndex += index[i] * strides[i]
        }

        return arrayIndex
    }

    fun getAs1D(index: Int): Complex {
        val idx = offset + indices[index]
        return real[idx] j imag[idx]
    }

    operator fun get(vararg indices: Int): Complex {
        val idx = getArrayIndex(indices)
        return real[idx] j imag[idx]
    }

    operator fun get(vararg ranges: IntRange) : NDArray {
        val newShape = shape.toMutableList()
        val newStrides = strides.toMutableList()
        for (i in ranges.indices) {
            newShape[i] = ranges[i].last - ranges[i].first + 1
            newStrides[i] = strides[i] * ranges[i].step
        }
        return NDArray(newShape, offset + ranges.foldIndexed(0) { i, acc, range -> acc + range.first * strides[i] }, newStrides, data = real to imag)
    }

    operator fun set(vararg indices: Int, value: Number) {
        val idx = getArrayIndex(indices)
        val x = value.toComplex()
        real[idx] = x.real
        imag[idx] = x.imag
    }

    operator fun set(vararg ranges: IntRange, value: Number) {
        val x = value.toComplex()
        for (i in ranges.foldIndexed(0) { i, acc, range -> acc + range.first * strides[i] } until ranges.foldIndexed(0) { i, acc, range -> acc + range.last * strides[i] } step strides.min()) {
            real[i] = x.real
            imag[i] = x.imag
        }
    }

    // Transforms

    fun copy(): NDArray {
        return NDArray(shape) {
            val idx = indices[it]
            real[offset + idx] j imag[offset + idx]
        }
    }

    fun transform(transform: (Complex) -> Number): NDArray {
        return copy().also {
            it.transformInplace(transform)
        }
    }

    fun transformInplace(transform: (Complex) -> Number) {
        for (i in indicesForTransform) {
            val x = transform(real[offset + i] j imag[offset + i]).toComplex()
            real[offset + i] = x.real
            imag[offset + i] = x.imag
        }
    }

    fun transformIndexed(transform: (IntArray, Complex) -> Number): NDArray {
        return copy().also {
            it.transformIndexedInplace(transform)
        }
    }

    fun transformIndexedInplace(transform: (IntArray, Complex) -> Number) {
        if (indices.size != indicesForTransform.size) throw IllegalStateException("Cannot transform indexed in-place on a broadcast array, create a copy first")

        for (index in product(*shape.map { (0 until it).toList() }.toTypedArray())) {
            val arr = index.toIntArray()
            val i = getArrayIndex(arr)
            val x = transform(arr, real[i] j imag[i]).toComplex()
            real[i] = x.real
            imag[i] = x.imag
        }
    }

    internal fun broadcastTo(shape: List<Int>): NDArray {
        if (shape == this.shape) {
            return this
        }

        val currentShape = this.shape.toMutableList()
        val currentStrides = this.strides.toMutableList()

        while (currentShape.size < shape.size) {
            currentShape.add(0, 1)
            currentStrides.add(0, 0)
        }

        return NDArray(shape, offset, currentStrides, Pair(real, imag))
    }

    // Basic operators

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NDArray) return false

        if (shape != other.shape) return false
        for ((i, j) in indices.zip(other.indices)) {
            if (real[i + offset] != other.real[j + other.offset] || imag[i + offset] != other.imag[j + other.offset]) {
                return false
            }
        }
        return true
    }
    operator fun plus(other: Number): NDArray = transform { it + other }
    operator fun plusAssign(other: Number) = transformInplace { it + other }
    operator fun unaryPlus(): NDArray = this
    operator fun minus(other: Number): NDArray = transform { it - other }
    operator fun minusAssign(other: Number) = transformInplace { it - other }
    operator fun unaryMinus(): NDArray = transform { -it }
    operator fun times(other: Number): NDArray = transform { it * other }
    operator fun timesAssign(other: Number) = transformInplace { it * other }
    operator fun div(other: Number): NDArray = transform { it / other }
    operator fun divAssign(other: Number) = transformInplace { it / other }
    operator fun rem(other: Number): NDArray = transform { it % other }
    operator fun remAssign(other: Number) = transformInplace { it % other }
    operator fun inc(): NDArray = transform { it + 1 }
    operator fun dec(): NDArray = transform { it - 1 }

    operator fun contains(other: Number): Boolean {
        for (i in indicesForTransform) {
            if (real[offset + i] j imag[offset + i] == other) {
                return true
            }
        }
        return false
    }

    // Advanced operators

    operator fun get(check: (Complex) -> Boolean): NDArray = transform { if (check(it)) 1 else 0 }

    operator fun get(other: NDArray): NDArray {
        if (other.shape != shape) throw IllegalArgumentException("Shape mismatch: ${other.shape} != $shape")
        // Assume other is an array of 1.0 and 0.0
        return transformIndexed { idx, c ->
            when (other.get(*idx)) {
                0 j 0 -> 0
                1 j 0 -> c
                else -> throw IllegalArgumentException("Invalid mask value ${other.get(*idx)}, expected 0 or 1")
            }
        }
    }

    fun compare(other: NDArray): NDArray {
        if (other.shape != shape) throw IllegalArgumentException("Shape mismatch: ${other.shape} != $shape")
        return transformIndexed { idx, c ->
            if (c == other.get(*idx)) 1 j 0 else 0 j 0
        }
    }

    fun compare(other: Number) = this[{ it == other }]

    fun all(condition: (Complex) -> Boolean): Boolean {
        for (i in indicesForTransform) {
            if (!condition(real[offset + i] j imag[offset + i])) {
                return false
            }
        }
        return true
    }

    fun any(condition: (Complex) -> Boolean): Boolean {
        for (i in indicesForTransform) {
            if (condition(real[offset + i] j imag[offset + i])) {
                return true
            }
        }
        return false
    }

    fun none(condition: (Complex) -> Boolean) = !any(condition)

    // Numpy operations

    // Get a view over the current index on the given axis
    // This does NOT copy the data
    fun view(index: Int, axis: Int = 0): NDArray {
        if (axis < 0 || axis >= shape.size) throw IllegalArgumentException("Axis $axis is out of bounds [0, ${shape.lastIndex}]")

        val newDims = shape.toMutableList()
        val size = newDims.removeAt(axis)

        if (index < 0 || index >= size) throw IllegalArgumentException("Index $index is out of bounds [0, ${size - 1}]")

        val newStrides = strides.toMutableList()
        newStrides.removeAt(axis)

        return NDArray(newDims, offset + (index * strides[axis]), newStrides, Pair(real, imag))
    }

    fun transposed(): NDArray {
        val newShape = shape.reversed()
        val newStrides = strides.reversed()
        return NDArray(newShape, offset, newStrides, Pair(real, imag))
    }

    fun reshape(vararg shape: Int): NDArray {
        return reshape(shape.toList())
    }

    fun reshape(shape: List<Int>): NDArray {
        require(shape.fold(1) { acc, i -> acc * i } == this.shape.fold(1) { acc, i -> acc * i }) { "Shape mismatch: ${this.shape} cannot be reshaped to $shape" }

        return NDArray(shape) {
            val idx = indices[it]
            real[offset + idx] j imag[offset + idx]
        }
    }

    // Stringifying

    private data class SFP(
        val intSize: Int,
        val floatSize: Int,
        val imagIntSize: Int,
        val imagFloatSize: Int,
    )
    private fun getStringFormatParameters(precision: Int): SFP {
        var intSize = 0
        var floatSize = 0
        var imagIntSize = 0
        var imagFloatSize = 0

        for (i in real.indices) {
            val r = real[i]
            val j = imag[i]

            val realSign = if (r < 0) 1 else 0
            val realInt = floor(abs(r)).toInt()
            val realFloat = abs(r) - realInt
            val imagInt = floor(abs(j)).toInt()
            val imagFloat = abs(j) - imagInt

            if (realFloat != 0.0) {
                floatSize = maxOf(minOf(precision, realFloat.roundTo(precision).toString().length - 2), floatSize)
            }
            var s = realSign + realInt.toString().length
            if (s > intSize) {
                intSize = s
            }
            if (imagFloat != 0.0) {
                if (imagIntSize == 0) {
                    imagIntSize = 1
                }
                imagFloatSize = maxOf(minOf(precision, imagFloat.roundTo(precision).toString().length - 2), imagFloatSize)
            }
            if (imagInt != 0) {
                s = imagInt.toString().length
                if (s > imagIntSize) {
                    imagIntSize = s
                }
            }
        }

        return SFP(intSize, floatSize, imagIntSize, imagFloatSize)
    }
    private fun valueAsString(cmpl: Complex, params: SFP): String {
        val sb = StringBuilder()
        val realSign = if (cmpl.real < 0) "-" else ""
        val realInt = floor(abs(cmpl.real)).toInt()
        val realFloat = cmpl.real - realInt
        val imagInt = floor(abs(cmpl.imag)).toInt()
        val imagFloat = cmpl.imag - imagInt

        sb.append((realSign + realInt.toString()).padStart(params.intSize))
        if (params.floatSize > 0 || (params.imagIntSize > 0 && params.imagFloatSize > 0)) {
            sb.append('.')
            if (realFloat != 0.0) {
                sb.append(realFloat.toString().padEnd(params.floatSize, ' ').substring(2, params.floatSize + 2))
            } else {
                sb.append(" ".repeat(params.floatSize))
            }
        }
        if (params.imagIntSize > 0) {
            if (cmpl.imag < 0.0) {
                sb.append("-")
            } else {
                sb.append("+")
            }
            sb.append(imagInt.toString().padStart(params.imagIntSize))
            if (params.imagFloatSize > 0) {
                sb.append('.')
                if (imagFloat != 0.0) {
                    val imagFloatStr = imagFloat.roundTo(params.imagFloatSize).toString().substring(2) + "j"
                    sb.append(imagFloatStr.padEnd(params.imagFloatSize+1, ' '))
                } else {
                    sb.append("j" + " ".repeat(params.imagFloatSize))
                }
            } else {
                sb.append("j")
            }
        }
        return sb.toString()
    }
    private fun toString(maxDisplay: Int, params: SFP): String {
        val sb = StringBuilder()
        sb.append("[")

        val (separator, trunc, getter) = if (shape.size == 1) {
            Triple(", ", "...") { it: Int -> valueAsString(this[it], params) }
        } else {
            Triple("," + "\n".repeat(shape.size - 1), " ...") { it: Int -> this.view(it).toString(maxDisplay, params).prependIndent(" ").let { s -> if (it == 0) s.trimStart() else s } }
        }

        val dimSize = shape.first()

        if (dimSize <= maxDisplay) {
            for (i in 0 until dimSize) {
                sb.append(getter(i))
                if (i != dimSize - 1) sb.append(separator)
            }
        } else {
            val num = maxDisplay / 2
            for (i in 0 until num) {
                sb.append(getter(i))
                sb.append(separator)
            }
            sb.append(trunc)
            sb.append(separator)
            for (i in dimSize - num until dimSize) {
                sb.append(getter(i))
                if (i != dimSize - 1) sb.append(separator)
            }
        }

        sb.append("]")
        return sb.toString()
    }
    fun toString(maxDisplay: Int, precision: Int = 4): String {
        val sfp = getStringFormatParameters(precision)
        return toString(maxDisplay, sfp)
    }
    override fun toString(): String {
        val s = StringBuilder()
        s.append("array(")
        s.append(toString(6).prependIndent(" ".repeat(6)).trimStart())
        s.append(")")
        return s.toString().replace(Regex("\n\\s+\n"), "\n\n")
    }

    companion object {
        // Alternative construction methods

        fun zeros(vararg shape: Int) = NDArray(*shape) { 0 }
        fun zeros(shape: List<Int>) = NDArray(shape) { 0 }
        fun ones(vararg shape: Int) = NDArray(*shape) { 1 }
        fun ones(shape: List<Int>) = NDArray(shape) { 1 }

        // TODO: Support List<Int> maybe for easy 2D arrays?
        fun of(vararg values: Number) = NDArray(values.size) { values[it] }
        fun of(values: List<Number>) = NDArray(values.size) { values[it] }

        fun toeplitz(column: Collection<Number>, row: Collection<Number>): NDArray {
            require(column.elementAt(0) == row.elementAt(0)) { "First element of column and row must be equal" }

            val a = zeros(column.size, row.size)
            for (i in column.indices) {
                for (j in row.indices) {
                    if (i == 0) {
                        a[i, j] = row.elementAt(j)
                    } else if (j == 0) {
                        a[i, j] = column.elementAt(i)
                    } else {
                        a[i, j] = a[i - 1, j - 1]
                    }
                }
            }
            return a
        }

        fun identity(size: Int): NDArray {
            val a = zeros(size, size)
            for (i in 0 until size) {
                a[i, i] = 1
            }
            return a
        }

        fun random(seed: Long? = null, vararg shape: Int): NDArray {
            val random = if (seed != null) Random(seed) else Random.Default
            return NDArray(*shape) { random.nextDouble() }
        }
    }
}
