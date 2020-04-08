import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Test of a BST-based String Set.
 * @author Pavel Gladkevich
 */
public class BSTStringSetTest  {

    @Test
    public void testEmpty() {
        BSTStringSet t = new BSTStringSet();
        t.put("");
        assertTrue(t.contains(""));
    }

    @Test
    public void testBasic() {
        BSTStringSet t = new BSTStringSet();
        String[] word1 = {"f", "c", "a", "d", "e", "b"};
        for (String i : word1) {
            t.put(i);
        }
    }

    @Test
    public void testAsList() {
        BSTStringSet t = new BSTStringSet();
        String[] word1 = {"f", "c", "a", "d", "e", "b"};
        for (String i : word1) {
            t.put(i);
        }
        String[] word1S = {"a", "b", "c", "d", "e", "f"};
        List<String> aL = t.asList();
        for (int i = 0; i < word1.length; i += 1) {
            assertEquals(word1S[i], aL.get(i));
        }
    }

    @Test
    public void testIterator() {
        BSTStringSet t = new BSTStringSet();
        String[] word1 = {"f", "c", "a", "d", "e", "b"};
        for (String i : word1) {
            t.put(i);
        }
        for (Iterator<String> i = t.iterator("b", "e");
             i.hasNext();) {
            System.out.println(i.next());
        }
    }
    @Test
    public void testIterator2() {
        BSTStringSet t = new BSTStringSet();
        String[] word1 = {"f", "c", "a", "d", "e", "b"};
        for (String i : word1) {
            t.put(i);
        }
        for (Iterator<String> i = t.iterator("a", "e");
             i.hasNext();) {
            System.out.println(i.next());
        }
    }
    @Test
    public void testIterator3() {
        BSTStringSet t = new BSTStringSet();
        String[] word1 = {"acasdcs", "ccsadcsd", "asdfasdf", "safdsadfd",
                "asdfsfe", "basfsdfa"};
        for (String i : word1) {
            t.put(i);
        }
        for (Iterator<String> i = t.iterator("a", "z");
             i.hasNext();) {
            System.out.println(i.next());
        }
    }
    @Test
    public void testIterator4() {
        BSTStringSet t = new BSTStringSet();
        String[] word1 = {"a", "f", "b", "d", "c", "e"};
        for (String i : word1) {
            t.put(i);
        }
        for (Iterator<String> i = t.iterator("b", "e");
             i.hasNext();) {
            System.out.println(i.next());
        }
    }
}
