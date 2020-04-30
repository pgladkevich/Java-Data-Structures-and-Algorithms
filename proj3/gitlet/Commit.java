package gitlet;

import java.io.Serializable;
import java.util.HashMap;

public class Commit implements Serializable {

    /** Create a new commit that contains a _message MSG, a _parent PRNT,
     * and _millitime that is a long representing the time in milliseconds since
     * UNIX epoch time (0 if Commit is the initial). This long will be the
     * positive difference of the current locale's time in milliseconds at the
     * moment the constructor is called. The value of _millitime is parsed and
     * formatted as needed. */
    public Commit(String msg) {
        _message = msg;
        _parent = null;
        _millitime = 0;
        _blobs = new HashMap<>();
    }
    /** Create a new commit that contains a _message MSG, a _parent PRNT,
     * and _millitime that is a long representing the time in milliseconds since
     * UNIX epoch time (0 if Commit is the initial). This long will be the
     * positive difference of the current locale's time in milliseconds at the
     * moment the constructor is called. The value of _millitime is parsed and
     * formatted as needed. */
    public Commit(String msg, String prnt, Commit current) {
        _message = msg;
        _parent = prnt;
        _millitime = System.currentTimeMillis();
        _blobs = current.get_blobs();
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
    /** Getter method for _parent */
    public String get_parent() {
        return _parent;
    }
    /** Remove method for _blobs */
    public void removeblob(String name) {
        _blobs.remove(name);
    }
    /** Add method for _blobs */
    public void addblob(String name, String SHA) {
        _blobs.put(name, SHA);
    }
    /** Check if the SHA provided matches the SHA corresponding to the name
     * provided. If it matches return true, otherwise false.  */
    public boolean checkMATCHES(String name, String SHA) {
        if (_blobs.get(name).compareTo(SHA) == 0) {
            return true;
        } else {
            return false;
        }
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
