/* Skeleton Copyright (C) 2015, 2020 Paul N. Hilfinger and the Regents of the
 * University of California.  All rights reserved. */
package loa;

import java.lang.reflect.Array;
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
            return heuristic(board);
        }
        Move[] moves = board.legalMoves().toArray(new Move[0]);
        int bestScore = -INFTY * sense;
        if (saveMove == true) {
            _index = 0;
        }

        for (int i =0; i < moves.length; i += 1) {
            Move move = moves[i];
            board.makeMove(move);
            int score = findMove(board,depth-1,false,
                    sense * -1, alpha, beta);
            if (score > bestScore) {
                bestScore = score;
                if(saveMove == true) {
                    _index = i;
                }
            }

            if (sense == 1 && side == WP || sense == -1 && side == BP) {
                alpha = Math.max(score, alpha);
            } else {
                beta = Math.min(score, beta);
            }
            if (alpha >= beta) {
                board.retract();
                break;
            }
            board.retract();
        }
        if (saveMove) {
            _foundMove = board.legalMoves().get(_index);
        }

        return bestScore;
    }

    /** Return a search depth for the current position. */
    private int chooseDepth() {
        return 1;
    }

    private int heuristic(Board board) {
        int score = 0;
        if (board.getWINNERKNOWN()) {
            Piece winner = board.winner();
            if (winner == WP) {
                return WINNING_VALUE;
            } else if (winner == BP) {
                return -1 * WINNING_VALUE;
            } else {
                assert (winner == EMP);
                return 0;
            }
        }
        ArrayList<Integer> wR = (ArrayList<Integer>) board.getRegionSizes(WP);
        ArrayList<Integer> bR = (ArrayList<Integer>) board.getRegionSizes(BP);
//        int difference = Math.abs(board.getbNUM() - board.getwNUM());
//        score -= (Math.pow(wR.get(0), difference));
//        score += (Math.pow(bR.get(0), difference));
        score = wR.get(0) - bR.get(0);

//        return score;
        //System.out.println(getGame().randInt(63) % board.legalMoves().size());
//        return getGame().randInt(63) % board.legalMoves().size();
        return score;
    }

    /** Used to convey moves discovered by findMove. */
    private Move _foundMove;

    private int _index;
}
