package enigma;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;
import static enigma.TestUtils.*;

public class RotorTests {
    //    @Test(expected = IllegalArgumentException.class)
//    public void testNoAlphabet() {
//        Permutation p = getNewPermutation("",
//                getNewAlphabet(""));
//        if (p.size()==0) {
//            throw new IllegalArgumentException("No alphabet.");
//        }
//
//    }
//    @Test(expected = EnigmaException.class)
//    public void testNotInAlphabet() {
//        Permutation p = getNewPermutation("(ABCD)",
//                getNewAlphabet("ABC"));
//
//    }
//
//    @Test(expected = EnigmaException.class)
//    public void testInAlphabetTwice() {
//        Permutation p = getNewPermutation("(ABD)",
//                getNewAlphabet("ABBD"));
//
//    }
//    @Test(expected = EnigmaException.class)
//    public void testInCyclesTwice() {
//        Permutation p = getNewPermutation("(ABBD)",
//                getNewAlphabet("ABD"));
//
//    }
//    @Test(expected = IllegalArgumentException.class)
//    public void testInvalidLetter() {
//        Alphabet a = getNewAlphabet("A*");
//        Alphabet a1 = getNewAlphabet("A(");
//        Alphabet a2 = getNewAlphabet("A)");
//        if (a.contains('*') || a1.contains('(') || a2.contains(')')) {
//            throw new IllegalArgumentException("Alphabet can't contain *, (, " +
//                    "or ) characters");
//        }
//    }
//    @Test(expected = EnigmaException.class)
//    public void testInvalidCycleForm() {
//        Permutation p = getNewPermutation("((A))",
//                getNewAlphabet("AB"));
//    }
}
