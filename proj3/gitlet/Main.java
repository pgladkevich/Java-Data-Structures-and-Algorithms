package gitlet;

import com.sun.tools.corba.se.idl.Util;
import sun.applet.resources.MsgAppletViewer_zh_HK;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.util.Map.entry;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Pavel Gladkevich
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> ....
     *  If a user doesn't input any arguments, prints "Please enter a command."
     *  and exits with code 0.
     *  If a user inputs a command that doesn't exist, print the message
     *  "No command with that name exists." and exit.
     *  If a user inputs a command with the wrong number or format of operands,
     *  print the message "Incorrect operands." and exit.
     *  */
    public static void main(String... args) {
        // FILL THIS IN
        try {
            new Main(args);
        } catch (GitletException excp) {
            System.err.printf("%s%n", excp.getMessage());
            System.exit(0);
        }

    }

    /** Check ARGS and perform the necessary commands (see comment on main). */
    Main(String[] args) {
        if (args.length == 0) {
            throw Utils.error("Please enter a command.");
        }

        _cwd = new File(System.getProperty("user.dir"));
        _gitlet = Utils.join(_cwd, ".gitlet");
        _exists = _gitlet.exists() && _gitlet.isDirectory();
        _HEADF = Utils.join(_gitlet, "HEAD");
        _objects = Utils.join(_gitlet, "objects");
        _branches = Utils.join(_gitlet, "branches");
        _staging = Utils.join(_gitlet, "staging");
        _addition = Utils.join(_addition, "addition");
        _removal = Utils.join(_removal, "removal");
        _commits = Utils.join(_gitlet, "commits");

        switch (args[0]) {
            case "init":
                init(args);
                break;
            case "add":
                add(args);
                break;

            default:
                throw Utils.error("No command with that name exists.",
                        args[0]);
        }
    }

    /** init: If there is already a .gitlet directory present, abort. Otherwise,
     *  create a new .gitlet directory, the initial commit, HEAD file with
     *  branch “master” pointing to initial commit. Also create empty Objects
     *  directory, branches directory that contains SHA-1 ID of the initial
     *  commit from the master branch.
     *
     *  Additionally if ARGS contains anything other than just the init command,
     *  exit. If a Gitlet version control system already exists in the current
     *  working directory, print the error message "A Gitlet version-control
     *  system already exists in the current directory." */
    private void init(String[] args) {
        if (args.length > 1) {
            throw Utils.error("Incorrect operands.", args[0]);
        } else if (_exists) {
            throw Utils.error("A Gitlet version-control system already " +
                    "exists in the current directory.", args[0]);
        } else {
            _gitlet.mkdir();
            _objects.mkdir();
            _branches.mkdir();
            _staging.mkdir();
            _addition.mkdir();
            _removal.mkdir();
            _commits.mkdir();
            Commit initial = new Commit("initial commit", null);
            byte[] serialized = initial.serialize();
            String sha1 = Utils.sha1(serialized);
            Utils.writeContents(_HEADF,
                    Utils.join(_branches, "master"));
            Utils.writeContents(Utils.join(_branches, "master"), sha1);
            Utils.writeContents(Utils.join(_commits,sha1), serialized);
            _exists = true;
        }
    }

    private void add(String[] args) {
    }



    /** File object representing the current working directory ~. */
    private static File _cwd;
    /** File object representing the ~/.gitlet hidden directory. */
    private static File _gitlet;
    /** File object representing the ~/.gitlet/HEAD file location. */
    private static File _HEADF;
    /** HEAD stores filepath to the head of the active branch.
     * branches/</active branch> eg. ~/.gitlet/branches/master */
    private String _HEAD;
    /** File object representing the ~/.gitlet/objects directory. Objects
     * stores every blob that has ever been committed. Blob SHA-1 code is blob
     * file name & contents are serialized. */
    private static File _objects;
    /** File object representing the ~/.gitlet/branches directory. Branches
     *  stores a file for every branch. File name is branch name, and contents
     *  of each file is the SHA-1 ID of the head of the branch. */
    private static File _branches;
    /** File object representing the ~/.gitlet/staging directory. */
    private static File _staging;
    /** File object representing the ~/.gitlet/staging/addition directory. */
    private static File _addition;
    /** File object representing the ~/.gitlet/staging/removal directory. */
    private static File _removal;
    /** File object representing the ~/.gitlet/commits directory. */
    private static File _commits;

    /** Boolean representing if the .gitlet directory is present in _cwd. */
    private boolean _exists;

}
