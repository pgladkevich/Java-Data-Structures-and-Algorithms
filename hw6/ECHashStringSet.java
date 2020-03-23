import com.sun.xml.internal.xsom.impl.scd.Iterators;

import java.util.ArrayList;
import java.util.List;

/** A set of String values.
 *  @author Pavel Gladkevich
 */
class ECHashStringSet implements StringSet {

    /** Creates a new ArrayList size 5 of empty ArrayLists. */
    public ECHashStringSet() {
        threshold = 5;
        bucketcount = 5;
        buckets = new ArrayList<>(5);
        elements = 0;
    }

    @Override
    public void put(String s) {
        // FIXME
    }

    /** Creates a new ArrayList size 5 of empty ArrayLists. */
    public int hashcodeHELPER(String s) {
        if (s.hashCode() < 0) {
            return (s.hashCode() & 0x7fffffff) % bucketcount;
        } else {
            return  s.hashCode() % bucketcount;
        }
    }

    @Override
    public boolean contains(String s) {
        int h = hashcodeHELPER(s);
        if (buckets.get(h).isEmpty() || !buckets.get(h).contains(s)) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public List<String> asList() {
        return null; // FIXME
    }

    /** ArrayList storing the strings in ArrayLists sorted by hashcode. */
    private ArrayList<ArrayList<String>> buckets;

    /** Count of the number of buckets, starts at five. */
    private int bucketcount;

    /** Count of the number of elements, starts at zero. */
    private int elements;

    /** Ccurrent computed value of the load factor. Is equal to the number of
     * elements divded by the number of buckets. */
    private int factor;

    /** If the load factor variable 'factor' exceeds the threshold we need to
     * rehash the Hash with the new number of buckets. */
    private int threshold;


}
