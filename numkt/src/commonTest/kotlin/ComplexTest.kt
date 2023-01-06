import com.martmists.numkt.complex.j
import com.martmists.numkt.math.isClose
import com.martmists.numkt.math.nklog
import com.martmists.numkt.math.nkpow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ComplexTest {
    @Test
    fun testLog() {
        assertTrue {
            isClose(
                .34657359027997264 j 0.7853981633974483,
                nklog(1 j 1),
            )
        }
    }

    @Test
    fun testPow() {
        val x = 1 j 1
        val y = 1.2 j 3.2
        assertTrue {
            isClose(
                -0.056773912376423395 j 0.10886187325245357,
                nkpow(x, y),
            )
        }
    }
}
