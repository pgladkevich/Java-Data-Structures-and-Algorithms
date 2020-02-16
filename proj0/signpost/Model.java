package signpost;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Arrays;

import static signpost.Place.pl;
import static signpost.Place.PlaceList;
import static signpost.Utils.*;

/** The state of a Signpost puzzle.  Each cell has coordinates (x, y),
 *  where 0 <= x < width(), 0 <= y < height().  The upper-left corner
 *  of the puzzle has coordinates (0, height() - 1), and the lower-right
 *  corner is at (width() - 1, 0).
 *
 *  A constructor initializes the squares according to a particular
 *  solution.  A solution is an assignment of sequence numbers from 1
 *  to size() == width() * height() to square positions so that
 *  squares with adjacent numbers are separated by queen moves. A
 *  queen move is a move from one square to another horizontally,
 *  vertically, or diagonally. The effect is to give each square whose
 *  number in the solution is less than size() an <i>arrow
 *  direction</i>, 1 <= d <= 8, indicating the direction of the next
 *  higher numbered square in the solution: d * 45 degrees clockwise
 *  from straight up (i.e., toward higher y coordinates).  Thus,
 *  direction 1 is "northeast", 2 is "east", ..., and 8 is "north".
 *  The highest-numbered square has direction 0.  Certain squares can
 *  have their values <i>fixed</i> to those in the solution.
 *  Initially, the only two squares with fixed values are those with
 *  the lowest and highest sequence numbers in the solution.  Before
 *  the puzzle is presented to the user, however, the program fixes
 *  other numbers so as to make the solution unique for the given
 *  arrows.
 *
 *  At any given time after initialization, a square whose value is
 *  not fixed may have an unknown value, represented as 0, or a
 *  tentative number (not necessarily that of the solution) between 1
 *  and size(). Squares may be connected together, indicating that
 *  their sequence numbers (unknown or not) are consecutive.
 *
 *  When square S0 is connected to S1, we say that S1 is the
 *  <i>successor</i> of S0, and S0 is the <i>predecessor</i> of S1.
 *  Sequences of connected squares with as-yet unknown values (denoted
 *  by 0) form a <i>group</i>, identified by a unique <i>group
 *  number</i>.  Numbered cells (whether linked or not) are in group
 *  0.  Unnumbered, unlinked cells are in group -1.  On the board displayed
 *  to the user, cells in the same group indicate their grouping and sequence
 *  with labels such as "a", "a+1", "a+2", etc., with a different letter
 *  for each different group.  The first square in a group is called the
 *  <i>head</i> of the group.  Each unnumbered square points to the head
 *  of its group (if the square is unlinked, it points to itself).
 *
 *  Squares are represented as objects of the inner class Sq
 *  (Model.Sq).  A Model object is itself iterable. That is, if M is
 *  of type Model, one can write
 *       for (Model.Sq s : M) { ... }
 *  to sequence through all its squares in unspecified order.
 *
 *  The puzzle is solved when all cells are contained in a single
 *  sequence of consecutively numbered cells (therefore all in group
 *  0) and all cells with fixed sequence numbers appear at the
 *  corresponding position in that sequence.
 *
 *  @author Pavel Gladkevich
 */
class Model implements Iterable<Model.Sq> {

