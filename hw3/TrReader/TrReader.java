import java.io.Reader;
import java.io.IOException;

/** Translating Reader: a stream that is a translation of an
 *  existing reader.
 *  @author Pavel Gladkevich
 */
public class TrReader extends Reader {
    /** A new TrReader that produces the stream of characters produced
     *  by STR, converting all characters that occur in FROM to the
     *  corresponding characters in TO.  That is, change occurrences of
     *  FROM.charAt(i) to TO.charAt(i), for all i, leaving other characters
     *  in STR unchanged.  FROM and TO must have the same length. */
    public TrReader(Reader str, String from, String to) {
        this._str = str;
        this._from = from;
        this._to = to;
    }

    public int read(char[] cbuf, int off, int len) throws IOException {
        int index = off;
        if (len == 0) {
            return 0;
        }
        while (index < (len+off)) {
            int c = _str.read();
            if (c == -1) {
                return -1;
            }
            if (_from.contains(Character.toString((char)c))) {
                int i = _from.indexOf(c);
                cbuf[index] = _to.charAt(i);
            }
            else {
                cbuf[index] = (char) c;
            }
            index +=1;
        }

        return index - off;
    }

    public void close() {
        _str = null;
    }

    private Reader _str;
    private String _from;
    private String _to;
    private int _off;
    private int _len;
}
