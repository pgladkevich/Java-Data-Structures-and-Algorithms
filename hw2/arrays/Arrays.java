package arrays;

/* NOTE: The file Arrays/Utils.java contains some functions that may be useful
 * in testing your answers. */

/** HW #2 */

/** Array utilities.
 *  @author
 */
class Arrays {

    /* C1. */
    /** Returns a new array consisting of the elements of A followed by the
     *  the elements of B. */
    static int[] catenate(int[] A, int[] B) {
        int[] result = new int[A.length + B.length];
        if (A.length == 0 ) {
            return B;
        }
        else if (B.length == 0) {
            return A;
        }
        else {
            int last_A_index = 0;
            for (int i = 0; i < A.length; i+=1) {
                result[i] = A[i];
                last_A_index += 1;
            }
            for (int j = 0; j < B.length; j+=1) {
                result[last_A_index +j] = B[j];
            }
            return result;
        }
    }

    /* C2. */
    /** Returns the array formed by removing LEN items from A,
     *  beginning with item #START. */
    static int[] remove(int[] A, int start, int len) {
        if (A.length == 0) {
            return A;
        }
        if (len == A.length && start == 0) {
            int[] A_null = new int[] {};
            return A_null;
        }
        else {
            if (A.length - start == len) {
                return Utils.subarray(A,0,start);
            }
            else {
                int[] removed = arrays.Arrays.catenate(
                        Utils.subarray(A,0,start),
                        Utils.subarray(A,start+len,A.length - start - len)
                );
                return removed;
            }
        }
    }

    /* C3. */
    /** Returns the array of arrays formed by breaking up A into
     *  maximal ascending lists, without reordering.
     *  For example, if A is {1, 3, 7, 5, 4, 6, 9, 10}, then
     *  returns the three-element array
     *  {{1, 3, 7}, {5}, {4, 6, 9, 10}}. */
    static int[][] naturalRuns(int[] A) {
        /* *Replace this body with the solution. */
        return null;
    }
}
