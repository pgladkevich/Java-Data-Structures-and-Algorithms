package lists;

/* NOTE: The file Utils.java contains some functions that may be useful
 * in testing your answers. */

/** HW #2, Problem #1. */

/** List problem.
 *  @author
 */
class Lists {

    /* B. */
    /** Return the list of lists formed by breaking up L into "natural runs":
     *  that is, maximal strictly ascending sublists, in the same order as
     *  the original.  For example, if L is (1, 3, 7, 5, 4, 6, 9, 10, 10, 11),
     *  then result is the four-item list
     *            ((1, 3, 7), (5), (4, 6, 9, 10), (10, 11)).
     *  Destructive: creates no new IntList items, and may modify the
     *  original list pointed to by L. */
    static IntListList naturalRuns(IntList L) {
        if (L == null) {
            return null;
        }
        else if (L.tail == null) {
            IntListList result = new IntListList();
            result.head = L;
            result.tail = null;
            return result;
        }
        else {
            IntListList result = new IntListList();
            IntList curr = L;
            result.head = curr;
            int val_to_beat = L.head;
            result.tail = null;
            IntListList ptr = new IntListList();

            while (L.tail != null) {
                if (val_to_beat >= L.tail.head) {
                    if (result.tail == null) {
                        ptr = ptr.list(L.tail);
                        result.tail = ptr;
                    }
                    else {
                        ptr.tail = ptr.list(L.tail);
                        ptr = ptr.tail;
                    }
                    val_to_beat = L.tail.head;
                    curr = L.tail;
                    L.tail = null;
                    L = curr;
                }
                else {
                    if (L.tail == null) {
                        L = L.tail;
                        continue;
                    }
                    val_to_beat = L.tail.head;
                    L = L.tail;
                    continue;
                }
            }
            return result;
        }
    }
}
