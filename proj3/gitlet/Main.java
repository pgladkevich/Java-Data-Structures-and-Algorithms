package gitlet;

// import edu.neu.ccs.util.FileUtilities;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

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
            case "branch":
                branch(args);
                break;
            case "rm-branch":
                rmbranch(args);
                break;
            case "reset":
                reset(args);
                break;
            case "merge":
                merge(args);
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
        // TODO be able to handle merge commit creation
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
        // TODO figure out if this breaks under merge commit creation
        if (!addition.isEmpty()) {
            for (String name : addition) {
                File pot = Utils.join(_addition, name);
                byte[] blob = Utils.serialize(Utils.readContents(pot));
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
        System.out.println();
    }

    /** checkout: Checkout is a kind of general command that can do a few
     * different things depending on what its ARGS are. There are 3 possible
     * use cases.
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
            // Short UID
            if (sha.length() < 40) {
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
//            String shaf = _blobs.get(_nameFILE);
//            File source = Utils.join(_objects, shaf);
//            String input = Utils.readContentsAsString(source);
//            Utils.writeContents(dest, input);

            String shaf = _blobs.get(_nameFILE);
            String contents = getblobCONTENTS(shaf);
            Utils.writeContents(dest, contents);
        } else if (args.length == 2) {
            String branch = args[1];
            File check = Utils.join(_branches, branch);
            String path = Utils.readContentsAsString(_HEAD);
            File currBRANCH = new File(path);
            if (!check.exists()) {
                throw Utils.error("No such branch exists.", args[0]);
            } else if (branch.compareTo(currBRANCH.getName()) == 0) {
                throw Utils.error("No need to checkout the current " +
                        "branch.", args[0]);
            }
            setcurrent();
            HashMap<String, String> oldblobs = _current.get_blobs();
            String SHA = getBRANCHHEAD(branch);
            setcurrentTOID(SHA);
            setBLOBS();
            List<String> cwd = Utils.plainFilenamesIn(_cwd);
//            for (String name : cwd) {
//                if (_blobs.containsKey(name) && !oldblobs.containsKey(name)) {
//                    throw Utils.error("There is an untracked file in the "
//                            + "way; delete it, or add and commit it first.");
//                }
//            }

            // For each file in the head commit of the given branch, copy/overwrite
            //     * the file in the working directory.
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
            // If a file is in the commit of the current branch but not in the
            // specified branch, delete it from the current working directory.
            for (Map.Entry mapElement : oldblobs.entrySet()) {
                String name = (String) mapElement.getKey();
                if (!_blobs.containsKey(name)) {
                    File file = Utils.join(_cwd, name);
                    file.delete();
                }
            }
            // clear the staging area.
            clearSTAGING();
            // Last step to set the current branch the the checked out branch
            updateHEAD(branch);
        } else {
            throw Utils.error("Incorrect operands.", args[0]);
        }
    }

    /** Create a new branch(reference to a SHA-1 identifier) with the given
     * name and point it at the current head node. This command does NOT
     * immediately switch to the newly created branch.
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
     * given commit.
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
        String SHA = args[1];
        File commit = Utils.join(_commits, SHA);
        if (!commit.exists()) {
            throw Utils.error("No commit with that id exists.", args[0]);
        }
        setcurrent();
        setBLOBS();
        HashMap<String, String> oldblobs = _current.get_blobs();
        String branch = getbranchCURRENT();
        setcurrentTOID(SHA);
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
        updateBRANCH(branch, SHA);
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
     * is neither of the above, proceed with the steps below.
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
        if (args.length != 2) {
            throw Utils.error("Incorrect operands.", args[0]);
        }
        _conflict = false;
        _givnBRNCHNAME = args[1];
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
        findgivnANCESTORS(_parent);
        findgivnANCESTORS(_secondparent);
        findsplitPOINT();
    }

    /** Helper method for updating the HEAD file for the passed
     * in ACTIVEBRANCH. */
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
        _secondparent = _current.get_secondparent();
    }
    /** Helper method for setting the _current Commit to provided ID. */
    public void setcurrentTOID(String SHA) {
        File commit = Utils.join(_commits, SHA);
        _currSHA = SHA;
        _current = Utils.readObject(commit, Commit.class);
        _parent = _current.get_parent();
        _secondparent = _current.get_secondparent();
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
    /** Helper method for de-serializing and retrieving the contents of the
     * blob whose SHA ID was passed in. Returns the string representing said
     * contents. */
    public String getblobCONTENTS(String SHA) {
        File source = Utils.join(_objects, SHA);
        byte[] bytec = Utils.readObject(source, byte[].class);
        return new String(bytec);
    }

    /** Helper method for getting the name of the currently active BRANCH. */
    public String getbranchCURRENT() {
        String path = Utils.readContentsAsString(_HEAD);
        File branch = new File(path);
        String name = branch.getName();
        return name;
    }
    /** Helper method for getting the SHA-1 ID of the head of the given
     * BRANCH. */
    public String getBRANCHHEAD(String branch) {
        File head = Utils.join(_branches, branch);
        String SHA = Utils.readContentsAsString(head);
        return SHA;
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
    /** Helper method for writing the contents of passed in file NAME into the
     * user's current working directory. Contents are retrieved from _objects
     * using the passed in SHA ID to get the corresponding blob. */
    public void writeblobTOCWD(String NAME, String SHA) {
        File dest = Utils.join(_cwd, NAME);
        String contents = getblobCONTENTS(SHA);
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
     * given branch. This method fills out the _givnANCESTORS hashmap.  */
    private void findgivnANCESTORS(String SHA) {
        if (SHA == null) {
            return;
        }
        setcurrentTOID(SHA);
        if (!_givnANCESTORS.containsKey(_currSHA)) {
            _givnANCESTORS.put(_currSHA, _current);
            findgivnANCESTORS(_parent);
            findgivnANCESTORS(_secondparent);
        }
    }
    /** Helper method for the merge command to find the first */
    private void findsplitPOINT() {

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
    /** The current Commit object's second parent SHA-1 UID. */
    private String _secondparent;
    /** The current Commit's blobs HashMap. */
    private HashMap<String, String> _blobs;
    /** The named of the file to potentially be used. */
    private String _nameFILE;
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


}
