/* Skeleton Copyright (C) 2015, 2020 Paul N. Hilfinger and the Regents of the
 * University of California.  All rights reserved. */
package loa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;

import java.util.regex.Pattern;

import static loa.Piece.*;
import static loa.Square.*;

/** Represents the state of a game of Lines of Action.
 *  @author Pavel Gladkevich
 */
class Board {

    /** Default number of moves for each side that results in a draw. */
    static final int DEFAULT_MOVE_LIMIT = 60;

    /** Pattern describing a valid square designator (cr). */
    static final Pattern ROW_COL = Pattern.compile("^[a-h][1-8]$");

    /** A Board whose initial contents are taken from INITIALCONTENTS
     *  and in which the player playing TURN is to move. The resulting
     *  Board has
     *        get(col, row) == INITIALCONTENTS[row][col]
     *  Assumes that PLAYER is not null and INITIALCONTENTS is 8x8.
     *
     *  CAUTION: The natural written notation for arrays initializers puts
     *  the BOTTOM row of INITIALCONTENTS at the top.
     */
    Board(Piece[][] initialContents, Piece turn) {
        initialize(initialContents, turn);
    }

    /** A new board in the standard initial position. */
    Board() {
        this(INITIAL_PIECES, BP);
    }

    /** A Board whose initial contents and state are copied from
     *  BOARD. */
    Board(Board board) {
        this();
        copyFrom(board);
    }

    /** Set my state to CONTENTS with SIDE to move. */
    void initialize(Piece[][] contents, Piece side) {
        for (int r = 0; r < BOARD_SIZE; r += 1) {
            for (int c = 0; c < BOARD_SIZE; c += 1) {
                _board[sq(c, r).index()] = contents[r][c];
            }
        }
        _winner = null;
        _turn = side;
        _moveLimit = DEFAULT_MOVE_LIMIT;
        _winnerKnown = false;
        _moves.clear();
        _whiteRegionSizes.clear();
        _blackRegionSizes.clear();
        _subsetsInitialized = false;
        computeRegions();
        _subsetsInitialized = true;
    }

    /** Set me to the initial configuration. */
    void clear() {
        initialize(INITIAL_PIECES, BP);
    }

    /** Set my state to a copy of BOARD. */
    void copyFrom(Board board) {
        if (board == this) {
            return;
        }
        for (int r = 0; r < BOARD_SIZE; r += 1) {
            for (int c = 0; c < BOARD_SIZE; c += 1) {
                _board[sq(c, r).index()] = board.get(sq(c, r));
            }
        }
        _moves.clear();
        ArrayList<Move> moves = board.getMOVES();
        _moves.addAll(moves);
        _turn = board.getTURN();
        setMoveLimit(board.getLIMIT());
        _winnerKnown = board.getWINNERKNOWN();
        _winner = board.getWINNER();
        _subsetsInitialized = board.getSUBSETSINITIALIZED();
        _whiteRegionSizes.clear();
        _blackRegionSizes.clear();
        ArrayList<Integer> white = board.getWHITE(), black = board.getBLACK();
        _whiteRegionSizes.addAll(white);
        _blackRegionSizes.addAll(black);
    }

    /** Return the contents of the square at SQ. */
    Piece get(Square sq) {
        return _board[sq.index()];
    }

    /** Set the square at SQ to V and set the side that is to move next
     *  to NEXT, if NEXT is not null. */
    void set(Square sq, Piece v, Piece next) {
        _board[sq.index()] = v;
        if (next != null) {
            _turn = next;
        }
    }

    /** Set the square at SQ to V, without modifying the side that
     *  moves next. */
    void set(Square sq, Piece v) {
        set(sq, v, null);
    }

    /** Set limit on number of moves by each side that results in a tie to
     *  LIMIT, where 2 * LIMIT > movesMade(). */
    void setMoveLimit(int limit) {
        if (2 * limit <= movesMade()) {
            throw new IllegalArgumentException("move limit too small");
        }
        _moveLimit = 2 * limit;
    }

