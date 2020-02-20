import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;

/** String translation.
 *  @author Pavel Gladkevich
 */
public class Translate {
    /** This method should return the String S, but with all characters that
     *  occur in FROM changed to the corresponding characters in TO.
     *  FROM and TO must have the same length.
     *  NOTE: You must use your TrReader to achieve this. */
    static String translate(String S, String from, String to) {
        /* NOTE: The try {...} catch is a technicality to keep Java happy. */
        char[] buffer = new char[S.length()];
        try {
            StringReader sr = new StringReader(S);
            TrReader tr = new TrReader(sr,from,to);
            tr.read(buffer, 0, S.length());
            return Arrays.toString(buffer);
        } catch (IOException e) {
            return null;
        }
    }
    /*
       REMINDER: translate must
      a. Be non-recursive
      b. Contain only 'new' operations, and ONE other method call, and no
         other kinds of statement (other than return).
      c. Use only the library classes String, and any classes with names
         ending with "Reader" (see online java documentation).
    */
    public static void main(String[] args) {
        System.out.println(Translate.translate ("hello, I am the autograder", "hIthr", "HiTHR"));
        System.out.println(Translate.translate("", "", ""));
    }
}