    /** A Model whose solution is SOLUTION, initialized to its
     *  starting, unsolved state (where only cells with fixed numbers
     *  currently have sequence numbers and no unnumbered cells are
     *  connected).  SOLUTION must be a proper solution:
     *    1. It must have dimensions w x h such that w * h >= 2.
     *    2. There must be a sequence of chess-queen moves such that the
     *       sequence of values in the cells reached is 1, 2, ... w * h.
     *       The contents of SOLUTION are copied into a fresh array in
     *       this Model, so that subsequent changes to SOLUTION have no
     *       effect on the Model.  */
    Model(int[][] solution) {
        if (solution.length == 0 || solution.length * solution[0].length < 2) {
            throw badArgs("must have at least 2 squares");
        }
        _width = solution.length; _height = solution[0].length;
        int last = _width * _height;
        BitSet allNums = new BitSet();
        _allSuccessors = Place.successorCells(_width, _height);
        _solution = new int[_width][_height];
        deepCopy(solution, _solution);
        _board = new Sq [_width][_height];
        int x0,y0, sequenceNum, dir, group;
        boolean fixed = false;

        for (int col  = 0, index = 0; col < _solution.length; col += 1) {
            for (int row = 0; row < _solution[col].length; row += 1) {
                x0 = col;
                y0 = row;
                sequenceNum = _solution[x0][y0];
                dir = arrowDirection(x0,y0);
                if (sequenceNum == 1 || sequenceNum == last){
                    fixed = true;
                    group = 0;
                    _board[col][row] = new Sq (
                            x0,y0,sequenceNum,fixed,dir,group);
                    _allSquares.add(_board[col][row]);
                    fixed = false;
                } else {
                    group = -1;
                    _board[col][row] = new Sq (
                            x0,y0,0,fixed,dir,group);
                    _allSquares.add(_board[col][row]);
                }
            }
        }
        _solnNumToPlace = new Place[last+1];
        for (int index = 0;index < last+1; index +=1) {
            if (index ==0) {
                _solnNumToPlace[index] = null;
            } else {
                int[] coords = findCoords(index, _solution);
                _solnNumToPlace[index] = pl(coords[0], coords[1]);
            }
        }
        for (Place current : _solnNumToPlace) {
            if (current == null) {
                continue;
            } else if (_solution[current.x][current.y] < 1 || _solution[current.x][current.y] > last) {
                    throw badArgs("IllegalArgumentException");
            }
        }
        for (Sq current : _allSquares) {
            if (current.sequenceNum() == last) {
                current._successors = null;
            } else {
                PlaceList[][][] N = Place.successorCells(_width, _height);
                current._successors = N[current.x][current.y][current._dir];
            }
        }
        for (Sq current : _allSquares) {
            if (current.sequenceNum() == 1) {
                current._predecessors = null;
            } else {
                  PlaceList[][][] P = Place.successorCells(_width, _height);
                  PlaceList P_reduced = P[current.x][current.y][0];
                  PlaceList P_further_reduced = new PlaceList();
                  for (Place curr_P : P_reduced) {
                      Sq check = _board[curr_P.x][curr_P.y];
                      int dir_to = signpost.Place.dirOf(check.x,check.y,current.x,current.y);
                      if (dir_to == check._dir) {
                          P_further_reduced.add(curr_P);
                      }
                  }
                  current._predecessors = P_further_reduced;
            }
        }
        _unconnected = last - 1;
    }

    /** Initializes a copy of MODEL.
     * Initialize _board and _allSquares to contain copies of the Sq objects in
     * MODEL other than their _successor, _predecessor, and _head fields
     * (Which can't be set until all the Sq objects are first created.)
     * Create Sq[][] _board based off of the _solution variable
     * Sq(int x0, int y0, int sequenceNum, boolean fixed, int dir, int group)
     *
     *       Once all the new Sq objects are in place, fill in their
     *        successor, _predecessor, and _head fields.  For example,
     *        if in MODEL, the _successor field of the Sq at
     *        position (2, 3) pointed to the Sq in MODEL at position
     *        (4, 1), then the Sq at position (2, 3) in this copy
     *        will have a _successor field pointing to the Sq at
     *        position (4, 1) in this copy.  Be careful NOT to have
     *        any of these fields in the copy pointing at the old Sqs in
     *        MODEL. */
    Model(Model model) {
        _width = model.width(); _height = model.height();
        _unconnected = model._unconnected;
        _solnNumToPlace = model._solnNumToPlace;
        _solution = model._solution;
        _usedGroups.addAll(model._usedGroups);
        _allSuccessors = model._allSuccessors;
        _board = new Sq[_width][_height];

        for (int i = 0; i < _width; i+=1) {
            for (int j = 0; j < _height; j+=1) {
                Sq s_og = model._board[i][j];
                Sq s = new Sq(model._board[i][j]);
                this._board[i][j] = s;
                this._allSquares.add(s);
            }
        }

        for (int i = 0; i < _width; i +=1) {
            for (int j = 0; j < _height; j+=1) {
                Sq s = this._board[i][j];
                if (model.get(i,j)._successor == null ) {
                    s._successor = null;
                } else {
                    Place s_successor = model.get(i,j)._successor.pl;
                    s._successor = this.get(s_successor);
                }
                if (model.get(i,j)._predecessor == null ) {
                    s._predecessors = null;
                } else {
                    Place s_predecessor = model.get(i,j)._predecessor.pl;
                    s._predecessor = this.get(s_predecessor);
                }
                if (model.get(i,j)._predecessors == null) {
                    s._predecessors = null;
                } else {
                    PlaceList s_predecessors = model.get(i,j)._predecessors;
                    s._predecessors = s_predecessors;
                }
                if (model.get(i,j)._successors == null) {
                    s._successors = null;
                } else {
                    PlaceList s_successors = model.get(i,j)._successors;
                    s._successors = s_successors;
                }
                Place s_head = model.get(i,j)._head.pl;
                s._head = this.get(s_head);
            }
        }
    }

