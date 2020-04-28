package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

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
        _HEAD = Utils.join(_gitlet, "HEAD");
        _objects = Utils.join(_gitlet, "objects");
        _branches = Utils.join(_gitlet, "branches");
        _staging = Utils.join(_gitlet, "staging");
        _addition = Utils.join(_staging, "addition");
        _removal = Utils.join(_staging, "removal");
        _commits = Utils.join(_gitlet, "commits");

        switch (args[0]) {
            case "init":
                init(args);
                break;
            case "add":
                add(args);
                break;
            case "commit":
                commit(args);
                break;
            case "log":
                log(args);
                break;
            case "checkout":
                checkout(args);
                break;

            default:
                throw Utils.error("No command with that name exists.",
                        args[0]);
        }
    }

    /** init: If a Gitlet version control system already exists in the current
     *  directory or args>1, abort. Otherwise, create a new .gitlet directory,
     *  the initial commit, HEAD file with branch “master” pointing to initial
     *  commit. Also create empty Objects directory, branches directory that
     *  contains SHA-1 ID of the initial commit from the master branch, _staging
     *  directory and _addition & _removal subdirectories, as well as, _commits
     *  directory. This concludes the setup of the .gitlet persistence
     *  architecture. */
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
            Commit initial = new Commit("initial commit");
            byte[] serialized = initial.serialize();
            String sha1 = Utils.sha1(serialized);
            updateHEAD("master");
            updateBRANCH("master", sha1);
            updateCOMMIT(sha1, serialized);
            _exists = true;
        }
    }


    /** If file does not exist in the current working directory, abort.
     * Otherwise, add a copy of it to the addition subdirectory of staging. If
     * the file is already in the addition area, and the contents of the
     * file are the same as the version from the last commit, remove it from
     * addition subdirectory. If the file was staged for removal, remove it
     * from the removal subdirectory. If the file is already in the staging
     * area, but its contents are not the same as the version in the previous
     * commit (or that commit did not contain this file), override the old file
     * with the new contents. */
    private void add(String[] args) {
        checkGITLET(args);
        if (args.length != 2 || (args[1] == null)) {
            throw Utils.error("Incorrect operands.", args[0]);
        }
        _nameFILE = args[1];
        File source = Utils.join(_cwd, _nameFILE);
        if (!source.exists()) {
            throw Utils.error("File does not exist.", args[0]);
        }
        setCURRENT();
        setBLOBS();
        boolean inaddition = Utils.plainFilenamesIn(_addition)
                .contains(_nameFILE);
        boolean inremoval = Utils.plainFilenamesIn(_removal)
                .contains(_nameFILE);
        File dest = Utils.join(_addition, _nameFILE);
        if (!inaddition) {
            try {
                Files.copy(source.toPath(), dest.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            byte[] contents = Utils.serialize(source);
            String SHA1 = Utils.sha1(contents);
            Utils.restrictedDelete(dest);
            if (_blobs.containsKey(_nameFILE) &&
                    _blobs.get(_nameFILE).compareTo(SHA1) != 0) {
                try {
                    Files.copy(source.toPath(), dest.toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (inremoval) {
            File remove = Utils.join(_removal, _nameFILE);
            Utils.restrictedDelete(remove);
        }

    }

    /** If staging area is empty or message is empty, abort.
     * Create a new commit whose contents are by default the same as the
     * current commit. Add any files in the staging area that are not in the
     * current commit. For files in the addition subdirectory that are
     * contained in the current commit, compare hash values of files in new and
     * current commit, adding any files whose hash value is not in the current
     * commit. For files in the removal subdirectory remove the files from
     * being tracked in the next commit. Clear the staging area and add new
     * commit to branch, updating the HEAD. */
    private void commit(String[] args) {
        checkGITLET(args);
        List<String> remove = Utils.plainFilenamesIn(_removal);
        List<String> addition = Utils.plainFilenamesIn(_addition);
        if (args.length != 2) {
            throw Utils.error("Incorrect operands.", args[0]);
        } else if (args[1] == null) {
            throw Utils.error("Please enter a commit message.", args[0]);
        } else if (remove == null && addition == null) {
            throw Utils.error("No changes added to the commit.", args[0]);
        }
        setCURRENT();
        Commit commit = new Commit(args[1], _currSHA, _current);
        updateCURRENT(commit);
        if (remove != null) {
            for(String name : remove) {
                _current.removeblob(name);
                File rfile = Utils.join(_removal, name);
                rfile.delete();
            }
        }
        setBLOBS();
        if (addition != null) {
            for (String name : addition) {
                File pot = Utils.join(_addition, name);
                byte[] blob = Utils.readContents(pot);
                String sha = Utils.sha1(blob);
                if (_blobs == null || !_blobs.containsKey(name) ||
                        _blobs.containsKey(name)
                                && !_current.checkMATCHES(name, sha)) {
                    _current.addblob(name, sha);
                    updateOBJECTS(sha, blob);
                }
                pot.delete();
            }
        }
        byte[] serialized = _current.serialize();
        String sha1 = Utils.sha1(serialized);
        updateBRANCH("master", sha1);
        updateCOMMIT(sha1, serialized);
    }

    private void log(String[] args) {
        checkGITLET(args);
        if (args.length != 1) {
            throw Utils.error("Incorrect operands.", args[0]);
        }
        setCURRENT();
        printLOG();
        while(_parent != null) {
            setparentCURRENT(_parent);
            printLOG();
        }
    }

    private void checkout(String[] args) {
        checkGITLET(args);
        if (args.length == 3) {
            File dest = Utils.join(_cwd, args[2]);
            setCURRENT();
            setBLOBS();
            String sha = _blobs.get(args[2]);
            File source = Utils.join(_objects, sha);
            String input = Utils.readContentsAsString(source);
            Utils.writeContents(dest, input);
        } else if (args.length == 4) {
            return;
        }
    }

    /** Helper method for updating the HEAD file. */
    public void updateHEAD(String activeBRANCH) {
        String PATH = _branches.toPath().toString() + File.separator +
        activeBRANCH;
        Utils.writeContents(_HEAD, PATH);
    }
    /** Helper method for updating the branches/branch file. */
    public void updateBRANCH(String branch, String commitSHA1) {
        Utils.writeContents(Utils.join(_branches, branch), commitSHA1);
    }
    /** Helper method for updating the commits/commitSHA1 file. */
    public void updateCOMMIT(String commitSHA1, byte[] serializedCOMMIT) {
        Utils.writeContents(Utils.join(_commits,commitSHA1), serializedCOMMIT);
    }
    /** Helper method for setting the _current Commit. */
    public void setCURRENT() {
        String path = Utils.readContentsAsString(_HEAD);
        File file = new File(path);
        _currSHA = Utils.readContentsAsString(file);
        File commit = Utils.join(_commits, _currSHA);
        _current = Utils.readObject(commit, Commit.class);
        _parent = _current.get_parent();
    }
    /** Helper method for setting the _current Commit to provided ID. */
    public void setparentCURRENT(String SHA) {
        File commit = Utils.join(_commits, SHA);
        _currSHA = SHA;
        _current = Utils.readObject(commit, Commit.class);
        _parent = _current.get_parent();
    }

    /** Helper method for updating the _current Commit. */
    public void updateCURRENT(Commit newCURR) {
        _current = newCURR;
    }
    /** Helper method for setting the _blobs of the current Commit. */
    public void setBLOBS() {
        _blobs = _current.get_blobs();
    }
    /** Helper method for updating the _objects with a potentially new blob. */
    public void updateOBJECTS(String sha, byte[] blob) {
        File name = Utils.join(_objects, sha);
        if (!name.exists()) {
            Utils.writeContents(name, blob);
        }
    }
    /** Helper method for printing the log of the current Commit. */
    public void printLOG() {
        System.out.println("===");
        System.out.println("commit " + _currSHA);
        String date = timestamp(_current.get_millitime());
        System.out.println("Date: " + date);
        System.out.println(_current.get_message());
        System.out.println();
    }
    /** Helper method for getting the timestamp of the current Commit. */
    public String timestamp(long millitime) {
        String pattern = "E MMM d HH:mm:ss yyyy Z";
        Date date = new Date(millitime);
        Locale currentLocale = Locale.getDefault();
        SimpleDateFormat formatter = new SimpleDateFormat(pattern,
                currentLocale);
        String formatted = formatter.format(date);
        return formatted;
        // Thu Nov 9 20:00:05 2017 -0800
    }
    /** Helper method for checking the .gitlet directory existence. */
    public void checkGITLET(String[] args) {
        if (!_exists) {
            throw Utils.error("Not in an initialized Gitlet directory.",
                    args[0]);
        }
    }


    /** File object representing the current working directory ~. */
    private static File _cwd;
    /** File object representing the ~/.gitlet hidden directory. */
    private static File _gitlet;
    /** File object representing the ~/.gitlet/HEAD file location. HEAD stores
     * filepath to the head of the active branch. branches/</active branch>
     * eg. ~/.gitlet/branches/master */
    private static File _HEAD;
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
    /** The latest Commit object that was committed, the head. */
    private Commit _current;
    /** The current Commit object's SHA1 ID. */
    private String _currSHA;
    /** The current Commit object's parent SHA. */
    private String _parent;
    /** The latest Commit's blobs HashMap. */
    private HashMap<String, String> _blobs;
    /** The named of the file to potentially be used. */
    private String _nameFILE;

}
