package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import static gitlet.Utils.UID_LENGTH;

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
        _head = Utils.join(_gitlet, "HEAD");
        _objects = Utils.join(_gitlet, "objects");
        _branches = Utils.join(_gitlet, "branches");
        _staging = Utils.join(_gitlet, "staging");
        _addition = Utils.join(_staging, "addition");
        _removal = Utils.join(_staging, "removal");
        _commits = Utils.join(_gitlet, "commits");
        _remotesLOCAL = Utils.join(_gitlet, "remotes");

        switch (args[0]) {
        case "init":
            init(args); break;
        case "add":
            add(args); break;
        case "commit":
            commit(args); break;
        case "rm":
            rm(args); break;
        case "log":
            log(args); break;
        case "global-log":
            globallog(args); break;
        case "find":
            find(args); break;
        case "status":
            status(args); break;
        case "checkout":
            checkout(args); break;
        case "branch":
            branch(args); break;
        case "rm-branch":
            rmbranch(args); break;
        case "reset":
            reset(args); break;
        case "merge":
            merge(args); break;
        case "add-remote":
            addremote(args); break;
        case "rm-remote":
            rmremote(args); break;
        case "push":
            push(args); break;
        case "fetch":
            fetch(args); break;
        case "pull":
            pull(args); break;

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
     *  architecture. Takes input from args. Takes input from ARGS. */
    private void init(String[] args) {
        if (args.length > 1) {
            throw Utils.error("Incorrect operands.", args[0]);
        } else if (_exists) {
            throw Utils.error("A Gitlet version-control system already "
                    + "exists in the current directory.", args[0]);
        } else {
            _gitlet.mkdir();
            _objects.mkdir();
            _branches.mkdir();
            _staging.mkdir();
            _addition.mkdir();
            _removal.mkdir();
            _commits.mkdir();
            _remotesLOCAL.mkdir();
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
     * with the new contents. Takes input from ARGS. */
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
        setcurrent();
        setBLOBS();
        boolean inaddition = Utils.plainFilenamesIn(_addition)
                .contains(_nameFILE);
        boolean inremoval = Utils.plainFilenamesIn(_removal)
                .contains(_nameFILE);
        boolean incurrent = _blobs.containsKey(_nameFILE);
        File dest = Utils.join(_addition, _nameFILE);
        if (!inaddition && !inremoval && !incurrent) {
            try {
                Files.copy(source.toPath(), dest.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            byte[] contents = Utils.serialize(Utils.readContents(source));
            String sha1 = Utils.sha1(contents);
            dest.delete();
            if (_blobs.containsKey(_nameFILE)
                    && _blobs.get(_nameFILE).compareTo(sha1) != 0) {
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
     * commit to branch, updating the HEAD. Takes input from ARGS. */
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
        String message = args[1];
        setcurrent();
        Commit commit;
        if (message.length() > 6
                && message.substring(0, 6).compareTo("Merged") == 0) {
            commit = new Commit(message, _currMERGESHA, _givnMERGESHA,
                    _currMERGECOM);
        } else {
            commit = new Commit(message, _currSHA, _current);
        }

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
                byte[] blob = Utils.serialize(Utils.readContents(pot));
                String sha = Utils.sha1(blob);
                if (_blobs == null || !_blobs.containsKey(name)
                        || _blobs.containsKey(name)
                                && !_current.checkMATCHES(name, sha)) {
                    _current.addblob(name, sha);
                    updateOBJECTS(sha, blob);
                }
                pot.delete();
            }
        }
        byte[] serialized = _current.serialize();
        String sha1 = Utils.sha1(serialized);
        String branch = getbranchCURRENT();
        updateBRANCH(branch, sha1);
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
        if (!inaddition && !tracked) {
            throw Utils.error("No reason to remove the file.", args[0]);
        } else if (inaddition && !tracked) {
            additionFILE.delete();
        } else if (!inaddition && tracked && incwd) {
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
     * commit ids, respectively. Takes input from ARGS. */
    private void log(String[] args) {
        checkGITLET(args);
        if (args.length != 1) {
            throw Utils.error("Incorrect operands.", args[0]);
        }
        setcurrent();
        printLOG();
        while (_parent != null) {
            setcurrentTOID(_parent);
            printLOG();
        }
    }
    /** For each commit ever made, print the commit’s information.
     * Output will be unordered. Takes input from ARGS. */
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
     *  line. Takes input from ARGS.
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
                if (_current.getmessage().compareTo(message) == 0) {
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
     * introduced, since Gitlet does not deal with them. Takes input ARGS. */
    private void status(String[] args) {
        checkGITLET(args);
        if (args.length != 1) {
            throw Utils.error("Incorrect operands.", args[0]);
        }
        statusSETUP();
        System.out.println(); System.out.println("=== Staged Files ===");
        for (String name : _additionL) {
            System.out.println(name);
        }
        System.out.println(); System.out.println("=== Removed Files ===");
        for (String name : _removalL) {
            System.out.println(name);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String name : _cwdL) {
            File cwdFILE = Utils.join(_cwd, name);
            File addFILE = Utils.join(_addition, name);
            boolean inadd = addFILE.exists();
            boolean tracked = _blobs.containsKey(name);
            String shaCWD = toSHA(cwdFILE);
            if (tracked && !inadd) {
                String shaTRACKED = _blobs.get(name);
                if (shaTRACKED.compareTo(shaCWD) != 0) {
                    System.out.println(name + " (modified)");
                }
            } else if (inadd) {
                String addSHA = toSHA(addFILE);
                if (shaCWD.compareTo(addSHA) != 0) {
                    System.out.println(name + " (modified)");
                }
            }
        }
        for (String name : _additionL) {
            if (!_cwdL.contains(name)) {
                System.out.println(name + " (modified)");
            }
        }
        for (Map.Entry mapElement : _blobs.entrySet()) {
            String n = (String) mapElement.getKey();
            if (!_cwdL.contains(n) && !_additionL.contains(n)
                    && !_removalL.contains(n)) {
                System.out.println(n + " (deleted)");
            }
        }
        System.out.println(); System.out.println("=== Untracked Files ===");
        for (String name : _cwdL) {
            if (!_blobs.containsKey(name) && !_additionL.contains(name)) {
                System.out.println(name);
            }
        }
        System.out.println();
    }

    /** checkout: Checkout is a kind of general command that can do a few
     * different things depending on what its ARGS are. There are 3 possible
     * use cases. Takes input from ARGS.
     * 1. Search for file in list of files in head commit, and copy/overwrite
     * the file into the working directory. The new version of the file is not
     * staged.
     *      Failure Cases: If the file does not exist in the previous commit,
     *      abort, printing the error message "File does not exist
     *      in that commit."
     * 2. Search for file in list of files in the specified commit, and
     * copy/overwrite the file in the working directory. The new version of the
     * file is not staged.
     *      Failure Cases: If no commit with the given id exists, print
     *      "No commit with that id exists." Otherwise, if the file does not
     *      exist in the given commit, print the same message as for failure
     *      case 1.
     * 3. For each file in the head commit of the given branch, copy/overwrite
     * the file in the working directory. If a file is in the commit of the
     * current branch but not in the specified branch, delete it from the
     * current working directory. If the current and given branches are
     * different, clear the staging area. Set the given branch to head.
     *      Failure Cases: If no branch with that name exists, print
     *      "No such branch exists." If that branch is the current branch,
     *      print "No need to checkout the current branch." If a working file is
     *      un-tracked in the current branch and would be overwritten by the
     *      checkout, print "There is an un-tracked file in the way; delete it,
     *      or add and commit it first." and exit;
     *      perform this check before doing anything else. */
    private void checkout(String[] args) {
        checkGITLET(args);
        if (args.length == 3) {
            if (args[1].compareTo("--") != 0) {
                throw Utils.error("Incorrect operands.", args[0]);
            }
            _nameFILE = args[2];
            File dest = Utils.join(_cwd, _nameFILE);
            setcurrent();
            setBLOBS();
            if (!_blobs.containsKey(_nameFILE)) {
                throw Utils.error("File does not exist in that commit.",
                        args[0]);
            }
            String sha = _blobs.get(_nameFILE);
            String contents = getblobCONTENTS(sha);
            Utils.writeContents(dest, contents);
        } else if (args.length == 4) {
            if (args[2].compareTo("--") != 0) {
                throw Utils.error("Incorrect operands.", args[0]);
            }
            String sha = args[1];
            if (sha.length() < UID_LENGTH) {
                int comlen = sha.length();
                List<String> commits = Utils.plainFilenamesIn(_commits);
                for (String commit : commits) {
                    String substring = commit.substring(0, comlen);
                    if (sha.compareTo(substring) == 0) {
                        sha = commit;
                        break;
                    }
                }
            }
            _nameFILE = args[3];
            File com = Utils.join(_commits, sha);
            if (!com.exists()) {
                throw Utils.error("No commit with that id exists.",
                        args[0]);
            }
            setcurrentTOID(sha);
            setBLOBS();
            if (!_blobs.containsKey(_nameFILE)) {
                throw Utils.error("File does not exist in that commit.",
                        args[0]);
            }
            File dest = Utils.join(_cwd, _nameFILE);
            String shaf = _blobs.get(_nameFILE);
            String contents = getblobCONTENTS(shaf);
            Utils.writeContents(dest, contents);
        } else if (args.length == 2) {
            checkoutBRANCH(args);
        } else {
            throw Utils.error("Incorrect operands.", args[0]);
        }
    }
    /** Perform a checkout of a branch. Takes input ARGS. See comment for
     * checkout above. */
    private void checkoutBRANCH(String[] args) {
        String branch = args[1];
        File check = Utils.join(_branches, branch);
        String path = Utils.readContentsAsString(_head);
        File currBRANCH = new File(path);
        String checkDIR = currBRANCH.getParentFile().getName();
        if (!check.exists()) {
            throw Utils.error("No such branch exists.", args[0]);
        } else if (branch.compareTo(currBRANCH.getName()) == 0
                && checkDIR.compareTo("branches") == 0) {
            throw Utils.error("No need to checkout the current "
                    + "branch.", args[0]);
        }
        setcurrent();
        HashMap<String, String> oldblobs = _current.getblobs();
        String sha = getBRANCHHEAD(branch);
        setcurrentTOID(sha);
        setBLOBS();
        List<String> cwd = Utils.plainFilenamesIn(_cwd);
        for (Map.Entry mapElement : _blobs.entrySet()) {
            String n = (String) mapElement.getKey();
            String s = (String) mapElement.getValue();
            if (!oldblobs.containsKey(n) && cwd.contains(n)
                    && _blobs.containsKey(n)) {
                throw Utils.error("There is an untracked file in the "
                        + "way; delete it, or add and commit it first.");
            }
            writeblobTOCWD(n, s);
        }
        for (Map.Entry mapElement : oldblobs.entrySet()) {
            String name = (String) mapElement.getKey();
            if (!_blobs.containsKey(name)) {
                File file = Utils.join(_cwd, name);
                file.delete();
            }
        }
        clearSTAGING();
        updateHEAD(branch);
    }

    /** Create a new branch(reference to a SHA-1 identifier) with the given
     * name and point it at the current head node. This command does NOT
     * immediately switch to the newly created branch. Takes input from ARGS.
     *
     * Failure cases: If a branch with the given name already exists, print
     * the error message "A branch with that name already exists." */
    private void branch(String[] args) {
        checkGITLET(args);
        if (args.length != 2) {
            throw Utils.error("Incorrect operands.", args[0]);
        }
        File branch = Utils.join(_branches, args[1]);
        if (branch.exists()) {
            throw Utils.error("A branch with that name already exists.",
                    args[0]);
        }
        setcurrent();
        Utils.writeContents(branch, _currSHA);
    }

    /** Remove specified branch from list of branches. Deletes the branch with
     * the given name. Deleting only the pointer associated with the branch;
     * not any commits created under the branch, or anything like that.
     * Takes input from ARGS.
     *
     * Failure Cases: If a branch with the given name does not exist, aborts.
     * Print the error message
     "A branch with that name does not exist." If you try to remove the branch
     you're currently on, aborts, printing the error message
     "Cannot remove the current branch." */
    private void rmbranch(String[] args) {
        checkGITLET(args);
        File branch = Utils.join(_branches, args[1]);
        if (!branch.exists()) {
            throw Utils.error("A branch with that name does not exist.",
                    args[0]);
        }
        String currentBRANCH = getbranchCURRENT();
        if (currentBRANCH.compareTo(branch.getName()) == 0) {
            throw Utils.error("Cannot remove the current branch.",
                    args[0]);
        }
        branch.delete();
    }
    /**  For each file in the given commit, write the version of the file from
     * the given commit into cwd. Set the head of the current branch to the
     * given commit. Takes input from ARGS.
     *
     * Failure Cases: If no commit with the given id exists, print
     * "No commit with that id exists." If a working file is untracked in the
     * current branch and would be overwritten by the reset, print
     * "There is an untracked file in the way; delete it, or add and commit it
     * first." and exit; perform this check before doing anything else. */
    private void reset(String[] args) {
        if (args.length != 2) {
            throw Utils.error("Incorrect operands.", args[0]);
        }
        checkGITLET(args);
        String sha = args[1];
        File commit = Utils.join(_commits, sha);
        if (!commit.exists()) {
            throw Utils.error("No commit with that id exists.", args[0]);
        }
        setcurrent();
        setBLOBS();
        HashMap<String, String> oldblobs = _current.getblobs();
        String branch = getbranchCURRENT();
        setcurrentTOID(sha);
        setBLOBS();
        List<String> cwd = Utils.plainFilenamesIn(_cwd);
        for (Map.Entry mapElement : _blobs.entrySet()) {
            String n = (String) mapElement.getKey();
            String s = (String) mapElement.getValue();
            if (!oldblobs.containsKey(n) && cwd.contains(n)
                    && _blobs.containsKey(n)) {
                throw Utils.error("There is an untracked file in the "
                        + "way; delete it, or add and commit it first.");
            }
            writeblobTOCWD(n, s);
        }
        for (Map.Entry mapElement : oldblobs.entrySet()) {
            String name = (String) mapElement.getKey();
            if (!_blobs.containsKey(name)) {
                File file = Utils.join(_cwd, name);
                file.delete();
            }
        }
        clearSTAGING();
        updateBRANCH(branch, sha);
    }

    /** Merges files from the given branch into the current branch. Retrieve
     * the head Commit of both branches. If these two do not point to the same
     * commit, then traverse the entire tree of the given branch, storing the
     * Commits in a HashMap<String, Commit> with the key being the SHA value.
     * Then, traverse the current branch until you find the first shared
     * commit. Set that commit to be the split point, which is the latest
     * common ancestor of both branches. If the split point is the same commit
     * as the given branch's head, do nothing. The merge is complete, and the
     * operation ends with the message "Given branch is an ancestor of the
     * current branch." If the split point is the current branch, then the
     * effect is to check out the given branch, and the operation ends after
     * printing the message "Current branch fast-forwarded." If the split point
     * is neither of the above, proceed with the steps below. Takes input from
     * ARGS.
     *
     * If at any point a conflict is encountered, set the boolean _conflict to
     * true, and concatenate the contents of the file in the current branch
     * with the contents of the version in the given branch.
     *
     * Iterate through each file in the given branch.
     *  1. If the file is absent from the split-point and the current branch,
     *  checkout the file from the given branch and stage for addition.
     *  2. If the file is absent from the current branch, and not modified from
     *  the version in the split-point, it should remain absent --> no action.
     *  3. If the file is absent from the current branch, and modified from the
     *  version in the split-point --> it is a conflict.
     *  4. If the file in the given branch is not modified from the split-point,
     *  but the file is modified in the current branch, it should remain as is
     *  --> no action.
     *  5. If the file is modified in the given branch, but is the same version
     *  in the current branch as from the split-point, checkout the file from
     *  the given branch and stage it for addition.
     *  6. If the file is modified in the given branch in the same way as the
     *  current branch --> no action.
     *  7. If the file is modified in the given branch in a different way from
     *  the modification in the current branch --> conflict.
     *
     * Iterate through every file in the current branch.
     *  1. If the file is in both the current branch and the split-point
     *  (not modified), and is absent in the given branch, call the
     *  rm command on the file.
     *
     * Failure Cases: If there are staged files present (in addition or removal)
     * print the error message "You have uncommitted changes." If a branch with
     * the given name does not exist, print the error message "A branch with
     * that name does not exist." If attempting to merge a branch with itself,
     * print the error message "Cannot merge a branch with itself." If an
     * un-tracked file in the current commit would be overwritten or deleted by
     * the merge, print "There is an untracked file in the way; delete it, or
     * add and commit it first." Perform this check before doing anything else.
     *
     * Once the files have been updated, merge automatically calls the commit
     * command with the message "Merged [given branch name] into
     * [current branch name]". Then, if the merge encountered a conflict,
     * print the message "Encountered a merge conflict." to the terminal. The
     * resulting commit will have the current branch as its parent, and the
     * given branch as its second parent. */
    private void merge(String[] args) {
        checkGITLET(args);
        if (args.length != 2) {
            throw Utils.error("Incorrect operands.", args[0]);
        }
        _conflict = false; _givnBRNCHNAME = args[1];
        _givnBRNCHFILE = Utils.join(_branches, _givnBRNCHNAME);
        _currBRNCHNAME = getbranchCURRENT();
        List<String> addition = Utils.plainFilenamesIn(_addition);
        List<String> removal = Utils.plainFilenamesIn(_removal);
        if (!addition.isEmpty() || !removal.isEmpty()) {
            throw Utils.error("You have uncommitted changes.", args[0]);
        } else if (!_givnBRNCHFILE.exists()) {
            throw Utils.error("A branch with that name does not exist.",
                    args[0]);
        } else if (_givnBRNCHNAME.compareTo(_currBRNCHNAME) == 0) {
            throw Utils.error("Cannot merge a branch with itself.",
                    args[0]);
        }
        setcurrent();
        setBLOBS();
        _currMERGESHA = _currSHA;
        _currMERGEBLOBS = _blobs;
        _currMERGECOM = _current;
        _givnMERGESHA = Utils.readContentsAsString(_givnBRNCHFILE);
        setcurrentTOID(_givnMERGESHA);
        setBLOBS();
        _givnMERGECOM = _current;
        _givnMERGEBLOBS = _blobs;
        mergecheckUNTRACKED();
        _givnANCESTORS = new HashMap<>();
        findgivnANCESTORS(_givnMERGESHA);
        findsplitPOINT();
        if (_spltMERGESHA.compareTo(_givnMERGESHA) == 0) {
            System.out.println("Given branch is an ancestor of the current "
                    + "branch.");
            return;
        } else if (_spltMERGESHA.compareTo(_currMERGESHA) == 0) {
            String[] newargs = {"checkout", _givnBRNCHNAME};
            checkout(newargs);
            System.out.println("Current branch fast-forwarded.");
            return;
        }
        for (Map.Entry mapElement : _givnMERGEBLOBS.entrySet()) {
            String n = (String) mapElement.getKey();
            String s = (String) mapElement.getValue();
            performACTION1(n, s);
        }
        for (Map.Entry mapElement : _currMERGEBLOBS.entrySet()) {
            String n = (String) mapElement.getKey();
            String s = (String) mapElement.getValue();
            performACTION2(n, s);
        }
        String[] cargs = {"commit", "Merged " + _givnBRNCHNAME + " into "
                + _currBRNCHNAME + "."};
        commit(cargs);
        if (_conflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /** Saves the given login information under the given remote name in a file
     * in the remotes subdirectory of .gitlet. In this case the login
     * information is simply the absolute path to the remote directory.
     * Attempts to push or pull from the given remote name will then attempt to
     * use this .gitlet directory.
     * By writing, java gitlet.Main add-remote other ../testing/otherdir/.gitlet
     * you can provide tests of remotes that will work from all locations.
     * Takes input from ARGS.
     *
     * Usage: `java gitlet.Main
     * add-remote [remote name] [name of remote directory]/.gitlet '
     * Failure cases: If a remote with the given name already exists,
     * print the error message: "A remote with that name already exists." */
    private void addremote(String[] args) {
        checkGITLET(args);
        List<String> remotes = Utils.plainFilenamesIn(_remotesLOCAL);
        if (args.length != 3) {
            throw Utils.error("Incorrect operands.", args[0]);
        }
        _remoteNAME = args[1];
        _remotePATH = args[2];
        if (remotes.contains(_remoteNAME)) {
            throw Utils.error("A remote with that name already exists.",
                    args[0]);
        }
        File remote = Utils.join(_remotesLOCAL, _remoteNAME);
        Utils.writeContents(remote, _remotePATH);
    }

    /** Remove information associated with the given remote name. If you ever
     * want to change a remote's information the rm-remote command will be
     * called and then the remote will be re-added. Takes input from ARGS.
     *
     * Usage: java gitlet.Main rm-remote [remote name]
     *
     * Failure cases: If a remote with the given name does not exist, print
     * the error message: "A remote with that name does not exist." */
    private void rmremote(String[] args) {
        checkGITLET(args);
        List<String> remotes = Utils.plainFilenamesIn(_remotesLOCAL);
        if (args.length != 2) {
            throw Utils.error("Incorrect operands.", args[0]);
        }
        _remoteNAME = args[1];
        if (!remotes.contains(_remoteNAME)) {
            throw Utils.error("A remote with that name does not exist.",
                    args[0]);
        }
        File remote = Utils.join(_remotesLOCAL, _remoteNAME);
        remote.delete();
    }

    /** push: Attempts to append the current branch's commits to the end of the
     * given branch at the given remote. This command only works if the remote
     * branch's head is in the history of the current local head, which means
     * that the local branch contains some commits in the future of the remote
     * branch. In this case, append the future commits to the remote branch.
     * Then, the remote should reset to the front of the appended commits
     * (so its head will be the same as the local head). This is called
     * fast-forwarding. If the Gitlet system on the remote machine exists, but
     * does not have the input branch, then simply add the branch to the remote
     * Gitlet. Takes input from ARGS.
     *
     * Usage: java gitlet.Main push [remote name] [remote branch name]
     *
     * Failure cases: If the remote branch's head is not in the history of the
     * current local head, print the error message
     * "Please pull down remote changes before pushing." If the remote
     * .gitlet directory does not exist, print "Remote directory not found." */
    private void push(String[] args) {
        checkGITLET(args);
        if (args.length != 3) {
            throw Utils.error("Incorrect operands.", args[0]);
        }
        _remoteNAME = args[1];
        _remoteBRNCHNAME = args[2];
        File path = Utils.join(_remotesLOCAL, _remoteNAME);
        _remotePATH = Utils.readContentsAsString(path);
        _gitletREMOTE = new File(_remotePATH);
        if (!_gitletREMOTE.exists()) {
            throw Utils.error("Remote directory not found.",
                    args[0]);
        }
        _branchesREMOTE = Utils.join(_gitletREMOTE, "branches");
        _remoteBRNCHFILE = Utils.join(_branchesREMOTE, _remoteBRNCHNAME);
        _commitsREMOTE = Utils.join(_gitletREMOTE, "commits");
        setcurrent();
        _currFIRSTANCESTORS = new HashMap<>();
        findFIRSTANCESTORS(_currSHA);
        if (!_remoteBRNCHFILE.exists()) {
            Utils.writeContents(_remoteBRNCHFILE, _currSHA);
        } else {
            _remoteCURRSHA = Utils.readContentsAsString(_remoteBRNCHFILE);
            if (!_currFIRSTANCESTORS.containsKey(_remoteCURRSHA)) {
                throw Utils.error("Please pull down remote changes "
                        + "before pushing.", args[0]);
            }
        }
        setcurrent();
        Utils.writeContents(_remoteBRNCHFILE, _currSHA);
        for (Map.Entry mapElement : _currFIRSTANCESTORS.entrySet()) {
            String comSHA = (String) mapElement.getKey();
            File localCOM = Utils.join(_commits, comSHA);
            File remoteCOM = Utils.join(_commitsREMOTE, comSHA);
            if (!remoteCOM.exists()) {
                try {
                    Files.copy(localCOM.toPath(), remoteCOM.toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /** Brings down commits from the remote Gitlet repository into the local
     * Gitlet repository. Copies all commits and blobs from the given branch in
     * the remote repository (that are not already in the current repository)
     * into a branch named [remote name]/[remote branch name] in the local
     * .gitlet, changing the branch [remote name]/[remote branch name] to point
     * to the head commit of the remote branch. Takes input from ARGS.
     *
     * 1. First, checks if the branch to be created already exists. If it does,
     * then get the Commit corresponding to the local head of this branch.
     * Check if this commit's SHA exists in the remote repo. If it doesn't this
     * means the local is ahead of the remote and fetch does not need to be
     * performed. Exit. Otherwise, set this commit's parent to be the base case.
     * If the branch does not exist then the base case is simply null,
     * the parent of the initial commit.
     * 2. Retrieve the SHA-1 ID of the head of the remote branch.
     * 3. Create the branch in the local repository if it did not previously
     * exist. In both cases set the head of the local copy of the remote branch
     * (change the SHA of the _branches/[remote name]/[remote branch name] file)
     * to the retrieved SHA-1 ID.
     * 4. Retrieve the remote commit corresponding to the aforementioned SHA
     * UID and set it to be the current commit as well as the current blobs.
     * 5. For each blob in the commit, check if it is already present in the
     * local _objects directory. If not, copy the file over.
     * 6. Once all blobs have been copied (if not present), copy the serialized
     * Commit file from the remote to the the local _commits directory. Then,
     * follow the parent pointer of the current commit and repeat steps 4-6
     * until the base case is reached (either null or parent of head of local
     * copy of remote). **Base case checked at the start.**
     *
     * Usage: java gitlet.Main fetch [remote name] [remote branch name]
     *
     * Failure cases: If the remote Gitlet repository does not have the given
     * branch name, print error: "That remote does not have that branch."
     * If the remote .gitlet directory does not exist, print:
     * "Remote directory not found." */
    private void fetch(String[] args) {
        checkGITLET(args);
        if (args.length != 3) {
            throw Utils.error("Incorrect operands.", args[0]);
        }
        _remoteNAME = args[1];
        _remoteBRNCHNAME = args[2];
        File path = Utils.join(_remotesLOCAL, _remoteNAME);
        _remotePATH = Utils.readContentsAsString(path);
        _gitletREMOTE = new File(_remotePATH);
        if (!_gitletREMOTE.exists()) {
            throw Utils.error("Remote directory not found.",
                    args[0]);
        }
        _branchesREMOTE = Utils.join(_gitletREMOTE, "branches");
        _remoteBRNCHFILE = Utils.join(_branchesREMOTE, _remoteBRNCHNAME);
        if (!_remoteBRNCHFILE.exists()) {
            throw Utils.error("That remote does not have that branch.",
                    args[0]);
        }
        _commitsREMOTE = Utils.join(_gitletREMOTE, "commits");
        _objectsREMOTE = Utils.join(_gitletREMOTE, "objects");
        _remoteCURRSHA = Utils.readContentsAsString(_remoteBRNCHFILE);
        File localbranchDIR = Utils.join(_branches, _remoteNAME);
        if (!localbranchDIR.exists()) {
            localbranchDIR.mkdir();
        }
        _localREMOTEBRNCHFILE = Utils.join(localbranchDIR, _remoteBRNCHNAME);
        _basecase = null;
        if (_localREMOTEBRNCHFILE.exists()) {
            String shaCOMMIT =
                    Utils.readContentsAsString(_localREMOTEBRNCHFILE);
            File checkAHEAD = Utils.join(_commitsREMOTE, shaCOMMIT);
            if (!checkAHEAD.exists()) {
                return;
            }
            setcurrentTOID(shaCOMMIT);
            _basecase = _parent;
        }
        Utils.writeContents(_localREMOTEBRNCHFILE, _remoteCURRSHA);
        setcurrentTOREMOTEID(_remoteCURRSHA);
        setBLOBS();
        copyCOMMITS();
    }
    /** Fetches branch [remote name]/[remote branch name] as for the fetch
     * command, and then merges that fetch into the current branch. Takes
     * in the input of ARGS.
     *
     * Usage: java gitlet.Main pull [remote name] [remote branch name]
     *
     * Failure cases: Just the failure cases of fetch and merge together. */
    private void pull(String[] args) {
        checkGITLET(args);
        if (args.length != 3) {
            throw Utils.error("Incorrect operands.", args[0]);
        }
        _remoteNAME = args[1];
        _remoteBRNCHNAME = args[2];
        String[] fargs = {"fetch", _remoteNAME, _remoteBRNCHNAME};
        fetch(fargs);
        String[] margs = {"merge", _remoteNAME + File.separator
                + _remoteBRNCHNAME};
        merge(margs);
    }

    /** Helper method for updating the HEAD file for the passed
     * in ACTIVEBRANCH. */
    public void updateHEAD(String activeBRANCH) {
        String path = _branches.toPath().toString() + File.separator
                + activeBRANCH;
        Utils.writeContents(_head, path);
    }
    /** Helper method for updating the branches/branch file. Update BRANCH file
     * to the COMMITSHA1. */
    public void updateBRANCH(String branch, String commitSHA1) {
        Utils.writeContents(Utils.join(_branches, branch), commitSHA1);
    }
    /** Helper method for updating the commits/commitSHA1 file. The string
     * representing the file is COMMITSHA1 and the contents are contained in
     * SERIALIZEDCOMMIT. */
    public void updateCOMMIT(String commitSHA1, byte[] serializedCOMMIT) {
        Utils.writeContents(Utils.join(_commits, commitSHA1), serializedCOMMIT);
    }
    /** Helper method for setting the _current Commit. */
    public void setcurrent() {
        String path = Utils.readContentsAsString(_head);
        File file = new File(path);
        _currSHA = Utils.readContentsAsString(file);
        File commit = Utils.join(_commits, _currSHA);
        _current = Utils.readObject(commit, Commit.class);
        _parent = _current.getparent();
        _secondparent = _current.getsecondparent();
    }
    /** Helper method for setting the _current Commit to provided SHA ID. */
    public void setcurrentTOID(String sha) {
        File commit = Utils.join(_commits, sha);
        _currSHA = sha;
        _current = Utils.readObject(commit, Commit.class);
        _parent = _current.getparent();
        _secondparent = _current.getsecondparent();
    }

    /** Helper method for updating the _current Commit to NEWCURR. */
    public void updateCURRENT(Commit newCURR) {
        _current = newCURR;
    }
    /** Helper method for setting the _blobs of the current Commit. */
    public void setBLOBS() {
        _blobs = _current.getblobs();
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
    /** Helper method for de-serializing and retrieving the contents of the
     * blob whose SHA ID was passed in. Returns the string representing said
     * contents. */
    public String getblobCONTENTS(String sha) {
        File source = Utils.join(_objects, sha);
        byte[] bytec = Utils.readObject(source, byte[].class);
        return new String(bytec);
    }

    /** Helper method for getting the name of the currently active BRANCH.
     * @return String of branch. */
    public String getbranchCURRENT() {
        String path = Utils.readContentsAsString(_head);
        File branch = new File(path);
        String name = branch.getName();
        return name;
    }
    /** Helper method for getting the SHA-1 ID of the head of the given
     * BRANCH.
     * @return String of branch head */
    public String getBRANCHHEAD(String branch) {
        File head = Utils.join(_branches, branch);
        String sha = Utils.readContentsAsString(head);
        return sha;
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
        if (_secondparent != null) {
            System.out.println("Merge: " + _parent.substring(0, 7) + " "
                    + _secondparent.substring(0, 7));
        }
        String date = timestamp(_current.getmillitime());
        System.out.println("Date: " + date);
        System.out.println(_current.getmessage());
        System.out.println();
    }
    /** Helper method for getting the timestamp of the current Commit.
     * Converts the input MILLITIME into a formatted string.
     * Format: (Day in Week) Month Day# Hour:Min:Sec Year Locale
     * @return String of timestamp */
    public String timestamp(long millitime) {
        String pattern = "E MMM d HH:mm:ss yyyy Z";
        Date date = new Date(millitime);
        Locale currentLocale = Locale.getDefault();
        SimpleDateFormat formatter = new SimpleDateFormat(pattern,
                currentLocale);
        String formatted = formatter.format(date);
        return formatted;
    }
    /** Helper method for converting a FILE to its SHA-1 ID string.
     * @return byte[] with serialized contents */
    public String toSHA(File file) {
        byte[] contents = Utils.readContents(file);
        byte[] serialized = Utils.serialize(contents);
        return Utils.sha1(serialized);
    }
    /** Helper method for checking the .gitlet directory existence. Takes
     * input from ARGS.  */
    public void checkGITLET(String[] args) {
        if (!_exists) {
            throw Utils.error("Not in an initialized Gitlet directory.",
                    args[0]);
        }
    }
    /** Helper method for writing the contents of passed in file NAME into the
     * user's current working directory. Contents are retrieved from _objects
     * using the passed in SHA ID to get the corresponding blob. */
    public void writeblobTOCWD(String name, String sha) {
        File dest = Utils.join(_cwd, name);
        String contents = getblobCONTENTS(sha);
        Utils.writeContents(dest, contents);
    }
    /** Helper method for deleting all files from addition and removal
     * subdirectories. This clears the staging area. */
    public void clearSTAGING() {
        List<String> addition = Utils.plainFilenamesIn(_addition);
        List<String> removal = Utils.plainFilenamesIn(_removal);
        for (String name : addition) {
            File file = Utils.join(_addition, name);
            file.delete();
        }
        for (String name : removal) {
            File file = Utils.join(_removal, name);
            file.delete();
        }
    }
    /** Helper method for the merge command to check if there are any untracked
     * files in the current working directory that would be over-written after
     * the merge commit. */
    private void mergecheckUNTRACKED() {
        List<String> cwd = Utils.plainFilenamesIn(_cwd);
        for (Map.Entry mapElement : _givnMERGEBLOBS.entrySet()) {
            String n = (String) mapElement.getKey();
            String s = (String) mapElement.getValue();
            if (!_currMERGEBLOBS.containsKey(n) && cwd.contains(n)
                    && _givnMERGEBLOBS.containsKey(n)) {
                File file = Utils.join(_cwd, n);
                String blobSHA = toSHA(file);
                if (blobSHA.compareTo(s) != 0) {
                    throw Utils.error("There is an untracked file in the "
                            + "way; delete it, or add and commit it first.");
                }
            }
        }
    }
    /** Helper method for the merge command to find all of the ancestors of the
     * given commit associated with  the provided SHA. This method fills out
     * the _givnANCESTORS hashmap. Traversal is performed recursively */
    private void findgivnANCESTORS(String sha) {
        if (sha == null) {
            return;
        }
        setcurrentTOID(sha);
        if (!_givnANCESTORS.containsKey(_currSHA)) {
            _givnANCESTORS.put(_currSHA, _current);
            String sprnt = _secondparent;
            findgivnANCESTORS(_parent);
            findgivnANCESTORS(sprnt);
        }
    }
    /** Helper method for the merge command to find the latest common ancestor,
     * or in other words the split-point. For this purpose a BFS is performed
     * with a traversal or the parent and secondparent of _currMERGECOM.
     * Because the parent is added to the queue first in the case of crisscross
     * merges the parent will always be selected as the split-point. */
    private void findsplitPOINT() {
        int capacity = 8;
        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(capacity);
        queue.add(_currMERGESHA);
        while (queue.size() != 0) {
            String sha = queue.poll();
            if (_givnANCESTORS.containsKey(sha)) {
                _spltMERGESHA = sha;
                setcurrentTOID(_spltMERGESHA);
                setBLOBS();
                _spltMERGEBLOBS = _blobs;
                _spltMERGECOM = _current;
                return;
            }
            setcurrentTOID(sha);
            queue.add(_parent);
            if (_secondparent != null) {
                queue.add(_secondparent);
            }
        }
    }
    /** Helper method for the merge command to perform the necessary actions
     * on the files contained in both the current and given branch. Action is
     * performed on the file created by using the passed in FILENAME and
     * retrieving the blob corresponding to the the SHA UID. */
    private void performACTION1(String fileNAME, String sha) {
        boolean incurr = _currMERGEBLOBS.containsKey(fileNAME);
        boolean insplt = _spltMERGEBLOBS.containsKey(fileNAME);
        if (!incurr && !insplt) {
            String[] cargs = {"checkout", _givnMERGESHA, "--", fileNAME};
            checkout(cargs);
            stageFILE(fileNAME);
        } else if (!incurr && insplt) {
            String spltblobSHA = _spltMERGEBLOBS.get(fileNAME);
            if (sha.compareTo(spltblobSHA) != 0) {
                conflictRESOLVE(fileNAME, "3");
                stageFILE(fileNAME);
            }
        } else if (incurr && !insplt) {
            String currblobSHA = _currMERGEBLOBS.get(fileNAME);
            if (sha.compareTo(currblobSHA) != 0) {
                conflictRESOLVE(fileNAME, "2");
                stageFILE(fileNAME);
            }
        } else {
            String spltblobSHA = _spltMERGEBLOBS.get(fileNAME);
            String currblobSHA = _currMERGEBLOBS.get(fileNAME);
            if (currblobSHA.compareTo(spltblobSHA) == 0
                    && currblobSHA.compareTo(sha) != 0) {
                String[] cargs = {"checkout", _givnMERGESHA, "--", fileNAME};
                checkout(cargs);
                stageFILE(fileNAME);
            } else if (currblobSHA.compareTo(spltblobSHA) != 0
                    && currblobSHA.compareTo(sha) != 0
                    && spltblobSHA.compareTo(sha) != 0) {
                conflictRESOLVE(fileNAME, "1");
                stageFILE(fileNAME);
            }
        }

    }
    /** Helper method for the merge command to perform the necessary actions
     * on the files contained in both the current and split commit, but not in
     * the given branch. Action is performed on the file created
     * by using the passed in FILENAME and retrieving the blob corresponding
     * to the the SHA UID. */
    private void performACTION2(String fileNAME, String sha) {
        boolean ingivn = _givnMERGEBLOBS.containsKey(fileNAME);
        boolean insplt = _spltMERGEBLOBS.containsKey(fileNAME);
        if (!ingivn && insplt) {
            String spltblobSHA = _spltMERGEBLOBS.get(fileNAME);
            String currblobSHA = _currMERGEBLOBS.get(fileNAME);
            if (spltblobSHA.compareTo(currblobSHA) == 0) {
                String[] args = {"rm", fileNAME};
                rm(args);
            } else {
                conflictRESOLVE(fileNAME, "4");
                stageFILE(fileNAME);
            }
        }
    }
    /** Helper method resolving conflicts during a merge. Input is taken from
     * the file corresponding to FILENAME and a conflict is resolved
     * depending on the case C. */
    private void conflictRESOLVE(String fileNAME, String c) {
        _conflict = true;
        File cwdFILE = Utils.join(_cwd, fileNAME);
        switch (c) {
        case ("1"):
        case ("2"):
            String shaCURR1 = _currMERGEBLOBS.get(fileNAME);
            String contentsCURR1 = getblobCONTENTS(shaCURR1);
            String shaGIVN1 = _givnMERGEBLOBS.get(fileNAME);
            String contentsGIVN1 = getblobCONTENTS(shaGIVN1);
            String result1 = formatCONFLICTRESULT(contentsCURR1,
                    contentsGIVN1);
            Utils.writeContents(cwdFILE, result1);
            break;
        case ("3"):
            String shaGIVN3 = _givnMERGEBLOBS.get(fileNAME);
            String contentsGIVN3 = getblobCONTENTS(shaGIVN3);
            String result3 = formatCONFLICTRESULT("",
                    contentsGIVN3);
            Utils.writeContents(cwdFILE, result3);
            break;
        case ("4"):
            String shaCURR4 = _currMERGEBLOBS.get(fileNAME);
            String contentsCURR4 = getblobCONTENTS(shaCURR4);
            String result4 = formatCONFLICTRESULT(contentsCURR4,
                    "");
            Utils.writeContents(cwdFILE, result4);
            break;
        default:
            break;
        }
    }
    /** Helper method for formatting the contents for the resulting file when
     * you have conflicts during a merge. The current commit's contents are
     * provided in CONTENTSCURR and the given commit's contents are provided
     * in CONTENTSGIVN.
     *
     *@return Formatted string of concatenated file contents */
    private String formatCONFLICTRESULT(String contentsCURR,
                                        String contentsGIVN) {
        return "<<<<<<< HEAD" + System.lineSeparator() + contentsCURR
                + "=======" + System.lineSeparator() + contentsGIVN
                + ">>>>>>>" + System.lineSeparator();
    }
    /** Helper method for the merge command to perform the necessary actions
     * on the files contained in both the current and given branch, as well as
     * the current working directory. Action is performed on the file created
     * by using the passed in FILENAME and retrieving the blob corresponding
     * to the the SHA UID. */
    private void stageFILE(String fileNAME) {
        String[] aargs = {"add", fileNAME};
        add(aargs);
    }
    /** Helper method for setting the _current Commit to provided SHA ID that
     * corresponds to a commit in the remote repository. */
    public void setcurrentTOREMOTEID(String sha) {
        File commit = Utils.join(_commitsREMOTE, sha);
        _currSHA = sha;
        _current = Utils.readObject(commit, Commit.class);
        _parent = _current.getparent();
        _secondparent = _current.getsecondparent();
    }
    /** Helper method for the push command to find all of the first parent
     * ancestors of the current commit associated with the provided SHA.
     * This method fills out the _currFIRSTANCESTORS hashmap.
     * Traversal is performed recursively */
    private void findFIRSTANCESTORS(String sha) {
        if (sha == null) {
            return;
        }
        setcurrentTOID(sha);
        _currFIRSTANCESTORS.put(_currSHA, _current);
        findFIRSTANCESTORS(_parent);
    }
    /** Helper method for fetch to copy over blobs and commits. */
    private void copyCOMMITS() {
        while (_parent != null) {
            if (_basecase != null && _basecase.compareTo(_parent) == 0) {
                return;
            }
            for (Map.Entry mapElement : _blobs.entrySet()) {
                String s = (String) mapElement.getValue();
                File localBLOB = Utils.join(_objects, s);
                if (!localBLOB.exists()) {
                    File remoteBLOB = Utils.join(_objectsREMOTE, s);
                    try {
                        Files.copy(remoteBLOB.toPath(),
                                localBLOB.toPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            File localCOM = Utils.join(_commits, _currSHA);
            File remoteCOM = Utils.join(_commitsREMOTE, _currSHA);
            try {
                Files.copy(remoteCOM.toPath(), localCOM.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            setcurrentTOREMOTEID(_parent);
            setBLOBS();
        }
    }
    /** Helper method for setting up status. */
    private void statusSETUP() {
        _cwdL = Utils.plainFilenamesIn(_cwd);
        _branchesL = Utils.plainFilenamesIn(_branches);
        _removalL = Utils.plainFilenamesIn(_removal);
        _additionL = Utils.plainFilenamesIn(_addition);
        setcurrent(); String path = Utils.readContentsAsString(_head);
        File file = new File(path); setBLOBS();
        String current = file.getName(); System.out.println("=== Branches ===");
        for (String branch : _branchesL) {
            if (branch.compareTo(current) == 0) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
    }

    /** File object representing the current working directory ~. */
    private static File _cwd;
    /** File object representing the ~/.gitlet hidden directory. */
    private static File _gitlet;
    /** File object representing the ~/.gitlet/HEAD file location. HEAD stores
     * filepath to the head of the active branch. branches/active branch
     * eg. ~/.gitlet/branches/master */
    private static File _head;
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
    /** File object representing the local .gitlet/remotes directory that stores
     * remote files representing the path to each remote/.gitlet directory. */
    private static File _remotesLOCAL;
    /** File object representing the [remote path]/.gitlet directory. */
    private static File _gitletREMOTE;
    /** File object representing the [remote path]/.gitlet/objects directory. */
    private static File _objectsREMOTE;
    /** File object representing [remote path]/.gitlet/branches directory. */
    private static File _branchesREMOTE;
    /** File object representing the [remote path]/.gitlet/commits directory. */
    private static File _commitsREMOTE;

    /** Boolean representing if the .gitlet directory is present in _cwd. */
    private boolean _exists;
    /** The latest Commit object that was committed, the head. */
    private Commit _current;
    /** The current Commit object's SHA-1 UID. */
    private String _currSHA;
    /** The current Commit object's parent SHA-1 UID. */
    private String _parent;
    /** The current Commit object's second parent SHA-1 UID. */
    private String _secondparent;
    /** The current Commit's blobs HashMap. */
    private HashMap<String, String> _blobs;
    /** The name of a file to potentially be used. */
    private String _nameFILE;
    /** The name of the remote. */
    private String _remoteNAME;
    /** The path of the remote .gitlet directory. */
    private String _remotePATH;
    /** The name of the remote branch. */
    private String _remoteBRNCHNAME;
    /** The SHA-1 UID of the current commit of the remote branch. */
    private String _remoteCURRSHA;
    /** The file object for the remote branch. */
    private File _remoteBRNCHFILE;
    /** The file object for the local repository's pointer to the head of the
     * local copy of the remote branch. This will be stored in the local
     * .gitlet/branches */
    private File _localREMOTEBRNCHFILE;
    /** The boolean representing whether a merge of two branches encountered
     * any conflicts. By default this will be set to false. */
    private boolean _conflict;
    /** The name of the current branch. */
    private String _currBRNCHNAME;
    /** The name of the given branch when a merge command is called. */
    private String _givnBRNCHNAME;
    /** File object of the given branch when a merge command is called. */
    private File _givnBRNCHFILE;
    /** The given branch's head Commit object's SHA-1 UID when a merge command
     * is called. */
    private String _givnMERGESHA;
    /** The given branch's head Commit object when a merge command is called. */
    private Commit _givnMERGECOM;
    /** The given branch's Commit's blobs when a merge command is called. */
    private HashMap<String, String> _givnMERGEBLOBS;
    /** The current Commit object's SHA-1 UID when a merge command is called. */
    private String _currMERGESHA;
    /** The current Commit object when a merge command is called. */
    private Commit _currMERGECOM;
    /** The current Commit's blobs HashMap when a merge command is called. */
    private HashMap<String, String> _currMERGEBLOBS;
    /** The given Commit's ancestors HashMap that contains all of the ancestors
     * Commit objects indexed by their corresponding SHA-1 UID. It is filled
     * by the findgivnANCESTORS() method when a merge command is called. */
    private HashMap<String, Commit> _givnANCESTORS;
    /** The split-point commit's SHA-1 UID when a merge command is called. */
    private String _spltMERGESHA;
    /** The split-point's Commit object when a merge command is called. */
    private Commit _spltMERGECOM;
    /** The split-point Commit's blobs when a merge command is called. */
    private HashMap<String, String> _spltMERGEBLOBS;
    /** The current Commit's ancestors HashMap that contains all of the
     * ancestors Commit objects indexed by their corresponding SHA-1 UID. It is
     * filled by the findFIRSTANCESTORS() method when a push command is called.
     * This will then be used to update the remote */
    private HashMap<String, Commit> _currFIRSTANCESTORS;
    /** String basecase for use in fetch. */
    private String _basecase;
    /** String List for use in status. */
    private List<String> _cwdL;
    /** String List for use in status. */
    private List<String> _branchesL;
    /** String List for use in status. */
    private List<String> _removalL;
    /** String List for use in status. */
    private List<String> _additionL;
}