    /** Returns the width (number of columns of cells) of the board. */
    final int width() {
        return _width;
    }

    /** Returns the height (number of rows of cells) of the board. */
    final int height() {
        return _height;
    }

    /** Returns the number of cells (and thus, the sequence number of the
     *  final cell). */
    final int size() {
        return _width * _height;
    }

    /** Returns true iff (X, Y) is a valid cell location. */
    final boolean isCell(int x, int y) {
        return 0 <= x && x < width() && 0 <= y && y < height();
    }

    /** Returns true iff P is a valid cell location. */
    final boolean isCell(Place p) {
        return isCell(p.x, p.y);
    }

    /** Returns all cell locations that are a queen move from (X, Y)
     *  in direction DIR, or all queen moves in any direction if DIR = 0. */
    final PlaceList allSuccessors(int x, int y, int dir) {
        return _allSuccessors[x][y][dir];
    }

    /** Returns all cell locations that are a queen move from P in direction
     *  DIR, or all queen moves in any direction if DIR = 0. */
    final PlaceList allSuccessors(Place p, int dir) {
        return _allSuccessors[p.x][p.y][dir];
    }

    /** Remove all connections and non-fixed sequence numbers. */
    void restart() {
        for (Sq sq : this) {
            sq.disconnect();
        }
        assert _unconnected == _width * _height - 1;
    }

    /** Return the number array that solves the current puzzle (the argument
     *  the constructor.  The result must not be subsequently modified.  */
    final int[][] solution() {
        return _solution;
    }

    /** Return the position of the cell with sequence number N in this board's
     *  solution. */
    Place solnNumToPlace(int n) {
        return _solnNumToPlace[n];
    }

    /** Return the Sq with sequence number N in this board's solution. */
    Sq solnNumToSq(int n) {
        return get(solnNumToPlace(n));
    }

    /** Return the current number of unconnected cells. */
    final int unconnected() {
        return _unconnected;
    }

    /** Returns true iff the puzzle is solved. */
    final boolean solved() {
        return _unconnected == 0;
    }

    /** Return the cell at (X, Y). */
    final Sq get(int x, int y) {
        return _board[x][y];
    }

    /** Return the cell at P. */
    final Sq get(Place p) {
        return p == null ? null : _board[p.x][p.y];
    }

    /** Return the cell at the same position as SQ (generally from another
     *  board), or null if SQ is null. */
    final Sq get(Sq sq) {
        return sq == null ? null : _board[sq.x][sq.y];
    }

