import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

public class CompoundInterestTest {

    @Test
    public void testNumYears() {
        /** Sample assert statement for comparing integers.

        assertEquals(0, 0); */
        assertEquals(30, CompoundInterest.numYears(2050));
        assertEquals(0, CompoundInterest.numYears(2020));
    }

    @Test
    public void testFutureValue() {
        double tolerance = 0.01;
        assertEquals(12.544, CompoundInterest.futureValue(10,12,2022), tolerance);
        assertEquals(10, CompoundInterest.futureValue(10,12, 2020), tolerance);
    }

    @Test
    public void testFutureValueReal() {
        double tolerance = 0.01;
        assertEquals(11.8026496, CompoundInterest.futureValueReal(10,12,2022, 3.0), tolerance);
        assertEquals(10, CompoundInterest.futureValueReal(10,12, 2020, 3.0), tolerance);
    }


    @Test
    public void testTotalSavings() {
        double tolerance = 0.01;
        assertEquals(16550, CompoundInterest.totalSavings(5000,2022,10), tolerance);
        assertEquals(5000, CompoundInterest.totalSavings(10,2020, 10), tolerance);
    }

    @Test
    public void testTotalSavingsReal() {
        double tolerance = 0.01;
        assertEquals(15571.895, CompoundInterest.totalSavingsReal(5000,2022,10, 3), tolerance);
        assertEquals(5000, CompoundInterest.totalSavingsReal(10,2020, 10, 3), tolerance);
    }


    /* Run the unit tests in this file. */
    public static void main(String... args) {
        System.exit(ucb.junit.textui.runClasses(CompoundInterestTest.class));
    }
}
