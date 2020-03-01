package arrays;

import org.junit.Test;
import static org.junit.Assert.*;

/** Tests of the Arrays class
 *  @author Pavel Gladkevich
 */

public class ArraysTest {
    /** Test that catenate properly concatenates input array B onto
     * the end of input array B and returns the resulting array.
     */
    @Test
    public void catenateTest(){
        int[] A_null = new int[] {};
        int[] B_null = new int[] {};
        assert Utils.equals(A_null, arrays.Arrays.catenate(A_null,B_null));

        int[] A = new int[] {1,3,7};
        int[] B = new int[] {-1,3,8};
        int[] result = new int[] {1,3,7,-1,3,8};
        assert Utils.equals(A, arrays.Arrays.catenate(A,B_null));
        assert Utils.equals(B, arrays.Arrays.catenate(B,A_null));
        assert Utils.equals(result, arrays.Arrays.catenate(A,B));
    }

    @Test
    public void remove() {
        int[] A_null = new int[] {};
        int start = 0;
        int len = 1;
        assert Utils.equals(A_null, arrays.Arrays.remove(A_null, start, len));

        int[] A_one = new int[] {1};
        assert Utils.equals(A_null, arrays.Arrays.remove(A_one, start, len));

        int[] A = new int[] {1,2,3,5,13,56,7,9};
        len = 8;
        assert Utils.equals(A_null, arrays.Arrays.remove(A, start, len));

        start = 4;
        len = 4;
        int[] removed_a1 = Utils.subarray(A,0,4);
        assert Utils.equals(removed_a1, arrays.Arrays.remove(A, start,len));

        start =4;
        len = 2;
        int[] removed_a2 = arrays.Arrays.catenate(Utils.subarray(A,0,4),Utils.subarray(A,6,2));
        assert Utils.equals(removed_a2, arrays.Arrays.remove(A,4,2));
    }


    public static void main(String[] args) {
        System.exit(ucb.junit.textui.runClasses(ArraysTest.class));
    }
}
