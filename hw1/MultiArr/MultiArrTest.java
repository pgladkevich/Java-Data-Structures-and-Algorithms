import static org.junit.Assert.*;
import org.junit.Test;

public class MultiArrTest {

    @Test
    public void testPrintRowAndCol() {
//        System.out.println("Rows: " + 2 + "\n");
//        System.out.println("Columns: " + 3);
        int[][] inta_test = new int [][]{{1,3,4},{1},{5,6,7,8},{7,9}};
//        assertEquals("Rows: 2\n" +
//                "Columns: 3", MultiArr.printRowAndCol(inta_test).outContent.to());
        MultiArr.printRowAndCol(inta_test);
    }

    @Test
    public void testMaxValue() {
        int[][] inta_test = new int [][]{{1,3,4},{1},{5,6,7,8},{7,9}};
        assertEquals(9, MultiArr.maxValue(inta_test));

        int[][] intb_test = new int [][]{{1,4,0},{-200,3,8},{100,0}};
        assertEquals(100, MultiArr.maxValue(intb_test));
    }

    @Test
    public void testAllRowSums() {
        int[][] inta_test = new int [][]{{1,3,4},{1},{5,6,7,8},{7,9}};
        int[] inta_test_answ = new int []{8,1,26,16};
        assertTrue(java.util.Arrays.equals(inta_test_answ, MultiArr.allRowSums(inta_test)));

        int[][] intb_test = new int [][]{{1,4,0},{-200,3,8},{100,0}};
        int[] intb_test_answ = new int []{5,-189,100};
        assertTrue(java.util.Arrays.equals(intb_test_answ, MultiArr.allRowSums(intb_test)));
    }


    /* Run the unit tests in this file. */
    public static void main(String... args) {
        System.exit(ucb.junit.textui.runClasses(MultiArrTest.class));
    }
}
