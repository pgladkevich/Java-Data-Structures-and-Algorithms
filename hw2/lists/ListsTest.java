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

    // It might initially seem daunting to try to set up
    // IntListList expected.
    //
    // There is an easy way to get the IntListList that you want in just
    // few lines of code! Make note of the IntListList.list method that
    // takes as input a 2D array.
    @Test
    public void naturalRunsTest() {
        Utils.readIntArray()
    }

    public static void main(String[] args) {
        System.exit(ucb.junit.textui.runClasses(ListsTest.class));
    }
}