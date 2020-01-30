/** Class that contains methods that act on integer arrays
*  @author Pavel Gladkevich
*/

private class Utils {

	public static int Max (int [] a) {
		len_a = a.length ()
		if (len_a > 0) {
			if (len_a % 2 == 0) {
				int index = 0;
				greatest = 0
				while (index < len_a) {
					if (a[index] >= greatest) {
						greatest = a[index];
					}
					index += 1;
				}
			}
			else {
				for (int index = 0; index < len_a; index +=1) {
					if (a[index] >= greatest) {
						greatest = a[index];
					}
				}
				return greatest;

			}
		};
		else{
			return NullPointerException;
		}
	}

	public static boolean threeSum (int [] a) {

	}

	public static boolean threeSumDistinct (int [] a) {

	}
}