    /** Connect all numbered cells with successive numbers that as yet are
     *  unconnected and are separated by a queen move.  Returns true iff
     *  any changes were made. */
    boolean autoconnect() {
        boolean changes = false;
        for (Sq curr : _allSquares) {
            for (Sq curr2 : _allSquares) {
                /* curr.hasFixedNum() == true && curr2.hasFixedNum() == true && curr.connectable(curr2 */
                if (curr._sequenceNum > 0 && curr2._sequenceNum > 0 && curr.connectable(curr2)){
                    curr.connect(curr2);
                    changes = true;
                }
            }
        }
        return changes;
    }

    /** Sets the numbers in this board's squares to the solution from which
     *  this board was last initialized by the constructor. */
    void solve() {
        this.restart();
        for (Place P : _solnNumToPlace) {
            if (P == null) {
                continue;
            }
            Sq curr_sq = this.get(P);
            int[] next_coords = findCoords((curr_sq._sequenceNum + 1), _solution);
            Sq next_seq = this.get(pl(next_coords[0],next_coords[1]));
            curr_sq.connect(next_seq);
        }
        _unconnected = 0;
    }

    /** Return the direction from cell (X, Y) in the solution to its
     *  successor, or 0 if it has none. */
    private int arrowDirection(int x, int y) {
        int seq0 = _solution[x][y];
        if (seq0 == this.size()) {
            return 0;
        }
        int seq1 = seq0+1;
        /* Iterate through and find x1,y1 of seq1, the next in the sequence after seq0 */
        int[] coords = findCoords(seq1, _solution);

        return signpost.Place.dirOf(x,y,coords[0],coords[1]);
    }

    /** Iterate through a 2d int[][] array and return the coordinates of the matching element */
    public int [] findCoords(int seq1, int [][] solution) {
        int[] result = new int[2];
        for (int i = 0; i < _solution.length; i +=1) {
            for (int j =0; j < _solution[0].length; j+=1) {
                if (_solution[i][j] == seq1) {
                    result [0] = i;
                    result [1] = j;
                }
            }
        }
        return result;
    }

    /** Return a new, currently unused group number > 0.  Selects the
     *  lowest not currently in use. */
    private int newGroup() {
        for (int i = 1; true; i += 1) {
            if (_usedGroups.add(i)) {
                return i;
            }
        }
    }

    /** Indicate that group number GROUP is no longer in use. */
    private void releaseGroup(int group) {
        _usedGroups.remove(group);
    }

    /** Combine the groups G1 and G2, returning the resulting group. Assumes
     *  G1 != 0 != G2 and G1 != G2. */
    private int joinGroups(int g1, int g2) {
        assert (g1 != 0 && g2 != 0);
        if (g1 == -1 && g2 == -1) {
            return newGroup();
        } else if (g1 == -1) {
            return g2;
        } else if (g2 == -1) {
            return g1;
        } else if (g1 < g2) {
            releaseGroup(g2);
            return g1;
        } else {
            releaseGroup(g1);
            return g2;
        }
    }

    @Override
    public Iterator<Sq> iterator() {
        return _allSquares.iterator();
    }

    @Override
    public String toString() {
        String hline;
        hline = "+";
        for (int x = 0; x < _width; x += 1) {
            hline += "------+";
        }

        Formatter out = new Formatter();
        for (int y = _height - 1; y >= 0; y -= 1) {
            out.format("%s%n", hline);
            out.format("|");
            for (int x = 0; x < _width; x += 1) {
                Sq sq = get(x, y);
                if (sq.hasFixedNum()) {
                    out.format("+%-5s|", sq.seqText());
                } else {
                    out.format("%-6s|", sq.seqText());
                }
            }
            out.format("%n|");
            for (int x = 0; x < _width; x += 1) {
                Sq sq = get(x, y);
                if (sq.predecessor() == null && sq.sequenceNum() != 1) {
                    out.format(".");
                } else {
                    out.format(" ");
                }
                if (sq.successor() == null
                    && sq.sequenceNum() != size()) {
                    out.format("o ");
                } else {
                    out.format("  ");
                }
                out.format("%s |", ARROWS[sq.direction()]);
            }
            out.format("%n");
        }
        out.format(hline);
        return out.toString();
    }

