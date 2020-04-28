package gitlet;

import java.io.Serializable;
import java.util.HashMap;

public class Commit implements Serializable {

    /** Create a new commit that contains a _message MSG, a _parent PRNT,
     * and _millitime that is a long representing the time in milliseconds since
     * UNIX epoch time (0 if Commit is the initial). This long will be the
     * positive difference of the current locale's time in milliseconds at the
     * moment the constructor is called. The value of _millitime is parsed and
     * formatted as needed.
     *
     * String pattern = "EEEEE, MMMMM dd, yyyy, HH:mm:ss Z";
     * String initTIME = "Thursday, January 01, 1970, 00:00:00";
     * SimpleDateFormat formatter = new SimpleDateFormat(pattern);
     * formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
     * Thursday, January 1st, 1970, 00:00:00            */
    public Commit(String msg, String prnt) {
        _message = msg;
        _parent = prnt;

        if (_parent == null) {
            _millitime = 0;
        } else {
            _millitime = System.currentTimeMillis();
        }
    }
    /** Serialize the contents of the commit and return a byte[] representing
     * the contents of this array. */
    public byte[] serialize() {
        return Utils.serialize(this);
    }

    /** Getter method for _blobs */
    public HashMap<String, String> get_blobs() {
        return _blobs;
    }
    /** Getter method for _message */
    public String get_message() {
        return _message;
    }
    /** Getter method for _timestamp */
    public long get_millitime() {
        return _millitime;
    }

    /** HashMap<String,String> of file names mapped to SHA1 blob hash values */
    private HashMap<String, String> _blobs;
    /** String that contains the message of the commit. */
    private String _message;
    /** Parent - the parent commit of the commit object. SHA1 string of parent
     commits. (2 possible for merge commits) */
    private String _parent;
    /** Long representing time in milliseconds at which the commit was
     * created. Assigned by the constructor.*/
    private long _millitime;
}
