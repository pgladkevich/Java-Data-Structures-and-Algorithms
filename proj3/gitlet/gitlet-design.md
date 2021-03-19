# Gitlet Design Document

**Name**: Pavel Gladkevich

## Classes and Data Structures
### Commit
#### Instance Variables
   * Blobs - HashMap<String,String> of file names mapped to SHA1 blob hash values
   * Message - String that contains the message of the commit.
   * Parent - String of the SHA1 value of the parent commit for the commit object. 
   * Millitime - Long representing millisecond time at which the commit was created. Assigned by the constructor.
   * Second Parent - String of second parent commits in the case of merge commit.

## Algorithms
   1. init: Create a new .gitlet directory, the initial commit object, HEAD file with branch “master” pointing to 
   initial commit. Also create empty Objects directory, branches directory that contains SHA-1 ID of the initial 
   commit from the master branch, , _staging directory and _addition & _removal subdirectories, as well as, _commits 
   directory. This concludes the setup of the .gitlet persistence architecture.                                                                                                                                                                                                                                                             *  architecture. 
       * Failure Cases: If there is already a .gitlet directory present in the current working directory, abort.
      
   2. add: Add a copy of it to the addition 
   subdirectory of staging. If the file is already in the staging area, override the old file with the new contents. If
   the file is in the staging area and the contents of the file are the same as the version from the last commit, 
   remove it from the staging area. If the file was staged for removal it will be removed from the removal subdirectory.
       * Failure Cases: If file does not exist in the current working directory, abort.
       
   3. commit: If staging area is empty or message is empty, abort. Create a new commit whose contents are by default 
   the same as the current commit. Add any files in the staging area that are not in the current commit. For files in 
   the addition subdirectory that are contained in the current commit, compare hash values of files in new and current 
   commit, adding any files whose hash value is not in the current commit. For files in the removal subdirectory remove
    the files from being tracked in the next commit. Clear the staging area and add new commit to branch, updating 
    the HEAD.
   
   4. rm: Search addition area for the file. If it is present, remove it, thus unstaging the file for addition. 
   If it is being tracked by the current commit, stage it for removal by adding it to the removal directory and 
   delete it from current working directory if the user has not already done so (do not remove it unless it is 
   tracked in the current commit).
      * Failure Cases: If the file is neither staged nor tracked by the head commit, print the error message "No reason 
      to remove the file."
   
   5. log: For each commit in the tree starting from head, print the commit’s information, and follow the 
   commit’s FIRST parent pointer. In the case of merge commits the second parent is ignored. For merge commits 
   (those that have two parent commits) add a line just below the first where the two hexadecimal numerals following 
   "Merge:" consist of the first seven digits of the first and second parents' commit ids, respectively.
   
   6. global-log: For each commit ever made, print the commit’s information. Output will be unordered.
   
   7. find: For each commit that exists, if the message passed in matches the message for the current commit, print the
    id of the commit on a new line.
        * Failure cases: If no such commit exists, prints the error message "Found no commit with that message."
   
   8. status: Print out names of each branch, and mark the current branch with an asterisk. Print names of files in 
   the addition directory, and then the files in the removal directory. Print names of files which are modified but not
   staged by checking for the following: 
       1. If the file IS in cwd, IS tracked, and NOT staged. Meaning, the contents of the file in the working directory 
       and current commit differ AND the file is not in staging area.
       2. If the file IS in cwd and IS staged. Meaning, the contents staged for addition differ from those in the 
       working directory. 
       3. If the file is NOT in cwd and IS staged. Meaning, the file was deleted in the working directory but is staged
        for addition.  
       4. If the file is NOT in cwd, IS tracked, and is NOT staged. Meaning, the file is not staged for removal, is 
       deleted from the working directory, but it is IN the current commit.
   Lastly, print un-tracked files by: checking all files that are IN the working directory, but are not in the staging 
   area or commit. This includes files that have been staged for removal, but then re-created without Gitlet's 
   knowledge. Ignore any subdirectories that may have been introduced, since Gitlet does not deal with them.
   
   9. checkout: Checkout is a kind of general command that can do a few different things depending on what its 
   arguments are. There are 3 possible use cases.
       1. Search for file in list of files in head commit, and copy/overwrite the file into the working directory.
       The new version of the file is not staged.
           * Failure Cases: If the file does not exist in the previous commit, abort, printing the error message 
               "File does not exist in that commit." 
       2. Search for file in list of files in the specified commit, and copy/overwrite the file in the 
       working directory. The new version of the file is not staged.
           * Failure Cases: If no commit with the given id exists, print No commit with that id exists. Otherwise, if 
           the file does not exist in the given commit, print the same message as for failure case 1.        
       3. For each file in the head commit of the given branch, copy/overwrite the file in the working directory. 
       If a file is in the commit of the current branch but not in the specified branch, delete it from the 
       current working directory. If the current and given branches are different, clear the staging area. Set the given 
       branch to head.
           * Failure Cases: If no branch with that name exists, print No such branch exists. 
           If that branch is the current branch, print No need to checkout the current branch. If a working file is 
           un-tracked in the current branch and would be overwritten by the checkout, print There is an un-tracked file 
           in the way; delete it, or add and commit it first. and exit; perform this check before doing anything else.
   
   10. branch: Create a new branch(reference to a SHA-1 identifier) with the given name and point it at the current 
   head node. This command does NOT immediately switch to the newly created branch.
       * Failure cases: If a branch with the given name already exists, print the error message "A branch with that 
       name already exists."
   
   11. rm-branch: Remove specified branch from list of branches. Deletes the branch with the given name. Deleting only 
   the pointer associated with the branch; not any commits created under the branch, or anything like that.
       * Failure Cases: If a branch with the given name does not exist, aborts. Print the error message 
       "A branch with that name does not exist." If you try to remove the branch you're currently on, aborts, printing 
       the error message "Cannot remove the current branch."
   
   12. reset: For each file in the given commit, call checkout [file name] on it. Set the head of the current branch 
   to the given commit.
       * Failure Cases: If no commit with the given id exists, print "No commit with that id exists." If a working file
       is untracked in the current branch and would be overwritten by the reset, print "There is an untracked file in 
       the way; delete it, or add and commit it first." and exit; perform this check before doing anything else.
   
   13. merge: Merges files from the given branch into the current branch. Retrieve the head Commit of both branches. 
   If these two do not point to the same commit, then traverse the entire tree of the given branch, storing the Commits
   in a HashMap<String, Commit> with the key being the SHA value. Then, traverse the current branch until you find the
   first shared commit. Set that commit to be the split point, which is the latest common ancestor of both branches.
   * If the split point is the same commit as the given branch's head, do nothing. The merge is complete, and the 
       operation ends with the message "Given branch is an ancestor of the current branch." 
   * If the split point is the current branch , then the effect is to check out the given branch, and the operation 
   ends after printing the message "Current branch fast-forwarded." If the split point is neither of the above, proceed
   with the steps below. If at any point a conflict is encountered, set the boolean _conflict to true, and concatenate
   the contents of the file in the current branch with the contents of the version in the given branch.
       1. Iterate through each filename in the given branch that represents a file corresponding to a blob. 
           1. If the file is absent from the split-point and the current branch, checkout the file from the given branch 
           and stage for addition.
           2. If the file is absent from the current branch, and not modified from the version in the split-point, it
           should remain absent --> no action. 
           3. If the file is absent from the current branch, and modified from the version in the split-point --> it is
           a conflict. 
           4. If the file in the given branch is not modified from the split-point, but the file is modified in the
           current branch, it should remain as is --> no action.
           5. If the file is modified in the given branch, but is the same version in the current branch as from the
           split-point, checkout the file from the given branch and stage it for addition. 
           6. If the file is modified in the given branch in the same way as the current branch --> no action.
           7. If the file is modified in the given branch in a different way from the modification in the current branch
           --> conflict.
       2. Iterate through every file in the current branch. 
           1. If the file is in both the current branch and the split-point (not modified), and is absent in the given
           branch, call the rm command on the file.
           2. If the file is modified in the current branch and absent from the given branch --> conflict.        
       * Failure Cases: If there are staged files present (in addition or removal) print the error message 
       "You have uncommitted changes." If a branch with the given name does not exist, print the error message
        "A branch with that name does not exist." If attempting to merge a branch with itself, print the error message 
        "Cannot merge a branch with itself." If an un-tracked file in the current commit would be overwritten or 
        deleted by the merge, print "There is an untracked file in the way; delete it, or add and commit it first."
        Perform this check before doing anything else. 
   * Once the files have been updated, merge automatically calls the commit command with the message 
   "Merged [given branch name] into [current branch name]". Then, if the merge encountered a conflict, print the message
   "Encountered a merge conflict." to the terminal. The resulting commit will have the current branch as 
   its parent, and the given branch as its second parent.

