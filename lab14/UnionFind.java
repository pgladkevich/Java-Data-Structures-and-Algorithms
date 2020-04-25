
/** Disjoint sets of contiguous integers that allows (a) finding whether
 *  two integers are in the same set and (b) unioning two sets together.  
 *  At any given time, for a structure partitioning the integers 1 to N, 
 *  into sets, each set is represented by a unique member of that
 *  set, called its representative.
 *  @author
 */
public class UnionFind {

    /** A union-find structure consisting of the sets { 1 }, { 2 }, ... { N }.
     */
    public UnionFind(int N) {
        _parents = new int[N+1];
        _sizes = new int[N+1];
        for (int i = 0; i < _parents.length; i += 1) {
            _parents[i] = i;
            _sizes[i] = 1;
        }
    }

    /** Return the representative of the set currently containing V.
     *  Assumes V is contained in one of the sets.  */
    public int find(int v) {
        if(_parents[v] == v) {
            return _parents[v];
        } else {
            return find(_parents[v]);
        }
    }

    /** Return true iff U and V are in the same set. */
    public boolean samePartition(int u, int v) {
        return find(u) == find(v);
    }

    /** Union U and V into a single set, returning its representative. */
    public int union(int u, int v) {
        int p1 = find(u), p2 = find(v);
        if (p1 == p2) {
            return p1;
        }
        int s1 = _sizes[p1], s2 = _sizes[p2];
        if (s1 > s2) {
            _sizes[p1] += s2;
//            int i = 1;
//            while (s2 > 0) {
//                if (_parents[i] == p2) {
//                    _parents[i] = p1;
//                    s2 -= 1;
//                }
//                i += 1;
//            }
            _parents[p2] = p1;
            return s1;
        } else {
            _sizes[p2] += s1;
//            int j = 1;
//            while (s1 > 0) {
//                if (_parents[j] == p1) {
//                    _parents[j] = p2;
//                    s1 -= 1;
//                }
//                j += 1;
//            }
            _parents[p1] = p2;
            return s2;
        }
    }

    /** An Array that stores the parents */
    int[] _parents;
    /** An Array that stores the sizes */
    int[] _sizes;
}
