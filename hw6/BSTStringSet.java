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
        if (!contains(s)) {
            _root = putHelper(_root, s);
        }
    }
    private Node putHelper(Node node, String s) {
        if (node == null) {
            return new Node(s);
        }
        if (node.compare(s) > 0) {
            node.setLeft(putHelper(node.getLeft(node), s));
        }
        else {
            node.setRight(putHelper(node.getRight(node), s));
        }
        return node;
    }

    @Override
    public boolean contains(String s) {
        if (_root == null) {
            return false;
        }
        else {
            BSTIterator I = new BSTIterator(_root);
            while(I.hasNext()) {
                if (s.compareTo(I.next()) == 0) {
                    return true;
                }
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
            addTree(node.right);
            return node.s;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        /** Add the relevant subtrees of the tree rooted at NODE. */
        private void addTree(Node node) {
            while (node != null && node.compare(_high) > 0) {
                node = node.left;
            }
            while (node != null && node.compare(_low) >= 0) {
                _toDo.push(node);
                node = node.left;
            }
        }
    }

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