    @Override
    public boolean equals(Object obj) {
        Model model = (Model) obj;
        return (_unconnected == model._unconnected
                && _width == model._width && _height == model._height
                && Arrays.deepEquals(_solution, model._solution)
                && Arrays.deepEquals(_board, model._board));
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(_solution) * Arrays.deepHashCode(_board);
    }

    /** Represents a square on the board. */
    final class Sq {
        /** A square at (X0, Y0) with arrow in direction DIR (0 if not
         *  set), group number GROUP, sequence number SEQUENCENUM (0
         *  if none initially assigned), which is fixed iff FIXED. */
        Sq(int x0, int y0, int sequenceNum, boolean fixed, int dir, int group) {
            x = x0; y = y0;
            pl = pl(x, y);
            _hasFixedNum = fixed;
            _sequenceNum = sequenceNum;
            _dir = dir;
            _head = this;
            _group = group;
        }

        /** A copy of OTHER, excluding head, successor, and predecessor. */
        Sq(Sq other) {
            this(other.x, other.y, other._sequenceNum, other._hasFixedNum,
                 other._dir, other._group);
            _successor = _predecessor = null;
            _head = this;
            _successors = other._successors;
            _predecessors = other._predecessors;
        }

        /** Return this square's current sequence number, or 0 if
         *  none assigned. */
        int sequenceNum() {
            return _sequenceNum;
        }

        /** Fix this square's current sequence number at N>0.  It is
         *  an error if this square's number is not initially 0 or N. */
        void setFixedNum(int n) {
            if (n == 0 || (_sequenceNum != 0 && _sequenceNum != n)) {
                throw badArgs("sequence number may not be fixed");
            }
            _hasFixedNum = true;
            if (_sequenceNum == n) {
                return;
            } else {
                releaseGroup(_head._group);
            }
            _sequenceNum = n;
            for (Sq sq = this; sq._successor != null; sq = sq._successor) {
                sq._successor._sequenceNum = sq._sequenceNum + 1;
            }
            for (Sq sq = this; sq._predecessor != null; sq = sq._predecessor) {
                sq._predecessor._sequenceNum = sq._sequenceNum - 1;
            }
        }

        /** Unfix this square's sequence number if it is currently fixed;
         *  otherwise do nothing. */
        void unfixNum() {
            Sq next = _successor, pred = _predecessor;
            _hasFixedNum = false;
            disconnect();
            if (pred != null) {
                pred.disconnect();
            }
            _sequenceNum = 0;
            if (next != null) {
                connect(next);
            }
            if (pred != null) {
                pred.connect(this);
            }
        }

        /** Return true iff this square's sequence number is fixed. */
        boolean hasFixedNum() {
            return _hasFixedNum;
        }

        /** Returns direction of this square's arrow (0 if no arrow). */
        int direction() {
            return _dir;
        }

        /** Return this square's current predecessor. */
        Sq predecessor() {
            return _predecessor;
        }

        /** Return this square's current successor. */
        Sq successor() {
            return _successor;
        }

        /** Return the head of the connected sequence this square
         * is currently in. */
        Sq head() {
            return _head;
        }

        /** Return the group number of this square's group.  It is
         *  0 if this square is numbered, and-1 if it is alone in its group. */
        int group() {
            if (_sequenceNum != 0) {
                return 0;
            } else {
                return _head._group;
            }
        }

        /** Size of alphabet. */
        static final int ALPHA_SIZE = 26;

        /** Return a textual representation of this square's sequence number or
         *  group/position. */
        String seqText() {
            if (_sequenceNum != 0) {
                return String.format("%d", _sequenceNum);
            }
            int g = group() - 1;
            if (g < 0) {
                return "";
            }

            String groupName =
                String.format("%s%s",
                              g < ALPHA_SIZE ? ""
                              : Character.toString((char) (g / ALPHA_SIZE
                                                           + 'a')),
                              Character.toString((char) (g % ALPHA_SIZE
                                                         + 'a')));
            if (this == _head) {
                return groupName;
            }
            int n;
            n = 0;
            for (Sq p = this; p != _head; p = p._predecessor) {
                n += 1;
            }
            return String.format("%s%+d", groupName, n);
        }

