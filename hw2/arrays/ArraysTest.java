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



    public static void main(String[] args) {
        System.exit(ucb.junit.textui.runClasses(ArraysTest.class));
    }
}
