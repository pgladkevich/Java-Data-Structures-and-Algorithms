/** Class that contains methods that act on integer arrays
*  @author Pavel Gladkevich
*/

public class Utils {

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

	public static boolean threeSum (int [] a) {
		return false;
	}

	public static boolean threeSumDistinct (int [] a) {
		return false;
	}
}