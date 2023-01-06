import com.martmists.numkt.PI
import com.martmists.numkt.complex.j
import com.martmists.numkt.math.convolve
import com.martmists.numkt.ndarray.NDArray
import kotlin.test.Test
import kotlin.test.assertEquals

class NDArrayTest {
    @Test
    fun testShapeStrides() {
        val mat = NDArray(2, 3, 4)
        assertEquals(listOf(2, 3, 4), mat.shape)
        assertEquals(listOf(12, 4, 1), mat.strides)
    }

    @Test
    fun testStringInt() {
        val mat = NDArray(2, 3, 4) { it }
        assertEquals(
            "array([[[ 0,  1,  2,  3],\n" +
            "        [ 4,  5,  6,  7],\n" +
            "        [ 8,  9, 10, 11]],\n" +
            "\n" +
            "       [[12, 13, 14, 15],\n" +
            "        [16, 17, 18, 19],\n" +
            "        [20, 21, 22, 23]]])",
            mat.toString(),
        )
    }

    @Test
    fun testStringComplex() {
        val mat = NDArray(2, 3, 4) { it j it }
        assertEquals(
            "array([[[ 0+ 0j,  1+ 1j,  2+ 2j,  3+ 3j],\n" +
            "        [ 4+ 4j,  5+ 5j,  6+ 6j,  7+ 7j],\n" +
            "        [ 8+ 8j,  9+ 9j, 10+10j, 11+11j]],\n" +
            "\n" +
            "       [[12+12j, 13+13j, 14+14j, 15+15j],\n" +
            "        [16+16j, 17+17j, 18+18j, 19+19j],\n" +
            "        [20+20j, 21+21j, 22+22j, 23+23j]]])",
            mat.toString(),
        )
    }

    @Test
    fun testStringPrecision() {
        val mat = NDArray(2, 3, 4) { it*PI j it/PI }
        assertEquals(
            "array([[[ 0.    +0.j    ,  3.1415+0.3183j,  6.2831+0.6366j,  9.4247+0.9549j],\n" +
            "        [12.5663+1.2732j, 15.7079+1.5915j, 18.8495+1.9099j, 21.9911+2.2282j],\n" +
            "        [25.1327+2.5465j, 28.2743+2.8648j, 31.4159+3.1831j, 34.5575+3.5014j]],\n" +
            "\n" +
            "       [[37.6991+3.8197j, 40.8407+4.138j , 43.9822+4.4563j, 47.1238+4.7746j],\n" +
            "        [50.2654+5.093j , 53.4070+5.4113j, 56.5486+5.7296j, 59.6902+6.0479j],\n" +
            "        [62.8318+6.3662j, 65.9734+6.6845j, 69.1150+7.0028j, 72.2566+7.3211j]]])",
            mat.toString(),
        )
    }

    @Test
    fun testStringTruncation() {
        val mat = NDArray(100, 100) { it }
        assertEquals(
            "array([[   0,    1,    2, ...,   97,   98,   99],\n" +
            "       [ 100,  101,  102, ...,  197,  198,  199],\n" +
            "       [ 200,  201,  202, ...,  297,  298,  299],\n" +
            "       ...,\n" +
            "       [9700, 9701, 9702, ..., 9797, 9798, 9799],\n" +
            "       [9800, 9801, 9802, ..., 9897, 9898, 9899],\n" +
            "       [9900, 9901, 9902, ..., 9997, 9998, 9999]])",
            mat.toString(),
        )
    }

    @Test
    fun testConvolve() {
        val a = NDArray.of(1, 2, 3)
        val v = NDArray.of(0, 1, 0.5)
        assertEquals(NDArray.of(0, 1, 2.5, 4, 1.5), convolve(a, v))
        assertEquals(NDArray.of(1, 2.5, 4), convolve(a, v, "same"))
        assertEquals(NDArray.of(2.5), convolve(a, v, "valid"))
    }

    @Test
    fun testRanges() {
        val a = NDArray(4, 4) { it }
        val view = a[1..2, 1..2]
        assertEquals(NDArray.of(5, 6, 9, 10).reshape(2, 2), view)
    }

    @Test
    fun testToeplitz() {
        val c = listOf(1, 2, 3, 4)
        val r = listOf(1, 1 j 1, 1 j 2, 1 j 3)
        val toeplitz = NDArray.toeplitz(c, r)
        assertEquals(
            NDArray.of(
                1, 1 j 1, 1 j 2, 1 j 3,
                2, 1, 1 j 1, 1 j 2,
                3, 2, 1, 1 j 1,
                4, 3, 2, 1
            ).reshape(4, 4),
            toeplitz
        )
    }
}
