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
				// System.out.println("Hello");
				return greatest;
			}
		}
		return 0;
	}

	/** Class that determines three integers (not neccesarily distinct) in a[] whose sum is zero.
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

	public static boolean threeSumDistinct (int [] a) {
		return false;
	}

	private static boolean halfInverse (int x, int [] a){
		int y = -1 * (x/2);
		return inList(y, a);
	}

	private static boolean sumInverse (int x, int [] a) {
		int first_two;
		for (int i=0; i < a.length; i+=1){
			first_two = x + a[i];
			if (inList(-first_two, a)) {
				return true;
			}
		}
		return false;
	}

	private static boolean inList (int look_for, int [] a){
		for (int i=0; i < a.length; i+=1){
			if (a[i] == look_for){
				return true;
			}
		}
		return false;
	}
}