    /** Assuming isLegal(MOVE), make MOVE. This function assumes that
     *  MOVE.isCapture() will return false.  If it saves the move for
     *  later retraction, makeMove itself uses MOVE.captureMove() to produce
     *  the capturing move. */
    void makeMove(Move move) {
        assert isLegal(move);
        boolean capture = false;
        Square from = move.getFrom(), to = move.getTo();
        if (get(to).opposite() == null) {
            _moves.add(move);
        } else {
            _moves.add(move.captureMove());
        }
        set(to, get(from));
        set(from, EMP);
        _subsetsInitialized = false;
        computeRegions();
        _subsetsInitialized = true;
        if (piecesContiguous(_turn) || piecesContiguous(_turn.opposite())
                || movesMade() == getLIMIT()) {
            _winner = winner();
        }
        _turn = _turn.opposite();
    }

    /** Retract (unmake) one move, returning to the state immediately before
     *  that move.  Requires that movesMade () > 0. */
    void retract() {
        assert movesMade() > 0;
        Move move = _moves.remove(_moves.size() - 1);
        Square f = move.getFrom(), t = move.getTo();
        Piece from = get(t);
        if (move.isCapture()) {
            set(t, get(t).opposite());
        } else {
            set(t, EMP);
        }
        set(f, from);
        _turn = _turn.opposite();
        _winnerKnown = false;
        _winner = null;
        _subsetsInitialized = false;
        computeRegions();
    }

    /** Return the Piece representing who is next to move. */
    Piece turn() {
        return _turn;
    }

    /** Return true iff FROM - TO is a legal move for the player currently on
     *  move.
     * if the direction we're moving torward has a different colored piece
     * in between us then return false
     * se blocked (Square from, Square to) to check if the above condition and
     * also if the square to has a piece of the same color as the square from
     *
     * Need to also check for the right number of steps. If not the right number
     * then return false. Valid number of steps is determined by the number
     * of black or white pieces in the line of action*/
    boolean isLegal(Square from, Square to) {

        if (!from.isValidMove(to) || !isRightSteps(from, to)
                || blocked(from, to) || get(from) != _turn) {
            return false;
        }
        return true;
    }
    /** Return true iff FROM - TO is a legal move for the player currently on
     *  move.
     *
     * The same as above but without step check */
    boolean isLegalWithoutSteps(Square from, Square to) {
        if (to == null || !from.isValidMove(to) || blocked(from, to)) {
            return false;
        }
        return true;
    }

    /** Return true iff FROM - TO has the right number of steps for the current
     * amount of pieces on the line of action. */
    boolean isRightSteps(Square from, Square to) {
        int dir1 = from.direction(to), dir2 = to.direction(from);
        int count = 1;
        Square curr = from.moveDest(dir1, 1);
        while (curr != null) {
            Piece currP = get(curr);
            if (currP != EMP) {
                count += 1;
            }
            curr = curr.moveDest(dir1, 1);
        }
        curr = from.moveDest(dir2, 1);
        while (curr != null) {
            Piece currP = get(curr);
            if (currP != EMP) {
                count += 1;
            }
            curr = curr.moveDest(dir2, 1);
        }
        return count == from.distance(to);
    }

    /** Return the number of steps for a line of action based off of a square
     * FROM and a direction DIR. */
    int steps(Square from, int dir) {
        int dir1 = dir;
        int dir2 = (dir1 + 4) % 8;
        int count = 1;
        Square curr = from.moveDest(dir1, 1);
        while (curr != null) {
            Piece currP = get(curr);
            if (currP != EMP) {
                count += 1;
            }
            curr = curr.moveDest(dir1, 1);
        }
        curr = from.moveDest(dir2, 1);
        while (curr != null) {
            Piece currP = get(curr);
            if (currP.abbrev().compareTo("-") != 0) {
                count += 1;
            }
            curr = curr.moveDest(dir2, 1);
        }
        return count;
    }

