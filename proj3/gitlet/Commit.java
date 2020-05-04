package gitlet;

import java.io.Serializable;
import java.util.HashMap;

/** Commit class for use in gitlet.
 * @author Pavel Gladkevich
 * */
public class Commit implements Serializable {

    /** Create a new commit that contains a _message MSG, a _parent PRNT,
     * and _millitime that is a long representing the time in milliseconds since
     * UNIX epoch time (0 if Commit is the initial). This long will be the
     * positive difference of the current locale's time in milliseconds at the
     * moment the constructor is called. The value of _millitime is parsed and
     * formatted as needed.
     *
     * This constructor will create the initial commit, which will have _parent
     * and _secondparent = null */
    public Commit(String msg) {
        _message = msg;
        _parent = null;
        _secondparent = null;
        _millitime = 0;
        _blobs = new HashMap<>();
    }
    /** See comment for Commit(String msg).
     *
     * This constructor will create a non-merge commit, which will have _parent
     * set to the passed in SHA string PRNT that corresponds to the commit
     * CURRENT. The blob values will be copied and commit message will be set
     * to MSG. _secondparent will be set to null again.  */
    public Commit(String msg, String prnt, Commit current) {
        _message = msg;
        _parent = prnt;
        _secondparent = null;
        _millitime = System.currentTimeMillis();
        _blobs = current.getblobs();
    }
    /** See comments for the other Commit constructors..
     *
     * This constructor will create a merge commit, which will have _parent
     * set to the passed in SHA string PRNT that corresponds to the commit
     * CURRENT. The blob values will be copied and commit message will be set
     * to MSG. _secondparent will be set to passed in SHA string of the given
     * branch's head commit SPRNT.  */
    public Commit(String msg, String prnt, String sprnt, Commit current) {
        _message = msg;
        _parent = prnt;
        _secondparent = sprnt;
        _millitime = System.currentTimeMillis();
        _blobs = current.getblobs();
    }


    /** Serialize the contents of the commit and return a byte[] representing
     * the contents of this array. */
    public byte[] serialize() {
        return Utils.serialize(this);
    }
    /** Getter method for _blobs.
     * @return blobs */
    public HashMap<String, String> getblobs() {
        return _blobs;
    }
    /** Getter method for _message.
     * @return message */
    public String getmessage() {
        return _message;
    }
    /** Getter method for _timestamp.
     * @return millitime */
    public long getmillitime() {
        return _millitime;
    }
    /** Getter method for _parent.
     * @return parent */
    public String getparent() {
        return _parent;
    }
    /** Getter method for _secondparent.
     * @return secondparent */
    public String getsecondparent() {
        return _secondparent;
    }
    /** Remove method for _blobs using NAME. */
    public void removeblob(String name) {
        _blobs.remove(name);
    }
    /** Add method for _blobs using NAME and SHA. */
    public void addblob(String name, String sha) {
        _blobs.put(name, sha);
    }
    /** Check if the SHA provided matches the SHA corresponding to the name
     * provided. If it matches return true, otherwise false. Uses NAME.  */
    public boolean checkMATCHES(String name, String sha) {
        return _blobs.get(name).compareTo(sha) == 0;
    }

    /** HashMap<String,String> of file names mapped to SHA1 blob hash values. */
    private HashMap<String, String> _blobs;
    /** String that contains the message of the commit. */
    private String _message;
    /** Parent - the parent commit of the commit object. SHA1 string of parent
     commit. */
    private String _parent;
    /** Second Parent - the second parent commit of a merge commit object. If
     * this is a merge commit it will be set to the SHA1 string of the given
     * branch's head commit. If this is not a merge commit it will be null
     * by default. */
    private String _secondparent;
    /** Long representing time in milliseconds at which the commit was
     * created. Assigned by the constructor.*/
    private long _millitime;
}