        /** Return locations of this square's potential successors. */
        PlaceList successors() {
            return _successors;
        }

        /** Return locations of this square's potential predecessors. */
        PlaceList predecessors() {
            return _predecessors;
        }

        /** Returns true iff this square may be connected to square S1, that is:
         *  + S1 is in the correct direction from this square.
         *  + S1 does not have a current predecessor, this square does not
         *    have a current successor, S1 is not the first cell in sequence,
         *    and this square is not the last.
         *  + If S1 and this square both have sequence numbers, then
         *    this square's is sequenceNum() == S1.sequenceNum() - 1.
         *  + If neither S1 nor this square have sequence numbers, then
         *    they are not part of the same connected sequence.
         */
        boolean connectable(Sq s1) {
             /* Have to make sure that s1 is not the same square as s0 */
            if (x == s1.x && y == s1.y) {
                return false;
            }
            /* Is s1 in the correct direction.
            * Place.dirOf(x0, y0, x1,y1) will return 0 if not a queen move apart.
            * Arrow is accessible via _dir for s0.
            * pl is the place instance variable of s0.
            * Have to make sure that the direction is not unset, so it can't be zero. */
            else if (_dir == 0 || _dir != pl.dirOf(x,y,s1.x,s1.y)) {
                return false;
            }
            /* Check for predecessor of s1, and s0 successor both of which should be null */
            else if (s1.predecessor() != null || this.successor() != null) {
                return false;
            }
            /* Check that s1 is not the first number in sequence and that s0 is not the last */
            else if (s1.sequenceNum() == 1 || this.sequenceNum() == size()) {
                return false;
            }
            /* If both s0 and s1 have sequence numbers then s0's must be one less than s1's */
            else if ((sequenceNum() != 0 && s1.sequenceNum() != 0) && (this.sequenceNum() != (s1.sequenceNum() - 1))) {
                return false;
            }
            /* If neither of them have sequence numbers then their _sequenceNum == 0 and _head must not be the same
            * or they would be a part of the same connectable group  */
            else if ((this.sequenceNum() == 0 && s1.sequenceNum() == 0) && (this.head() == s1.head())){
                return false;
            }
            /* Exhausted the possible errors so return true */
            return true;
        }

        /** Connect this square to S1, if both are connectable; otherwise do
         *  nothing. Returns true iff this square and S1 were connectable.
         *  Assumes S1 is in the proper arrow direction from this square. */
        boolean connect(Sq s1) {
            if (!connectable(s1)) {
                return false;
            }
            /* Create two ints to keep track of the square's starting groups */
            int s0group = this.group();
            int sgroup = s1.group();

            _unconnected -= 1;

            /* Set this square's _successor field and S1's  _predecessor field. */
            this._successor = s1;
            s1._predecessor = this;

            /* If this square has a number, number all its successors accordingly (if needed).
            * previously I had this: this.hasFixedNum() && !(s1.hasFixedNum())*/
            if (s0group == 0  && sgroup != 0) {
                int index = this.sequenceNum() +1;
                Sq s0_head = this._head;
                s1._sequenceNum = index;
                Sq curr = s1;

                while (curr.successor() != null && (index < (size()-1))) {
                    index +=1;
                    curr._successor._sequenceNum = index;
                    curr._successor._head = s0_head;
                    curr = curr.successor();
                }
            }

             /* If S1 is numbered, number this square and its predecessors accordingly (if needed).
             s1.hasFixedNum() && !(this.hasFixedNum() */
            if (s0group != 0  && sgroup == 0) {
                int index = s1.sequenceNum()-1;
                Sq s0_head = this._head;
                this._sequenceNum = index;
                Sq curr = this;

                while (curr.predecessor() != null && (index > 1)) {
                    index -=1;
                    curr._predecessor._sequenceNum = index;
                    curr = curr.predecessor();
                }
                curr = this;
                while (curr.successor() != null) {
                    curr._successor._head = s0_head;
                    curr = curr.successor();
                }
            }

            /* Set the _head fields of this square's successors to this square's _head */
            Sq next_pointer = this;
            while (next_pointer.successor() != null) {
                next_pointer.successor()._head = this.head();
                next_pointer = next_pointer.successor();
            }

            /* If either of this square or S1 used to be unnumbered and is now numbered,
            * release its group of whichever was unnumbered, so that it can be reused. */
            if (s0group > 0 && sgroup == 0) {
                releaseGroup(s0group);
            }
            else if ( s0group == 0 && sgroup > 0) {
                releaseGroup(sgroup);
            }
            /* If both this square and S1 are unnumbered, set the group of this square's
            * head to the result of joining the two groups. */
            if (this.sequenceNum() == 0 && s1.sequenceNum() == 0) {
                this._head._group = joinGroups(s0group, sgroup);
            }
            return true;
        }

