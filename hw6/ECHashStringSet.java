import java.util.ArrayList;
import java.util.List;

/** A set of String values.
 *  @author Pavel Gladkevich
 */
class ECHashStringSet implements StringSet {

    /** Creates a new ArrayList size 5 of empty ArrayLists. */
    public ECHashStringSet() {
        threshold = 5;
        buckets = new ArrayList<>(5);
        while(buckets.size() < 5) buckets.add(new ArrayList<>());
        bucketcount = 5;
        elements = 0;
    }

    @Override
    public void put(String s) {
        if (!contains(s)) {
            elements += 1;
            factor = elements/bucketcount;
            int h = hashcodeHELPER(s);
            if (factor <= 5) {
                buckets.get(h).add(s);
            } else {
                bucketcount *= 2;
                ArrayList<ArrayList<String>> newbuckets =
                        new ArrayList<>(bucketcount);
                while(newbuckets.size() < bucketcount)
                    newbuckets.add(new ArrayList<>());
                for (ArrayList<String> a : buckets) {
                    for (String b : a) {
                        int c = hashcodeHELPER(b);
                        newbuckets.get(c).add(b);
                    }
                }
                newbuckets.get(h).add(s);
                buckets = newbuckets;
            }
        }
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
        ArrayList<String> aL = new ArrayList<>();
        for (ArrayList<String> a : buckets) {
            aL.addAll(a);
        }
        return aL;
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
