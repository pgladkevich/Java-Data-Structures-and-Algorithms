/** Class that contains methods that act on integer arrays
*  @author Pavel Gladkevich
*/

public class Utils {

	/** Class that find the maximum of an integer array
	 * Either via a while loop or a for loop
	 * @param a
	 * @return greatest integer
	 */
	public static int max (int[] a) {
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

		public static boolean checkForI

		int len_a = a.length;

		for (int i = 0; i < len_a; i+=1) {
			if (a[i] == 0) {
				return true;
			}
			else if (a[i] == ) {

			}
			else if () {

			}
			else {
				return false;
			}
		}
		//return false;
	}

	public static boolean threeSumDistinct (int [] a) {
		return false;
	}
}