#### Extra Credit Algorithms
   1. add-remote: Saves the given login information under the given remote name in a file in the remotes subdirectory 
   of .gitlet. In this case the login information is simply the absolute path to the remote directory. Attempts to push 
   or pull from the given remote name will then attempt to use this .gitlet directory. By writing, e.g., 
   java gitlet.Main add-remote other ../testing/otherdir/.gitlet you can provide tests of remotes that will work from 
   all locations.
       * Usage: `java gitlet.Main add-remote [remote name] [name of remote directory]/.gitlet
       * Failure cases: If a remote with the given name already exists, print the error message: 
       "A remote with that name already exists."
   2. rm-remote: Remove information associated with the given remote name. If you ever want to change a remote's 
   information the rm-remote command will be called and then the remote will be re-added. 
       * Usage: java gitlet.Main rm-remote [remote name]
       * Failure cases: If a remote with the given name does not exist, print the error message: 
       "A remote with that name does not exist."
   3. push: Attempts to append the current branch's commits to the end of the given branch at the given remote. This 
   command only works if the remote branch's head is in the history of the current local head, which means that the 
   local branch contains some commits in the future of the remote branch. In this case, append the future commits to 
   the remote branch. Then, the remote should reset to the front of the appended commits (so its head will be the same 
   as the local head). This is called fast-forwarding. If the Gitlet system on the remote machine exists, but does not 
   have the input branch, then simply add the branch to the remote Gitlet.
       * Usage: java gitlet.Main push [remote name] [remote branch name]
       * Failure cases: If the remote branch's head is not in the history of the current local head, print the error 
       message "Please pull down remote changes before pushing." If the remote .gitlet directory does not exist, print 
       "Remote directory not found."
   4. fetch: Brings down commits from the remote Gitlet repository into the local Gitlet repository. Copies all commits 
   and blobs from the given branch in the remote repository (that are not already in the current repository) into a 
   branch named [remote name]/[remote branch name] in the local .gitlet, changing the branch 
   [remote name]/[remote branch name] to point to the head commit of the remote branch. 
       1. First, checks if the branch to be created already exists. If it does, then get the Commit corresponding to the
       local head of this branch. Check if this commit's SHA exists in the remote repo. If it doesn't this means the 
       local is ahead of the remote and fetch does not need to be performed. Exit. Otherwise, set this commit's parent 
       to be the base case. If the branch does not exist then the base case is simply null, the parent of the initial 
       commit. 
       2. Retrieve the SHA-1 ID of the head of the remote branch using a helper function.
       3. Create the branch in the local repository if it did not previously exist. In both cases set the head of the
       local copy of the remote branch (change the SHA of the _branches/[remote name]/[remote branch name] file) to the
       retrieved SHA-1 ID.
       4. Retrieve the remote commit corresponding to the aforementioned SHA UID and set it to be the current commit as
       well as the current blobs. 
       5. For each blob in the commit, check if it is already present in the local _objects directory. If not, copy the
       file over. 
       6. Once all blobs have been copied (if not present), copy the serialized Commit file from the remote to the the
       local _commits directory. Then, follow the parent pointer of the current commit and repeat steps 4-6 until the 
       base case is reached (either null or parent of head of local copy of remote). **Base case checked at the start.**
       * Usage: java gitlet.Main fetch [remote name] [remote branch name]
       * Failure cases: If the remote Gitlet repository does not have the given branch name, print the error message 
       "That remote does not have that branch." If the remote .gitlet directory does not exist, print: 
       "Remote directory not found."
   5. pull: Fetches branch [remote name]/[remote branch name] as for the fetch command, and then merges that fetch 
   into the current branch.
       * Usage: java gitlet.Main pull [remote name] [remote branch name]
       * Failure cases: Just the failure cases of fetch and merge together.
        
## Persistence
| cwd + files | In .gitlet | In .gitlet subdirectories | In Staging Subdirectory |
| --------- | ---------- | ---------- | ---------- |
| -- .gitlet directory | 
| | -- HEAD file |
| | -- Objects Directory | -- Every Blob |
| | -- Branches Directory | -- Head commit of each branch |
| | -- Commits Directory | -- Every Commit |
| | -- Staging Directory | -- addition subdirectory | files staged for addition |
| | -- Staging Directory | -- removal subdirectory | files staged for removal |
| | -- Remotes Directory | -- Every Remote |

Inspiration/References: 

https://inst.eecs.berkeley.edu/~cs61b/sp20/materials/proj/proj3

https://www.researchgate.net/post/Can_someone_help_with_a_definition_of_information_hiding_and_encapsulation

https://john.cs.olemiss.edu/~hcc/researchMethods/notes/ClassicParnas/ACMannotated/ClassicParnasRevisionAnnotated.pdf

https://blog.jayway.com/2013/03/03/git-is-a-purely-functional-data-structure/

https://paper.dropbox.com/doc/Gitlet-Persistence--Ay9ecQH7azheEFUnMaQelMXjAg-zEnTGJhtUMtGr8ILYhoab
