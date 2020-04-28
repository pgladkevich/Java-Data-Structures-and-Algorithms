# Gitlet Design Document

**Name**: Pavel Gladkevich

    Lab 13 Partner's name: Zain Hasnain
    Lab 13 Partner's SID: 3033718042
    Lab 13 Partner's email: zainhasnain@berkeley.edu
    
## Classes and Data Structures
### Commit
#### Instance Variables
   * Blobs - HashMap<String,String> of file names mapped to SHA1 blob hash values
   * Message - String that contains the message of the commit.
   * Parent - String of the SHA1 value of the parent commit for the commit object. String of parent commits (2 parents) in case of merge commit.
   * Millitime - Long representing millisecond time at which the commit was created. Assigned by the constructor.

## Algorithms
   1. init: If there is already a .gitlet directory present, abort. Otherwise, create a new .gitlet directory, the initial commit, HEAD file with branch “master” pointing to initial commit. Also create empty Objects directory, branches directory that contains SHA-1 ID of the initial commit from the master branch. 
   
   2. add: If file does not exist in the current working directory, abort. Otherwise, add a copy of it to the addition subdirectory of staging. If the file is already in the staging area, override the old file with the new contents. If the file is in the staging area and the contents of the file are the same as the version from the last commit, remove it from the staging area. If the file was staged for removal it will be removed from the removal subdirectory.
   
   3. commit: If staging area is empty or message is empty, abort. Create a new commit whose contents are by default the same as the current commit. Add any files in the staging area that are not in the current commit. For files in the addition subdirectory that are contained in the current commit, compare hash values of files in new and current commit, adding any files whose hash value is not in the current commit. For files in the removal subdirectory remove the files from being tracked in the next commit. Clear the staging area and add new commit to branch, updating the HEAD.
   4. rm: Search staging area for file’s hash. If present, remove it. If it is part of the current commit, add it to the removal directory and delete it from working directory.
   5. log: For each commit in the tree starting from head, print the commit’s information (toString), and follow the commit’s FIRST parent pointer.
   6. global-log: For each commit in the tree starting from head, print the commit’s information (toString), recursively call on the first parent, then on the second parent.
   7. find: For each commit in the tree, if the message passed in matches the message for the current commit, print the id of the commit on a new line.
   8. status: Print out names of each branch. If the head of the commit tree matches the head of a branch, put an asterisk next to it when printing. Print names of files in the addition directory, and then the files in the removal directory. Print names of files which are modified but not staged by: checking if the contents of the file in the working directory and current commit differ AND file is not in staging area, if it is in the commit but not in the working directory, or if it is not in the removal directory or working directory but it is in the current commit. Print untracked files by: checking all files in the working directory but not in the staging area or commit.
   9. checkout: 
            1. Search for file in list of files in head commit, and copy/overwrite the file in the working directory.
            2. Search for file in list of files in specified commit, and copy/overwrite the file in the working directory.
            3. For each file in the head commit of the given branch, copy/overwrite the file in the working directory. If a file is in the commit of the current branch but not in the specified branch, delete it from the current branch. If the current and given branches are different, clear the staging area. Set the given branch to head.
   10. branch: Create a new branch (holds the SHA-1 identifier) holding the head node hash.
   11. rm-branch: Remove specified branch from list of branches.
   12. reset: For each file in the given commit, call checkout [fileName] on it. Set the branch which is pointing to head to the given commit.
   13. merge: ***Needs to be revisited*** Set up two Commit pointers for the current branch and the given branch. While these two do not point to the same commit, follow the current commit’s first parent pointer. Once they are equal, set that commit to split point. For each file in the current branch, if it is equal to the file in the split point but not in the current branch, check the file out from the given branch. For every file in the given branch commit, if it was not present at the split point or in the current branch, check the file out and add it to staging addition area. For every file in the split point and the current branch commit but not in the given branch commit, remove the file and add it to the removal directory. For all files in the current branch, if the file is different in the given branch, print the differences for both branches. 
## Persistence
    1. .gitlet directory
        1. program-driving data
            1. commit tree
                1. all commit objects
        2. staging area directory (“index”)
            1. one directory for additions, one for removals
            2. contains references to files / commits
        3. branches directory

Inspiration/References: 

https://www.researchgate.net/post/Can_someone_help_with_a_definition_of_information_hiding_and_encapsulation

https://john.cs.olemiss.edu/~hcc/researchMethods/notes/ClassicParnas/ACMannotated/ClassicParnasRevisionAnnotated.pdf

https://blog.jayway.com/2013/03/03/git-is-a-purely-functional-data-structure/


## Maybe don't need 
#### Commit Tree (HashMap)
#### Instance Variables
        1. contains the history of all commits through a parent commit pointer (stores SHA-1 value)
        2. head pointer for current commit