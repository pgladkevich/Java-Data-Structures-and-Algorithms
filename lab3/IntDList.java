import java.awt.dnd.DnDConstants;

/**
 * Scheme-like pairs that can be used to form a list of integers.
 *
 * @author P. N. Hilfinger; updated by Vivant Sakore (1/29/2020)
 */
public class IntDList {

    /**
     * First and last nodes of list.
     */
    protected DNode _front, _back;

    /**
     * An empty list.
     */
    public IntDList() {
        _front = _back = null;
    }

    /**
     * @param values the ints to be placed in the IntDList.
     */
    public IntDList(Integer... values) {
        _front = _back = null;
        for (int val : values) {
            insertBack(val);
        }
    }

    /**
     * @return The first value in this list.
     * Throws a NullPointerException if the list is empty.
     */
    public int getFront() {
        return _front._val;
    }

    /**
     * @return The last value in this list.
     * Throws a NullPointerException if the list is empty.
     */
    public int getBack() {
        return _back._val;
    }

    /**
     * @return The number of elements in this list.
     */
    public int size() {
        if (_front == null && _back == null) {
            return 0;
        }
        else if ((_front._prev == null) && (_front._next == null)){
            return 1;
        }
        else {
            int count = 1;
            DNode curr = _front;
            while (curr._next != null) {
                curr = curr._next;
                count += 1;
            }
            return count;
        }
    }

    /**
     * @param i index of element to return,
     *          where i = 0 returns the first element,
     *          i = 1 returns the second element,
     *          i = -1 returns the last element,
     *          i = -2 returns the second to last element, and so on.
     *          You can assume i will always be a valid index, i.e 0 <= i < size for positive indices
     *          and -size <= i <= -1 for negative indices.
     * @return The integer value at index i
     */
    public int get(int i) {
        if (i == 0) {
            return getFront();
        }
        else if (i == -1) {
            return getBack();
        }
        else if (i > 0) {
            DNode curr = _front;
            int j =0;
            while (j < i) {
                curr = curr._next;
                j +=1;
                if (j == i) {
                    return curr._val;
                }
            }
        }
        else {
            DNode curr = _back;
            int j=0;
            while (j > i) {
                j-=1;
                if (j==i) {
                    return curr._val;
                }
                curr = curr._prev;
            }
        }
        return 0;
    }

    /**
     * @param d value to be inserted in the front
     */
    public void insertFront(int d) {
        DNode d_n = new DNode(null, d, null);
        if (size() == 0) {
            _front = d_n;
            _back = d_n;
        }
        else {
            d_n._next = _front;
            _front._prev = d_n;
            _front = d_n;
        }
    }

    /**
     * @param d value to be inserted in the back
     */
    public void insertBack(int d) {
        DNode d_n = new DNode(null, d, null);
        if (size() == 0) {
            _front = d_n;
            _back = d_n;
        }
        else {
            d_n._prev = _back;
            _back._next = d_n;
            _back = d_n;
        }
    }

    /**
     * @param d     value to be inserted
     * @param index index at which the value should be inserted
     *              where index = 0 inserts at the front,
     *              index = 1 inserts at the second position,
     *              index = -1 inserts at the back,
     *              index = -2 inserts at the second to last position, and so on.
     *              You can assume index will always be a valid index,
     *              i.e 0 <= index <= size for positive indices (including insertions at front and back)
     *              and -(size+1) <= index <= -1 for negative indices (including insertions at front and back).
     */
    public void insertAtIndex(int d, int index) {
        if (index == 0 || index == -(size()+1)) {
            insertFront(d);
        }
        else if (index == -1 || size() < 2 || index == size()) {
            insertBack(d);
        }
        else if (index > 0) {
            DNode d_n = new DNode(null, d, null);
            DNode curr = _front;
            int j = 1;
            while (j < index) {
                curr = curr._next;
                j += 1;
            }
            d_n._prev = curr;
            d_n._next = curr._next;
            curr._next._prev = d_n;
            curr._next = d_n;
        }
        else {
            DNode d_n = new DNode(null, d,null);
            DNode curr = _back;
            int j=-1;
            while (j > index) {
                j-=1;
                curr = curr._prev;
            }
            d_n._prev = curr;
            d_n._next = curr._next;
            curr._next._prev = d_n;
            curr._next = d_n;
        }
    }

    /**
     * Removes the first item in the IntDList and returns it.
     *
     * @return the item that was deleted
     */
    public int deleteFront() {
        // FIXME: Implement this method and return correct value
        return 0;
    }

    /**
     * Removes the last item in the IntDList and returns it.
     *
     * @return the item that was deleted
     */
    public int deleteBack() {
        // FIXME: Implement this method and return correct value
        return 0;
    }

    /**
     * @param index index of element to be deleted,
     *          where index = 0 returns the first element,
     *          index = 1 will delete the second element,
     *          index = -1 will delete the last element,
     *          index = -2 will delete the second to last element, and so on.
     *          You can assume index will always be a valid index,
     *              i.e 0 <= index < size for positive indices (including deletions at front and back)
     *              and -size <= index <= -1 for negative indices (including deletions at front and back).
     * @return the item that was deleted
     */
    public int deleteAtIndex(int index) {
        // FIXME: Implement this method and return correct value
        return 0;
    }

    /**
     * @return a string representation of the IntDList in the form
     * [] (empty list) or [1, 2], etc.
     * Hint:
     * String a = "a";
     * a += "b";
     * System.out.println(a); //prints ab
     */
    public String toString() {
        // FIXME: Implement this method to return correct value
        return null;
    }

    /**
     * DNode is a "static nested class", because we're only using it inside
     * IntDList, so there's no need to put it outside (and "pollute the
     * namespace" with it. This is also referred to as encapsulation.
     * Look it up for more information!
     */
    static class DNode {
        /** Previous DNode. */
        protected DNode _prev;
        /** Next DNode. */
        protected DNode _next;
        /** Value contained in DNode. */
        protected int _val;

        /**
         * @param val the int to be placed in DNode.
         */
        protected DNode(int val) {
            this(null, val, null);
        }

        /**
         * @param prev previous DNode.
         * @param val  value to be stored in DNode.
         * @param next next DNode.
         */
        protected DNode(DNode prev, int val, DNode next) {
            _prev = prev;
            _val = val;
            _next = next;
        }
    }

}