        /** Disconnect this square from its current successor, if any. */
        void disconnect() {
            Sq next = _successor;
            if (next == null) {
                return;
            }
            _unconnected += 1;
            next._predecessor = this._successor = null;
            /* Because _sequenceNum == 0 that means none of the elements have defined sequence numbers, and their
            * group number is above 0 since they have to be connected for us to disconnect them. */
            if (_sequenceNum == 0) {
                /* If both this and next are now one-element groups, release their former group
                * and set both group numbers to -1.
                * We know that successor of s0 is null since we're disconnecting, so only check its
                * predecessor. We know that predecessor of s1 is null so only check its successor. */
                if ((this.predecessor() == null) && (next.successor() == null)) {
                    releaseGroup(this.group());
                    this._group = -1;
                    next._group = -1;
                }
                /* Otherwise, if either is now a one-element group, set its group number
                * to -1 without releasing the group number.
                */
                else if (this.predecessor() == null) {
                    next._head = next;
                    /* Want to keep the original group of this and pass it on to next */
                    if (next.successor() != null){
                        next._group = this._group;
                        Sq curr = next;
                        while (curr.successor() != null) {
                            curr._head = next;
                            curr = curr.successor();
                        }
                    }
                    this._group = -1;
                }
                else if (next.successor() == null) {
                    next._group = -1;
                }
                /*  Otherwise, the group has been split into two multi- element groups.
                 * Create a new group for next. */
                else {
                    int newGroup = newGroup();
                    next._group = newGroup;
                    Sq curr = next;
                    while (curr.successor() != null) {
                        curr._head = next;
                        curr = curr.successor();
                    }
                }
            } else {
                /* If neither this nor any square in its group that precedes it
                * has a fixed sequence number, set all their sequence numbers to
                * 0 and create a new group for them if this has a current
                * predecessor (otherwise set group to -1). */
                boolean anyPrevious = false;
                Sq prevCurr = this;
                if (prevCurr.hasFixedNum()) {
                    anyPrevious = true;
                }
                if (prevCurr.predecessor() != null) {
                    while (prevCurr.predecessor() != null) {
                        if (prevCurr.predecessor().hasFixedNum()) {
                            anyPrevious = true;
                        }
                        prevCurr = prevCurr._predecessor;
                    }
                    if (!anyPrevious) {
                        prevCurr = this;
                        this._head._group = newGroup();
                        while (prevCurr != null) {
                            prevCurr._sequenceNum = 0;
                            prevCurr = prevCurr._predecessor;
                        }
                    }
                }
                else if (!anyPrevious) {
                    this._sequenceNum = 0;
                    this._group = -1;
                }
                /* If neither next nor any square in its group that follows it
                * has a fixed sequence number, set all their sequence numbers
                * to 0 and create a new group for them if next has a current
                * successor (otherwise set next's group to -1.) */
                boolean anyNext = false;
                Sq next_curr = next;
                if (next_curr.hasFixedNum()) {
                    anyNext = true;
                }
                if (next_curr.successor() != null) {
                    while (next_curr.successor() != null) {
                        if (next_curr.successor().hasFixedNum()) {
                            anyNext = true;
                        }
                        next_curr = next_curr._successor;
                    }
                    if (!anyNext) {
                        next_curr = next;
                        next._group = newGroup();
                        while (next_curr != null) {
                            next_curr._sequenceNum = 0;
                            next_curr = next_curr._successor;
                        }
                    }
                }
                else if (!anyNext) {
                    next._sequenceNum = 0;
                    next._group = -1;
                }
            }
            /* Set the _head of next and all squares in its group to next. */
            Sq nextCurr = next;
            next._head = next;
            while (nextCurr.successor() != null) {
                nextCurr._successor._head = next;
                nextCurr = nextCurr._successor;
            }
        }

