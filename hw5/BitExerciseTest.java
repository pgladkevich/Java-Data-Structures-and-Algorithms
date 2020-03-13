import org.junit.Test;
import static org.junit.Assert.*;

/** Tests of BitExercise
 *  @author Zoe Plaxco
 */
public class BitExerciseTest {

    @Test
    public void testLastBit() {
        int four = BitExercise.lastBit(100);
        assertEquals(4, four);
        assertEquals(1, BitExercise.lastBit(1));
        assertEquals(1, BitExercise.lastBit(-1));
        assertEquals(2, BitExercise.lastBit(-2));
        assertEquals(2, BitExercise.lastBit(10));
        assertEquals(0, BitExercise.lastBit(0));
    }

    @Test
    public void testPowerOfTwo() {
        boolean powOfTwo = BitExercise.powerOfTwo(32);
        assertTrue(powOfTwo);
        // 00010000
        // 11110000
        // 10
        // 10

        boolean notPower = BitExercise.powerOfTwo(7);
        assertFalse(notPower);
        // 000000111
        // 111111001

        boolean notPower2 = BitExercise.powerOfTwo(25);
        assertFalse(notPower2);
        assertFalse(BitExercise.powerOfTwo(0));

    }

    @Test
    public void testAbsolute() {
        int hundred = BitExercise.absolute(100);
        assertEquals(100, hundred);
        int negative = BitExercise.absolute(-100);
        assertEquals(100, negative);
        int zero = BitExercise.absolute(0);
        assertEquals(0,zero);
    }

    @Test
    public void testisBitOn() {
        assertFalse(BitExercise.isBitIOn(2, 0));
        assertTrue(BitExercise.isBitIOn(2, 1));
        assertTrue(BitExercise.isBitIOn(7,2));
        assertFalse(BitExercise.isBitIOn(8,2));
        assertTrue(BitExercise.isBitIOn(8,3));
    }

    @Test
    public void testturnBitIOn() {
        assertEquals(3,BitExercise.turnBitIOn(1,1));
        assertEquals(1,BitExercise.turnBitIOn(1,0));
        assertEquals(9,BitExercise.turnBitIOn(8,0));
    }

    public static void main(String[] args) {
        System.exit(ucb.junit.textui.runClasses(BitExerciseTest.class));
    }
}

