import org.junit.Test;
import static org.junit.Assert.*;

import ucb.junit.textui;

/** Tests for hw0. 
 *  @author Pavel Gladkevich
 */
public class Tester {

    /* Feel free to add your own tests.  For now, you can just follow
     * the pattern you see here.  We'll look into the details of JUnit
     * testing later.
     *
     * To actually run the tests, just use
     *      java Tester 
     * (after first compiling your files).
     *
     * DON'T put your HW0 solutions here!  Put them in a separate
     * class and figure out how to call them from here.  You'll have
     * to modify the calls to max, threeSum, and threeSumDistinct to
     * get them to work, but it's all good practice! */

    @Test
    public void maxTest() {
        System.out.println(Utils.max(new int[] { 0, -5, 2, 14, 10 }));
        System.out.println(Utils.max(new int[] { 0, -5, 2, 29, 36, 4}));
        System.out.println(Utils.max(new int[] {}));
    }

    @Test
    public void threeSumTest() {
        assertTrue(Utils.threeSum(new int[] { -6, 3, 10, 200 }));
        assertFalse(Utils.threeSum(new int[]{-6, 2, 5}));
        assertTrue(Utils.threeSum(new int[]{-6, 3, 10, 200}));
        assertTrue(Utils.threeSum(new int[]{8, 2, -1, 15}));
        assertTrue(Utils.threeSum(new int[]{8, 2, -1, -1, 15}));
        assertTrue(Utils.threeSum(new int[]{5, 1, 0, 3, 6}));

    }

    @Test
    public void threeSumDistinctTest() {
        assertFalse(Utils.threeSumDistinct(new int[] { -6, 3, 10, 200 }));
        assertTrue(Utils.threeSumDistinct(new int[]{-6, 2, 4}));
        assertFalse(Utils.threeSumDistinct(new int[]{-6, 2, 5}));
        assertFalse(Utils.threeSumDistinct(new int[]{-6, 3, 10, 200}));
        assertFalse(Utils.threeSumDistinct(new int[]{8, 2, -1, 15}));
        assertTrue(Utils.threeSumDistinct(new int[]{8, 2, -1, -1, 15}));
        assertFalse(Utils.threeSumDistinct(new int[]{5, 1, 0, 3, 6}));
    }

    public static void main(String[] unused) {
        textui.runClasses(Tester.class);
    }

}