    /** Return true iff MOVE is legal for the player currently on move.
     *  The isCapture() property is ignored. */
    boolean isLegal(Move move) {
        return isLegal(move.getFrom(), move.getTo());
    }

    /** Return a sequence of all legal moves from this position. */
    List<Move> legalMoves() {
        allmoves.clear();
        for (Square s : ALL_SQUARES) {
            if (get(s).equals(_turn)) {
                for (int dir = 0; dir < 4; dir += 1) {
                    int opppositeDIR = dir + 4;
                    int steps = steps(s, dir);
                    Square pot1 = s.moveDest(dir, steps);
                    Square pot2 = s.moveDest(opppositeDIR, steps);
                    if (isLegalWithoutSteps(s, pot1)) {
                        if (get(pot1) == _turn.opposite()) {
                            allmoves.add(Move.mv(s, pot1, true));
                        } else {
                            allmoves.add(Move.mv(s, pot1));
                        }
                    }
                    if (isLegalWithoutSteps(s, pot2)) {
                        if (get(pot2) == _turn.opposite()) {
                            allmoves.add(Move.mv(s, pot2, true));
                        } else {
                            allmoves.add(Move.mv(s, pot2));
                        }
                    }
                }
            }
        }
        return allmoves;
    }

    /** Return true iff the game is over (either player has all his
     *  pieces continguous or there is a tie). */
    boolean gameOver() {
        return winner() != null;
    }

    /** Return true iff SIDE's pieces are continguous. */
    boolean piecesContiguous(Piece side) {
        return getRegionSizes(side).size() == 1;
    }

    /** Return the winning side, if any.  If the game is not over, result is
     *  null.  If the game has ended in a tie, returns EMP. */
    Piece winner() {
        if (!_winnerKnown) {
            _winnerKnown = true;
            if (piecesContiguous(_turn)) {
                return _turn;
            } else if (piecesContiguous(_turn.opposite())) {
                return _turn.opposite();
            } else if (movesMade() == getLIMIT()) {
                return EMP;
            } else {
                _winnerKnown = false;
                return null;
            }
        }
        return _winner;
    }

    /** Return the total number of moves that have been made (and not
     *  retracted).  Each valid call to makeMove with a normal move increases
     *  this number by 1. */
    int movesMade() {
        return _moves.size();
    }

    @Override
    public boolean equals(Object obj) {
        Board b = (Board) obj;
        return Arrays.deepEquals(_board, b._board) && _turn == b._turn;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(_board) * 2 + _turn.hashCode();
    }

    @Override
    public String toString() {
        Formatter out = new Formatter();
        out.format("===%n");
        for (int r = BOARD_SIZE - 1; r >= 0; r -= 1) {
            out.format("    ");
            for (int c = 0; c < BOARD_SIZE; c += 1) {
                out.format("%s ", get(sq(c, r)).abbrev());
            }
            out.format("%n");
        }
        out.format("Next move: %s%n===", turn().fullName());
        return out.toString();
    }

    /** Return true if a move from FROM to TO is blocked by an opposing
     *  piece or by a friendly piece on the target square. */
    private boolean blocked(Square from, Square to) {
        Piece f = get(from);
        Piece t = get(to);
        if (f == t) {
            return true;
        }
        int dir = from.direction(to);
        int distance = from.distance(to);
        for (int steps = 1; steps < distance; steps += 1) {
            Square currSQ = from.moveDest(dir, steps);
            Piece currP = get(currSQ);
            if (f.opposite() == currP) {
                return true;
            }
        }
        return false;
    }

    /** Return the size of the as-yet unvisited cluster of squares
     *  containing P at and adjacent to SQ.  VISITED indicates squares that
     *  have already been processed or are in different clusters.  Update
     *  VISITED to reflect squares counted. */
    private int numContig(Square sq, boolean[][] visited, Piece p) {
        int col = sq.col(), row = sq.row();
        if (p == EMP || get(sq) != p || visited[col][row]) {
            return 0;
        }
        visited[col][row] = true;
        int count = 1;
        for (Square s : sq.adjacent()) {
            count += numContig(s, visited, p);
        }
        return count;
    }

