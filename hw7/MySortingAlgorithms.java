import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Note that every sorting algorithm takes in an argument k. The sorting 
 * algorithm should sort the array from index 0 to k. This argument could
 * be useful for some of your sorts.
 *
 * Class containing all the sorting algorithms from 61B to date.
 *
 * You may add any number instance variables and instance methods
 * to your Sorting Algorithm classes.
 *
 * You may also override the empty no-argument constructor, but please
 * only use the no-argument constructor for each of the Sorting
 * Algorithms, as that is what will be used for testing.
 *
 * Feel free to use any resources out there to write each sort,
 * including existing implementations on the web or from DSIJ.
 *
 * All implementations except Counting Sort adopted from Algorithms,
 * a textbook by Kevin Wayne and Bob Sedgewick. Their code does not
 * obey our style conventions.
 */
public class MySortingAlgorithms {

    /**
     * Java's Sorting Algorithm. Java uses Quicksort for ints.
     */
    public static class JavaSort implements SortingAlgorithm {
        @Override
        public void sort(int[] array, int k) {
            Arrays.sort(array, 0, k);
        }

        @Override
        public String toString() {
            return "Built-In Sort (uses quicksort for ints)";
        }
    }

    /** Insertion sorts the provided data. */
    public static class InsertionSort implements SortingAlgorithm {
        @Override
        public void sort(int[] array, int k) {
            for (int i = 0; i < k; i += 1) {
                int j = i;
                while (j > 0 && array[j] < array [j-1]) {
                    swap(array, j-1, j);
                    j = j-1;
                }
            }
        }

        @Override
        public String toString() {
            return "Insertion Sort";
        }
    }

    /**
     * Selection Sort for small K should be more efficient
     * than for larger K. You do not need to use a heap,
     * though if you want an extra challenge, feel free to
     * implement a heap based selection sort (i.e. heapsort).
     */
    public static class SelectionSort implements SortingAlgorithm {
        @Override
        public void sort(int[] array, int k) {
            for (int i = 0; i < k - 1; i += 1) {
                int min = i;
                for (int j = i + 1; j < k; j += 1) {
                    if (array[j] < array[min]) {
                        min = j;
                    }
                }
                if (min != i) {
                    swap(array, i, min);
                }
            }
        }

        @Override
        public String toString() {
            return "Selection Sort";
        }
    }

    /** Your mergesort implementation. An iterative merge
      * method is easier to write than a recursive merge method.
      * Note: I'm only talking about the merge operation here,
      * not the entire algorithm, which is easier to do recursively.
      */
    public static class MergeSort implements SortingAlgorithm {
        @Override
        public void sort(int[] array, int k) {
            if (array.length <= 1) {
                return;
            }
            int[] a = new int[k / 2];
            int[] b = new int[k - a.length];
            System.arraycopy(array, 0, a, 0 , a.length);
            System.arraycopy(array, a.length, b, 0, b.length);

            sort(a, a.length);
            sort(b, b.length);

            int[] c = merge(a, b);
            System.arraycopy(c,0, array, 0, c.length);
        }

        public int[] merge(int[] a, int[] b) {
            int[] c = new int[a.length + b.length];
            int aI = 0, bI = 0, cI = 0;
            while(aI < a.length && bI < b.length) {
                if (a[aI] < b[bI]) {
                    c[cI] = a[aI];
                    aI += 1;
                } else {
                    c[cI] = b[bI];
                    bI += 1;
                }
                cI += 1;
            }
            System.arraycopy(a, aI, c, cI, a.length - aI);
            System.arraycopy(b, bI, c, cI, b.length - bI);
            return c;
        }

        @Override
        public String toString() {
            return "Merge Sort";
        }
    }

    /**
     * Your Counting Sort implementation.
     * You should create a count array that is the
     * same size as the value of the max digit in the array.
     */
    public static class CountingSort implements SortingAlgorithm {
        @Override
        public void sort(int[] array, int k) {
            // FIXME: to be implemented
        }

        // may want to add additional methods

        @Override
        public String toString() {
            return "Counting Sort";
        }
    }

    /** Your Heapsort implementation.
     */
    public static class HeapSort implements SortingAlgorithm {
        @Override
        public void sort(int[] array, int k) {
            // FIXME
        }

        @Override
        public String toString() {
            return "Heap Sort";
        }
    }

    /** Your Quicksort implementation.
     */
    public static class QuickSort implements SortingAlgorithm {
        @Override
        public void sort(int[] array, int k) {
            // FIXME
        }

        @Override
        public String toString() {
            return "Quicksort";
        }
    }

    /* For radix sorts, treat the integers as strings of x-bit numbers.  For
     * example, if you take x to be 2, then the least significant digit of
     * 25 (= 11001 in binary) would be 1 (01), the next least would be 2 (10)
     * and the third least would be 1.  The rest would be 0.  You can even take
     * x to be 1 and sort one bit at a time.  It might be interesting to see
     * how the times compare for various values of x. */

    /**
     * LSD Sort implementation.
     */
    public static class LSDSort implements SortingAlgorithm {
        @Override
        public void sort(int[] a, int k) {
            int[] array = new int[k];
            System.arraycopy(a,0, array,0, k);
            List<Integer>[] bins = new ArrayList[10];
            for (int i = 0; i < bins.length; i++) {
                bins[i] = new ArrayList<>();
            }
            boolean done = false;
            int tmp = 0, divisor = 1;
            while (!done) {
                done = true;
                for (int i : array) {
                    tmp = i / divisor;
                    bins[tmp % 10].add(i);
                    if (done && tmp > 0) {
                        done = false;
                    }
                }
                int aI = 0;
                for (int b = 0; b < 10; b += 1) {
                    for (int i : bins[b]) {
                        array[aI++] = i;
                    }
                    bins[b].clear();
                }
                divisor *= 10;
            }
            System.arraycopy(array,0, a, 0, k);

        }

        @Override
        public String toString() {
            return "LSD Sort";
        }
    }

    /**
     * MSD Sort implementation.
     */
    public static class MSDSort implements SortingAlgorithm {
        @Override
        public void sort(int[] a, int k) {
            // FIXME
        }

        @Override
        public String toString() {
            return "MSD Sort";
        }
    }

    /** Exchange A[I] and A[J]. */
    private static void swap(int[] a, int i, int j) {
        int swap = a[i];
        a[i] = a[j];
        a[j] = swap;
    }

}