        @Override
        public boolean equals(Object obj) {
            Sq sq = (Sq) obj;
            return sq != null
                && pl == sq.pl
                && _hasFixedNum == sq._hasFixedNum
                && _sequenceNum == sq._sequenceNum
                && _dir == sq._dir
                && (_predecessor == null) == (sq._predecessor == null)
                && (_predecessor == null
                    || _predecessor.pl == sq._predecessor.pl)
                && (_successor == null || _successor.pl == sq._successor.pl);
        }

        @Override
        public int hashCode() {
            return (x + 1) * (y + 1) * (_dir + 1)
                * (_hasFixedNum ? 3 : 1) * (_sequenceNum + 1);
        }

        @Override
        public String toString() {
            return String.format("<Sq@%s, dir: %d>", pl, direction());
        }

        /** The coordinates of this square in the board. */
        protected final int x, y;
        /** The coordinates of this square as a Place. */
        protected final Place pl;
        /** The first in the currently connected sequence of cells ("group")
         *  that includes this one. */
        private Sq _head;
        /** The group number of the group of which this is a member, if
         *  head == this.  Numbered sequences have a group number of 0,
         *  regardless of the value of _group. Unnumbered one-member groups
         *  have a group number of -1.  If _head != this and the square is
         *  unnumbered, then _group is undefined and the square's group
         *  number is maintained in _head._group. */
        private int _group;
        /** True iff assigned a fixed sequence number. */
        private boolean _hasFixedNum;
        /** The current imputed or fixed sequence number,
         *  numbering from 1, or 0 if there currently is none. */
        private int _sequenceNum;
        /** The arrow direction. The possible values are 0 (for unset),
         *  1 for northeast, 2 for east, 3 for southeast, 4 for south,
         *  5 for southwest, 6 for west, 7 for northwest, and 8 for north. */
        private int _dir;
        /** The current predecessor of this square, or null if there is
         *  currently no predecessor. */
        private Sq _predecessor;
        /** The current successor of this square, or null if there is
         *  currently no successor. */
        private Sq _successor;
        /** Locations of the possible predecessors of this square. */
        private PlaceList _predecessors;
        /** Locations of the possible successors of this square. */
        private PlaceList _successors;
    }

    /** ASCII denotations of arrows, indexed by direction. */
    private static final String[] ARROWS = {
        " *", "NE", "E ", "SE", "S ", "SW", "W ", "NW", "N "
    };

    /** Number of squares that haven't been connected. */
    private int _unconnected;
    /** Dimensions of board. */
    private int _width, _height;
    /** Contents of board, indexed by position. */
    private Sq[][] _board;
    /** Contents of board as a sequence of squares for convenient iteration. */
    private ArrayList<Sq> _allSquares = new ArrayList<>();
    /** _allSuccessors[x][y][dir] is a sequence of all queen moves possible
     *  on the board of in direction dir from (x, y).  If dir == 0,
     *  this is all places that are a queen move from (x, y) in any
     *  direction. */
    private PlaceList[][][] _allSuccessors;
    /** The solution from which this Model was built. */
    private int[][] _solution;
    /** Inverse mapping from sequence numbers to board positions. */
    private Place[] _solnNumToPlace;
    /** The set of positive group numbers currently in use. */
    private HashSet<Integer> _usedGroups = new HashSet<>();
}
