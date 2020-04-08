/* Skeleton Copyright (C) 2015, 2020 Paul N. Hilfinger and the Regents of the
 * University of California.  All rights reserved. */
package loa;

import java.util.ArrayList;
import java.util.Iterator;

import static loa.Piece.*;

/** An automated Player.
 *  @author Pavel Gladkevich
 */
class MachinePlayer extends Player {

    /** A position-score magnitude indicating a win (for white if positive,
     *  black if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new MachinePlayer with no piece or controller (intended to produce
     *  a template). */
    MachinePlayer() {
        this(null, null);
    }

    /** A MachinePlayer that plays the SIDE pieces in GAME. */
    MachinePlayer(Piece side, Game game) {
        super(side, game);
    }

    @Override
    String getMove() {
        Move choice;

        assert side() == getGame().getBoard().turn();
        int depth;
        choice = searchForMove();
        getGame().reportMove(choice);
        return choice.toString();
    }

    @Override
    Player create(Piece piece, Game game) {
        return new MachinePlayer(piece, game);
    }

    @Override
    boolean isManual() {
        return false;
    }

    /** Return a move after searching the game tree to DEPTH>0 moves
     *  from the current position. Assumes the game is not over. */
    private Move searchForMove() {
        Board work = new Board(getBoard());
        int value;
        assert side() == work.turn();
        assert !work.gameOver() : "_winnerKnown error probably";
        _foundMove = null;
        if (side() == WP) {
            value = findMove(work, chooseDepth(), true, 1, -INFTY, INFTY);
        } else {
            value = findMove(work, chooseDepth(), true, -1, -INFTY, INFTY);
        }
        return _foundMove;
    }

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _foundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _foundMove. If the game is over
     *  on BOARD, does not set _foundMove. */
    private int findMove(Board board, int depth, boolean saveMove,
                         int sense, int alpha, int beta) {
        Piece side = side();
        if (depth == 0 || board.gameOver()) {
//            _foundMove = board.legalMoves().get(getGame().randInt(64)
//                    % board.legalMoves().size());
            // getGame().randInt(64)
            return heuristic(board, sense);
        }
        // FIXME
        int bestScore = 0;
        int bestMove = 0;
//        ArrayList<Move> moves = new ArrayList<>();
//        moves = (ArrayList<Move>) board.legalMoves();
//        Iterator<Move> moves = board.legalMoves().iterator();
        //moves.hasNext()
        Move[] moves = board.legalMoves().toArray(new Move[0]);

        for (Move move : moves) {
//            ArrayList<Move> movesL = new ArrayList<>();
////            movesL.clear();
//            movesL = (ArrayList<Move>) board.legalMoves();
//            Move move;
//            move = moves.get(i);
           //move = board.legalMoves().iterator().next();
            board.makeMove(move);
//            board = board;
            int score = findMove(board,depth-1,false,
                    sense * -1, alpha, beta);
            if (score > bestScore) {
                bestScore = score;
                bestMove = board.legalMoves().indexOf(move);
            }
            if (sense == 1 && side == WP || sense == -1 && side == BP) {
                alpha = Math.max(score, alpha);
            } else {
                beta = Math.min(score, beta);
            }
            if (alpha >= beta) {
                board.retract();
                return bestScore;
            }
            board.retract();
        }
        if (saveMove) {
            System.out.println("Hi");
            _foundMove = board.legalMoves().get(bestMove); //board.legalMoves().get(getGame().randInt(64)
                    // % board.legalMoves().size()); //null; // FIXME
        }

        return bestScore; // FIXME
    }

    /** Return a search depth for the current position. */
    private int chooseDepth() {
        return 2;  // FIXME
    }

    private int heuristic(Board board, int sense) {
        if (board.getWINNERKNOWN()) {
            Piece winner = board.winner();
            if (winner == WP) {
                return WINNING_VALUE;
            } else if (winner == BP) {
                return -1*WINNING_VALUE;
            } else {
                assert (winner == EMP);
                return 0;
            }

        }
        return getGame().randInt(64) % board.legalMoves().size();
        //return 0;
    }

    /** Used to convey moves discovered by findMove. */
    private Move _foundMove;
}
