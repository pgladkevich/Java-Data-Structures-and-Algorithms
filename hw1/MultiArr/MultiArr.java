/** Multidimensional array 
 *  @author Zoe Plaxco
 */

public class MultiArr {

    /**
    {{“hello”,"you",”world”} ,{“how”,”are”,”you”}} prints:
    Rows: 2
    Columns: 3
    
    {{1,3,4},{1},{5,6,7,8},{7,9}} prints:
    Rows: 4
    Columns: 4
    */
    public static void printRowAndCol(int[][] arr) {
        if (arr == null) {
            System.out.println("Null array passed in");;
        }

        int num_rows = arr.length;
        int num_cols = 0;
        for (int i = 0; i < arr.length; i+=1){
            if (arr[i].length > num_cols){
                num_cols = arr[i].length;
            }
            else {
                continue;
            }
        }
        System.out.println("Rows: " + num_rows + "\n" + "Columns: " + num_cols);
    } 

    /**
    @param arr: 2d array
    @return maximal value present anywhere in the 2d array
    */
    public static int maxValue(int[][] arr) {
        if (arr == null) {
            return 0;
        }

        int max = 0;
        for (int row=0; row < arr.length; row+=1) {
            for (int col=0; col < arr[row].length; col+=1) {
                if (row == 0 && col == 0) {
                    max = arr[row][col];
                }
                else if (max < arr[row][col]) {
                    max = arr[row][col];
                }
                else {
                    continue;
                }
            }
        }
        return max;
    }

    /**Return an array where each element is the sum of the 
    corresponding row of the 2d array*/
    public static int[] allRowSums(int[][] arr) {
        if (arr == null) {
            return null;
        }
        int [] result = new int[arr.length];

        for (int row = 0; row < arr.length; row +=1) {
            int sum = 0;
            for (int col = 0; col < arr[row].length; col +=1) {
                sum += arr[row][col];
                if (col == (arr[row].length -1)) {
                    result[row] = sum;
                }
            }
        }

        return result;
    }
}