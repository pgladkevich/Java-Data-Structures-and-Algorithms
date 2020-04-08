import org.junit.Test;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Test of a BST-based String Set.
 * @author Pavel Gladkevich
 */
public class ECHashStringSetTest  {
    @Test
    public void test1() {
        ArrayList<ArrayList<String>> t = new ArrayList<>(5);
        while(t.size() < 5) t.add(new ArrayList<>());
        System.out.println(t.isEmpty());
        System.out.println(t.get(2).isEmpty());
        String s = "";
        int x = s.hashCode();
        System.out.println(x);
        t.get(2).add("");
        t.get(2).add("hi");
        System.out.println(t.get(2).get(1));
    }

    @Test
    public void testEmpty() {
        ECHashStringSet t = new ECHashStringSet();
        t.put("");
        assertTrue(t.contains(""));
    }

    @Test
    public void testBasic() {
        ECHashStringSet t = new ECHashStringSet();
        String[] word1 = {"f", "c", "a", "d", "e", "b"};
        for (String i : word1) {
            t.put(i);
        }
    }

    @Test
    public void testAsList() {
        ECHashStringSet t = new ECHashStringSet();
        String[] word1 = {"f", "c", "a", "d", "e", "b"};
        for (String i : word1) {
            t.put(i);
        }
        List<String> aL = t.asList();
        for (int i = 0; i < word1.length; i += 1) {
            System.out.println(aL.get(i));
        }
    }

    @Test
    public void testHmmm() {
        ECHashStringSet t = new ECHashStringSet();
        String[] word1 = {"f", "c", "a", "d", "e", "b",
                "fasfd", "casdfas", "aasdf", "dasfd", "easfd", "basdfasf", };
        for (String i : word1) {
            t.put(i);
        }
        List<String> aL = t.asList();
        for (int i = 0; i < word1.length; i += 1) {
            System.out.println(aL.get(i));
        }
    }
}