    /** Set the values of _whiteRegionSizes and _blackRegionSizes. */
    private void computeRegions() {
        if (_subsetsInitialized) {
            return;
        }
        _whiteRegionSizes.clear();
        _blackRegionSizes.clear();
        boolean[][] visited = new boolean[BOARD_SIZE][BOARD_SIZE];
        for (int col = 0; col < BOARD_SIZE; col += 1) {
            for (int row = 0; row < BOARD_SIZE; row += 1) {
                if (!visited[col][row]) {
                    Piece p = get(sq(col, row));
                    if (p != EMP) {
                        if (p == WP) {
                            int countW = numContig(sq(col, row), visited, WP);
                            _whiteRegionSizes.add(countW);
                        } else {
                            int countB = numContig(sq(col, row), visited, BP);
                            _blackRegionSizes.add(countB);
                        }
                    }
                }
            }
        }
        Collections.sort(_whiteRegionSizes, Collections.reverseOrder());
        Collections.sort(_blackRegionSizes, Collections.reverseOrder());
        _subsetsInitialized = true;
    }

    /** Return the sizes of all the regions in the current union-find
     *  structure for side S. */
    List<Integer> getRegionSizes(Piece s) {
        computeRegions();
        if (s == WP) {
            return _whiteRegionSizes;
        } else {
            return _blackRegionSizes;
        }
    }

    /** Return the _turn variable from the board that calls this method. */
    public Piece getTURN() {
        return _turn;
    }
    /** Return the _moveLimit variable from the board that calls this method. */
    public int getLIMIT() {
        return _moveLimit;
    }
    /** Return the _winnerKnown variable from the board that calls this method.
     * */
    public boolean getWINNERKNOWN() {
        return _winnerKnown;
    }
    /** Return the _winner variable from the board that calls this method. */
    public Piece getWINNER() {
        return _winner;
    }
    /** Return the _subsetsInitialized variable from the board that calls
     * this method. */
    public boolean getSUBSETSINITIALIZED() {
        return _subsetsInitialized;
    }
    /** Return the _moves variable from the board that calls this method. */
    public ArrayList<Move> getMOVES() {
        return _moves;
    }
    /** Return the _whiteRegionSizes variable from the board that
     * calls this method. */
    public ArrayList<Integer> getWHITE() {
        return _whiteRegionSizes;
    }
    /** Return the _blackRegionSizes variable from the board that
     * calls this method. */
    public ArrayList<Integer> getBLACK() {
        return _blackRegionSizes;
    }

    /** The standard initial configuration for Lines of Action (bottom row
     *  first). */
    static final Piece[][] INITIAL_PIECES = {
        { EMP, BP,  BP,  BP,  BP,  BP,  BP,  EMP },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { EMP, BP,  BP,  BP,  BP,  BP,  BP,  EMP }
    };

    /** Current contents of the board.  Square S is at _board[S.index()]. */
    private final Piece[] _board = new Piece[BOARD_SIZE  * BOARD_SIZE];

    /** List of all unretracted moves on this board, in order. */
    private final ArrayList<Move> _moves = new ArrayList<>();
    /** Current side on move. */
    private Piece _turn;
    /** Limit on number of moves before tie is declared.  */
    private int _moveLimit;
    /** True iff the value of _winner is known to be valid. */
    private boolean _winnerKnown;
    /** Cached value of the winner (BP, WP, EMP (for tie), or null (game still
     *  in progress).  Use only if _winnerKnown. */
    private Piece _winner;

    /** True iff subsets computation is up-to-date. */
    private boolean _subsetsInitialized;

    /** List of the sizes of continguous clusters of pieces, by color. */
    private final ArrayList<Integer>
        _whiteRegionSizes = new ArrayList<>(),
        _blackRegionSizes = new ArrayList<>();

    /** List of the possible moves for this player. */
    private final ArrayList<Move> allmoves = new ArrayList<>();
}
