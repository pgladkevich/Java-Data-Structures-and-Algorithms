import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;

/**
 * Implementation of a BST based String Set.
 * @author Pavel Gladkevich
 */
public class BSTStringSet implements StringSet, SortedStringSet,
        Iterable<String> {
    /** Creates a new empty set. */
    public BSTStringSet() {
        _root = null;
    }

    @Override
    public void put(String s) {
        if (_root == null) {
            _root = new Node(s);
        }
        else if (!contains(s)) {
            _root = putHelper(_root, s);
        }
    }
    private Node putHelper(Node node, String s) {
        Node prevNode = node;
        Node currNode = node;
        while (currNode != null) {
            if (currNode.compare(s) > 0) {
                prevNode = currNode;
                currNode = currNode.getLeft(node);
            }
            else {
                prevNode = currNode;
                currNode = currNode.getRight(node);
            }
        }
        if (prevNode.compare(s) > 0) {
            prevNode.setLeft(new Node(s));
        } else {
            prevNode.setRight(new Node(s));
        }
        return node;
    }

    @Override
    public boolean contains(String s) {
        Node curr = _root;
        while (curr != null) {
            if (curr.compare(s) == 0) {
                return true;
            } else if (curr.compare(s) > 0) {
                curr = curr.getLeft(curr);
            } else {
                curr = curr.getRight(curr);
            }
        }
        return false;
    }

    @Override
    public List<String> asList() {
        ArrayList<String> aL = new ArrayList<String>();
        BSTIterator I = new BSTIterator(_root);
        while(I.hasNext()) {
            aL.add(I.next());
        }
        return aL;
    }


    /** Represents a single Node of the tree. */
    private static class Node {
        /** String stored in this Node. */
        private String s;
        /** Left child of this Node. */
        private Node left;
        /** Right child of this Node. */
        private Node right;

        /** Creates a Node containing SP. */
        Node(String sp) {
            s = sp;
        }

        public int compare(String s2) {
            return s.compareTo(s2);
        }

        public void setRight(Node right) {
            this.right = right;
        }

        public void setLeft(Node left) {
            this.left = left;
        }

        public Node getRight(Node right) {
            return this.right;
        }

        public Node getLeft(Node left) {
            return this.left;
        }
    }

    /** An iterator over BSTs. */
    private static class BSTIterator implements Iterator<String> {
        /** Stack of nodes to be delivered.  The values to be delivered
         *  are (a) the label of the top of the stack, then (b)
         *  the labels of the right child of the top of the stack inorder,
         *  then (c) the nodes in the rest of the stack (i.e., the result
         *  of recursively applying this rule to the result of popping
         *  the stack. */
        private Stack<Node> _toDo = new Stack<>();

        /** A new iterator over the labels in NODE. */
        BSTIterator(Node node) {
            addTree(node);
        }

        @Override
        public boolean hasNext() {
            return !_toDo.empty();
        }

        @Override
        public String next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            Node node = _toDo.pop();
            addTree(node.right);
            return node.s;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        /** Add the relevant subtrees of the tree rooted at NODE. */
        private void addTree(Node node) {
            while (node != null) {
                _toDo.push(node);
                node = node.left;
            }
        }
    }

    /** An iterator over a range in a BSTs. */
    private static class sortedIterator implements Iterator<String> {
        /** Stack of nodes to be delivered.  The values to be delivered
         *  are (a) the label of the top of the stack, then (b)
         *  the labels of the right child of the top of the stack inorder,
         *  then (c) the nodes in the rest of the stack (i.e., the result
         *  of recursively applying this rule to the result of popping
         *  the stack. */
        private Stack<Node> _toDo = new Stack<>();
        public String _low;
        public String _high;

        /** A new iterator over the labels in NODE. */
        sortedIterator(Node node, String low, String high) {
            _low = low;
            _high = high;
            addTree(node);
        }

        @Override
        public boolean hasNext() {
            return !_toDo.empty();
        }

        @Override
        public String next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            Node node = _toDo.pop();
            //addTree(node.right);
            return node.s;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        /** Add the relevant subtrees of the tree rooted at NODE. */
        private void addTree(Node node) {
            while (node != null) {
                if (node.compare(_low) >= 0 && node.compare(_high) < 0) {
                    addTree(node.right);
                    _toDo.add(node);
                    addTree(node.left);
                }
                if (node.compare(_low) < 0 && node.compare(_high) < 0) {
                    addTree(node.right);
                }
                if (node.compare(_low) >= 0 && node.compare(_high) >= 0) {
                    addTree(node.left);
                }
                if (node.compare(_low) < 0 && node. compare(_high) >= 0) {
                    addTree(node.left);
                    addTree(node.right);

                } else {
                    break;
                }
            }
        }
    }

//    private void addTree(Node node) {
//        while (node != null && node.compare(_high) >= 0) {
//            node = node.left;
//        }
//        while (node != null) {
//            if (node.compare(_low) >= 0) {
//                _toDo.push(node);
//            }
//            if (node.left != null
//                    && node.left.compare(_low) < 0
//                    && node.left.right != null) {
//                addRight(node.left.right);
//                break;
//            }
//            if (node.right != null
//                    && node.right.compare(_high) >= 0) {
//                searchLeft(node.right.left);
//                break;
//            }
//            node = node.left;
//        }
//    }
//    private void addRight(Node node) {
//        while (node!= null) {
//            if (node.compare(_low) >= 0) {
//                addTree(node);
//            }
//            node = node.right;
//        }
//    }
//    private void searchLeft(Node node) {
//        while (node!= null) {
//            if (node.compare(_low) >= 0 && node.compare(_high) < 0) {
//                addTree(node);
//            }
//            node = node.left;
//        }
//    }
//}

    @Override
    public Iterator<String> iterator() {
        return new BSTIterator(_root);
    }
    @Override
    public Iterator<String> iterator(String low, String high) {
        return new sortedIterator(_root, low, high);
    }


    /** Root node of the tree. */
    private Node _root;
}
