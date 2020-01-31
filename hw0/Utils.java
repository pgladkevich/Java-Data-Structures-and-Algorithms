import java.util.Arrays;

/** Class that contains methods that act on integer arrays
*  @author Pavel Gladkevich
*/

public class Utils {

	/** Class that find the maximum of an integer array
	 * Either via a while loop or a for loop
	 * @param a
	 * @return greatest integer
	 */
	public static int max (int [] a) {
		int len_a = a.length;
		if (len_a > 0) {
			int greatest = 0;
			if (len_a % 2 == 0) {
				int index = 0;
				while (index < len_a) {
					if (a[index] >= greatest) {
						greatest = a[index];
					}
					index += 1;
				}
				return greatest;
			}
			else {
				for (int index = 0; index < len_a; index +=1) {
					if (a[index] >= greatest) {
						greatest = a[index];
					}
				}
				return greatest;
			}
		}
		return 0;
	}

	/** Class that determines three integers (not necessarily distinct) in a[] whose sum is zero.
	 *
	 * @param a
	 * @return boolean
	 */
	public static boolean threeSum (int [] a) {

		int len_a = a.length;

		for (int i = 0; i < len_a; i+=1) {
			/* Return true if 0 is in the array because 0+0+0 = 0 */
			if (a[i] == 0) {
				return true;
			}
			/* Return true if for x=a[i] there exists y =a[j]= -1(a[i])/2 since x+2y=0 */
			else if (a[i] % 2 == 0) {
				if (halfInverse(a[i], a)) {
					return true;
				}
			}
			/* Return true if a[i] = a[j]+a[k] */
			else if (sumInverse(a[i], a)) {
				return true;
			}
		}
		/* All solutions considered so must be false otherwise */
		return false;
	}

	/** Class that determines three distinct integers in a[] whose sum is zero.
	 *
	 * @param a
	 * @return boolean
	 */
	public static boolean threeSumDistinct (int [] a) {
		int [] first_removed;
		int [] second_removed;
		/* Create first loop to select the first element */
		for (int i=0; i < a.length; i+=1){
			int total = a[i];
			first_removed = removeElement(i, a);
			/* Create the second loop to select the second element */
			for (int j=0; j < first_removed.length; j+=1){
				total += first_removed[j];
				second_removed = removeElement(j, first_removed);
				/* After adding the first two elements check for inverse */
				if (inArray(-total, second_removed)){
					return true;
				}
				total -= first_removed[j];
			}
		}
		return false;
	}

	/* Helper class to find the negative of half of an int */
	private static boolean halfInverse (int x, int [] a){
		int y = -1 * (x/2);
		return inArray(y, a);
	}

	/* Helper class to return another array with an element removed */
	private static int[] removeElement (int position, int [] a) {
		int [] one_removed = new int[a.length - 1];
		for (int i=0, k=0; i < a.length; i += 1){
			if (i == position){
				continue;
			}
			one_removed[k] = a[i];
			k += 1;
		}
		return one_removed;
	}

	/* Helper class to find the negative of two ints in a list */
	private static boolean sumInverse (int x, int [] a) {
		int first_two;
		for (int i=0; i < a.length; i+=1){
			first_two = x + a[i];
			if (inArray(-first_two, a)) {
				return true;
			}
		}
		return false;
	}

	/* Helper class to find an int in a list */
	private static boolean inArray (int look_for, int [] a){
		for (int i=0; i < a.length; i+=1){
			if (a[i] == look_for){
				return true;
			}
		}
		return false;
	}
}
