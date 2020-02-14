package lists;

import org.junit.Test;
import static org.junit.Assert.*;

/** Tests of the Lists class.
 *
 *  @author Pavel Gladkevich
 */

public class ListsTest {
    /** Test that the naturalRuns method properly breaks up the passed
     * intList into an intListList that contains n intLists,
     * each of which contains members strictly in ascending order.
     */

    @Test
    public void naturalRunsTest() {
        /* Some edge cases */
        assert Utils.equals(null, lists.Lists.naturalRuns(null));

        IntList edge_L1 =  IntList.list(1);
        IntListList ending_edge_L1 = new IntListList();
        int[][] ending_edge_L1_array = new int[][] {{1}};
        ending_edge_L1 = ending_edge_L1.list(ending_edge_L1_array);
        assert Utils.equals(ending_edge_L1, lists.Lists.naturalRuns(edge_L1));

        IntList starting_L1 =  IntList.list(1, 3, 7, 5, 4, 6, 9, 10, 10, 11);
        int[][] ending_L1_array = new int[][] {{1, 3, 7}, {5}, {4,6,9,10}, {10,11}};
        IntListList ending_L1 = IntListList.list(ending_L1_array);

        assert Utils.equals(ending_L1,lists.Lists.naturalRuns(starting_L1));
    }

    public static void main(String[] args) {
        System.exit(ucb.junit.textui.runClasses(ListsTest.class));
    }
}
