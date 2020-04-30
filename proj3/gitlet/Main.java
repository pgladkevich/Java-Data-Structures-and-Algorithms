package gitlet;

import edu.neu.ccs.util.FileUtilities;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

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
        try {
            new Main(args);
        } catch (GitletException excp) {
            System.err.printf("%s%n", excp.getMessage());
            System.exit(0);
        }
    }

    /** Check ARGS and perform the requested command if valid. */
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
            case "rm":
                rm(args);
                break;
            case "log":
                log(args);
                break;
            case "global-log":
                globallog(args);
                break;
            case "find":
                find(args);
                break;
            case "status":
                status(args);
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
        // TODO adding a tracked, unchanged file should have no effect.
        // TODO adding nonexistent file gets correct error
        checkGITLET(args);
        if (args.length != 2 || (args[1] == null)) {
            throw Utils.error("Incorrect operands.", args[0]);
        }
        _nameFILE = args[1];
        File source = Utils.join(_cwd, _nameFILE);
        if (!source.exists()) {
            throw Utils.error("File does not exist.", args[0]);
        }
        setcurrent();
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
            dest.delete();
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
            remove.delete();
        }
    }

    /** If staging area is empty or message is empty, abort.
     * Create a new commit whose contents are by default the same as the
     * current commit. Add any files in the staging area that are not in the
     * current commit. For files in the addition subdirectory that are
     * contained in the current commit, compare hash values of files in new and
     * current commit, adding any files whose hash value is novel. For
     * files in the removal subdirectory, remove the files from
     * being tracked in the next commit. Clear the staging area and add new
     * commit to branch, updating the HEAD. */
    private void commit(String[] args) {
        checkGITLET(args);
        List<String> remove = Utils.plainFilenamesIn(_removal);
        List<String> addition = Utils.plainFilenamesIn(_addition);
        if (args.length != 2) {
            throw Utils.error("Incorrect operands.", args[0]);
        } else if (args[1] == null || args[1].compareTo("") == 0) {
            throw Utils.error("Please enter a commit message.", args[0]);
        } else if (remove.isEmpty() && addition.isEmpty()) {
            throw Utils.error("No changes added to the commit.", args[0]);
        }
        setcurrent();
        Commit commit = new Commit(args[1], _currSHA, _current);
        updateCURRENT(commit);
        if (!remove.isEmpty()) {
            for (String name : remove) {
                _current.removeblob(name);
                File rfile = Utils.join(_removal, name);
                rfile.delete();
            }
        }
        setBLOBS();
        if (!addition.isEmpty()) {
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
    /** rm: Search addition for the file from ARGS. If it is present, remove it,
     * unstaging the file for addition. If it is being tracked by the current
     * commit, stage it for removal by adding it to the removal directory and
     * delete it from current working directory if the user has not already
     * done so (do not remove it unless it is tracked in the current commit).
     *
     * Failure Cases: If the file is neither staged nor tracked by the head
     * commit, print the error message "No reason to remove the file." */
    private void rm(String[] args) {
        checkGITLET(args);
        if (args.length != 2) {
            throw Utils.error("Incorrect operands.", args[0]);
        }
        _nameFILE = args[1];
        File cwdFILE = Utils.join(_cwd, _nameFILE);
        File additionFILE = Utils.join(_addition, _nameFILE);
        File removalFILE = Utils.join(_removal, _nameFILE);
        boolean incwd = cwdFILE.exists(), inaddition = additionFILE.exists();
        setcurrent();
        setBLOBS();
        boolean tracked = _blobs.containsKey(_nameFILE);
        // If it is not in the addition directory and it is not tracked
        if (!inaddition && !tracked) {
            throw Utils.error("No reason to remove the file.", args[0]);
        } else if (inaddition && !tracked) {
          // then just remove from addition and don't remove it from cwd
            additionFILE.delete();
        } else if (!inaddition && tracked && incwd) {
            // remove it from being tracked by adding it to removal
            try {
                Files.copy(cwdFILE.toPath(), removalFILE.toPath());
                Utils.restrictedDelete(cwdFILE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (!inaddition && tracked && !incwd) {
            String sha = _blobs.get(_nameFILE);
            File blob = Utils.join(_objects, sha);
            Utils.writeContents(removalFILE, Utils.readContents(blob));
        } else {
            Utils.writeContents(removalFILE, Utils.readContents(additionFILE));
            additionFILE.delete();
            Utils.restrictedDelete(cwdFILE);
        }
    }

    /** For each commit in the tree starting from head, print the commit’s
     * information (toString), and follow the commit’s FIRST parent pointer.
     * In the case of merge commits the second parent is ignored.
     * For merge commits (those that have two parent commits) add a line just
     * below the first where the two hexadecimal numerals following "Merge:"
     * consist of the first seven digits of the first and second parents'
     * commit ids, respectively. */
    private void log(String[] args) {
        checkGITLET(args);
        if (args.length != 1) {
            throw Utils.error("Incorrect operands.", args[0]);
        }
        //TODO Will need to revisit log after I finish merge
        setcurrent();
        printLOG();
        while (_parent != null) {
            setcurrentTOID(_parent);
            printLOG();
        }
    }
    /** For each commit ever made, print the commit’s information.
     * Output will be unordered. */
    private void globallog(String[] args) {
        checkGITLET(args);
        List<String> commits = Utils.plainFilenamesIn(_commits);
        if (!commits.isEmpty()) {
            for (String name : commits) {
                setcurrentTOID(name);
                printLOG();
            }
        }
    }
    /** For each commit that exists, if the message passed in matches the
     *  message for the current commit, print the id of the commit on a new
     *  line.
     *
     * Failure cases: If no such commit exists, prints the error message
     * "Found no commit with that message." */
    private void find(String[] args) {
        checkGITLET(args);
        if (args.length != 2) {
            throw Utils.error("Incorrect operands.", args[0]);
        }
        boolean atleastone = false;
        String message = args[1];
        List<String> commits = Utils.plainFilenamesIn(_commits);
        if (!commits.isEmpty()) {
            for (String sha : commits) {
                setcurrentTOID(sha);
                if (_current.get_message().compareTo(message) == 0) {
                    atleastone = true;
                    System.out.println(sha);
                }
            }
        }
        if (!atleastone) {
            throw Utils.error("Found no commit with that message.",
                    args[0]);
        }
    }
    /** status: All of the following printouts are in lexicographic order.
     * Print out names of each branch, and mark the current branch
     * with an asterisk. Print names of files in the addition directory, and
     * then the files in the removal directory. Print names of files which are
     * modified but not staged by checking for the following:
     * 1. If the file IS in cwd, IS tracked, and NOT staged. Meaning, the
     *    contents of the file in the working directory and current commit
     *    differ AND the file is not in staging area.
     * 2. If the file IS in cwd and IS staged. Meaning, the contents staged for
     * addition differ from those in the working directory.
     * 3. If the file is NOT in cwd and IS staged. Meaning, the file was
     * deleted in the working directory but is staged for addition.
     * 4. If the file is NOT in cwd, IS tracked, and is NOT staged. Meaning, the
     *    file is NOT staged for removal, is deleted from the working directory,
     *    but it is IN the current commit.
     * Lastly, print un-tracked files by: checking all files that are IN the
     * working directory, but are not in the staging area or commit. This
     * includes files that have been staged for removal, but then re-created
     * without Gitlet's knowledge. Ignore any subdirectories that may have been
     * introduced, since Gitlet does not deal with them. */
    private void status(String[] args) {
        checkGITLET(args);
        if (args.length != 1) {
            throw Utils.error("Incorrect operands.", args[0]);
        }
        List<String> cwd = Utils.plainFilenamesIn(_cwd);
        List<String> branches = Utils.plainFilenamesIn(_branches);
        List<String> removal = Utils.plainFilenamesIn(_removal);
        List<String> addition = Utils.plainFilenamesIn(_addition);
        Collections.sort(cwd);
        Collections.sort(branches);
        Collections.sort(removal);
        Collections.sort(addition);
        setcurrent();
        setBLOBS();
        String path = Utils.readContentsAsString(_HEAD);
        File file = new File(path);
        String current = file.getName();

        System.out.println("=== Branches ===");
        for (String branch : branches) {
            if (branch.compareTo(current) == 0) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        for (String name : addition) {
            System.out.println(name);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String name : removal) {
            System.out.println(name);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        // 1. If the file IS in cwd, IS tracked, and NOT staged. Meaning, the
        //     *    contents of the file in the working directory and current commit
        //     *    differ AND the file is not in staging area.
        //     * 2. If the file IS in cwd and IS staged. Meaning, the contents staged for
        //     * addition differ from those in the working directory.
//        for (String name : cwd) {
//            if (_blobs.containsKey(name) && !addition.contains(name)) {
//                String shaSTORED = _blobs.get(name);
//                File pot = Utils.join(_cwd, name);
//                String sha = toSHA(pot);
//                if (shaSTORED != sha) {
//                    System.out.println(name + " (modified)");
//                }
//            }
//            if (addition.contains(name)) {
//                File cwdF = Utils.join(_cwd, name);
//                File addF = Utils.join(_addition, name);
//                if (toSHA(cwdF).compareTo(toSHA(addF)) != 0) {
//                    System.out.println(name + " (modified)");
//                }
//            }
//        }
        //3. If the file is NOT in cwd and IS staged. Meaning, the file was
        //     * deleted in the working directory but is staged for addition.
        //     * 4. If the file is NOT in cwd, IS tracked, and is NOT staged. Meaning, the
        //     *    file is not staged for removal, is deleted from the working directory,
        //     *    but it is IN the current commit.
//        for (String name : addition) {
//            if (!cwd.contains(name)) {
//                System.out.println(name + " (modified)");
//            }
//        }

        System.out.println();
        System.out.println("=== Untracked Files ===");
        // Lastly, print un-tracked files by: checking all files that are IN the
        //     * working directory, but are not in the staging area or commit. This
        //     * includes files that have been staged for removal, but then re-created
        //     * without Gitlet's knowledge. Ignore any subdirectories that may have been
        //     * introduced, since Gitlet does not deal with them.

    }

    /**  */
    private void checkout(String[] args) {
        checkGITLET(args);
        if (args.length == 3) {
            File dest = Utils.join(_cwd, args[2]);
            setcurrent();
            setBLOBS();
            String sha = _blobs.get(args[2]);
            File source = Utils.join(_objects, sha);
            String input = Utils.readContentsAsString(source);
            Utils.writeContents(dest, input);
        } else if (args.length == 4) {
            String sha = args[1];
            File com = Utils.join(_commits, sha);
            Commit commit = Utils.readObject(com, Commit.class);
            File dest = Utils.join(_cwd, args[3]);
            String shaf = commit.get_blobs().get(args[3]);
            File source = Utils.join(_objects, shaf);
            String input = Utils.readContentsAsString(source);
            Utils.writeContents(dest, input);
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
    public void setcurrent() {
        String path = Utils.readContentsAsString(_HEAD);
        File file = new File(path);
        _currSHA = Utils.readContentsAsString(file);
        File commit = Utils.join(_commits, _currSHA);
        _current = Utils.readObject(commit, Commit.class);
        _parent = _current.get_parent();
    }
    /** Helper method for setting the _current Commit to provided ID. */
    public void setcurrentTOID(String SHA) {
        File commit = Utils.join(_commits, SHA);
        _currSHA = SHA;
        _current = Utils.readObject(commit, Commit.class);
        _parent = _current.get_parent();
    }

    /** Helper method for updating the _current Commit to NEWCURR. */
    public void updateCURRENT(Commit newCURR) {
        _current = newCURR;
    }
    /** Helper method for setting the _blobs of the current Commit. */
    public void setBLOBS() {
        _blobs = _current.get_blobs();
    }
    /** Helper method for updating the _objects with a potentially new blob.
     * If the BLOB already exists, then the SHA string will be representing its
     * file name and there will be a match, so it is not re-created. This way
     * every blob is unique. */
    public void updateOBJECTS(String sha, byte[] blob) {
        File name = Utils.join(_objects, sha);
        if (!name.exists()) {
            Utils.writeContents(name, blob);
        }
    }
    /** Helper method for printing the log of the current Commit. This will be
     * called by the log command once the proper objects for _current and
     * _currSHA have been set. The command will print out the commit's metadata
     * by calling the timestamp helper function to format it properly.
     * As an example the UNIX epoch time would be printed as a string:
     * "Thu Jan 1 00:00:00 1970 0000" if the system's local was UTC; */
    public void printLOG() {
        System.out.println("===");
        System.out.println("commit " + _currSHA);
        String date = timestamp(_current.get_millitime());
        System.out.println("Date: " + date);
        System.out.println(_current.get_message());
        System.out.println();
    }
    /** Helper method for getting the timestamp of the current Commit.
     * Converts the input MILLITIME into a formatted string.
     * Format: (Day in Week) Month Day# Hour:Min:Sec Year Locale */
    public String timestamp(long millitime) {
        String pattern = "E MMM d HH:mm:ss yyyy Z";
        Date date = new Date(millitime);
        Locale currentLocale = Locale.getDefault();
        SimpleDateFormat formatter = new SimpleDateFormat(pattern,
                currentLocale);
        String formatted = formatter.format(date);
        return formatted;
    }
    /** Helper method for converting a file to its SHA-1 ID string. */
    public String toSHA(File file) {
        byte[] contents = Utils.serialize(file);
        String SHA1 = Utils.sha1(contents);
        return SHA1;
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
    /** The current Commit object's SHA-1 UID. */
    private String _currSHA;
    /** The current Commit object's parent SHA-1 UID. */
    private String _parent;
    /** The latest Commit's blobs HashMap. */
    private HashMap<String, String> _blobs;
    /** The named of the file to potentially be used. */
    private String _nameFILE;

}
