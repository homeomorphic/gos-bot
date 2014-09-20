package gos.bot.engine;

import gos.bot.protocol.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

final class MoveSearcher {

    private static final long MOVE_TIME_MS = 1800;
    private static final int MAX_DEPTH = 10;
    private final State state;
    private long nodes;
    private long startTime;
    private boolean flagAboutToFall;
    private int depth;

    private long endTime;
    private Move[] lastPrincipalVariation = new Move[MAX_DEPTH+1];

    public MoveSearcher(State state) {
        depth = 1;
        flagAboutToFall = false;
        this.state = state;
    }

    public SearchResult search() {
        startTime = System.currentTimeMillis();
        SearchResult result = null;
        do {
            final SearchResult levelResult = search(state, depth, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
            if (timeIsUp()) {
                if (result == null) {
                    //System.err.println("** SLOW WARNING **");
                    result = levelResult;
                }
                break;
            }
            result = levelResult;
            for (int i = 0; i < lastPrincipalVariation.length; i++) {
                lastPrincipalVariation[i] = i < result.principalVariation.size() ? result.principalVariation.get(i)
                                                                                 : null;
            }
            //System.err.println("eval = " + result);
            depth++;
        } while (depth <= MAX_DEPTH);
        endTime = System.currentTimeMillis();
        return result;
    }

    public double nps() {
        return nodes / ((endTime - startTime) / 1000.0);
    }
    public long nodes() {
        return nodes;
    }

    static final class SearchResult {
        public final int eval;
        public final LinkedList<Move> principalVariation;

        public SearchResult(int eval, LinkedList<Move> principalVariation) {
            this.eval = eval;
            this.principalVariation = principalVariation;
        }

        @Override
        public String toString() {
            return "SearchResult{" +
                    "eval=" + eval +
                    ", principalVariation=" + principalVariation +
                    '}';
        }
    }

    private void orderMoves(List<Move> moves, State state, int ply) {
        if (ply < lastPrincipalVariation.length) {
            final Move preferredMove = lastPrincipalVariation[ply];
            for (int i = 0; i < moves.size(); i++) {
                if (moves.get(i).equals(preferredMove)) {
                    moves.set(i, moves.get(0));
                    moves.set(0, preferredMove);
                    break;
                }
            }
        }
    }

    private SearchResult search(State state, int remainingDepth, int ply, int alpha, int beta) {
        final List<Move> moves = state.possibleMoves();
        orderMoves(moves, state, ply);

        Player winner;
        final SearchResult result;

        winner = state.loser().opponent();
        if (winner == Player.None && moves.size() == 0) {
            winner = state.getPlayerToMove().opponent();
        }

        if (winner != Player.None) {
            final int eval = winner == Player.White ? Integer.MAX_VALUE - ply: Integer.MIN_VALUE + ply;
            result = new SearchResult(eval, new LinkedList<Move>());
        } else if (remainingDepth == 0) {
            final int eval = Evaluator.evaluate(state);
            result = new SearchResult(eval, new LinkedList<Move>());
        } else if (state.getPlayerToMove() == Player.White) {
            LinkedList<Move> pv = null;
            for (final Move move : moves) {
                final State childState = state.applyMove(move);
                final SearchResult childResult = search(childState, remainingDepth - 1, ply + 1, alpha, beta);
                if (childResult.eval > alpha || pv == null) {
                    pv = childResult.principalVariation;
                    childResult.principalVariation.addFirst(move);
                }
                alpha = Math.max(alpha, childResult.eval);
                if (beta <= alpha || timeIsUp()) {
                    break;
                }
            }
            result = new SearchResult(alpha, pv);
        } else {
            LinkedList<Move> pv = null;
            for (final Move move : moves) {
                final State childState = state.applyMove(move);
                final SearchResult childResult = search(childState, remainingDepth - 1, ply + 1, alpha, beta);
                if (childResult.eval < beta || pv == null) {
                    pv = childResult.principalVariation;
                    childResult.principalVariation.addFirst(move);
                }
                beta = Math.min(beta, childResult.eval);
                if (beta <= alpha || timeIsUp()) {
                    break;
                }
            }
            result = new SearchResult(beta, pv);
        }

        nodes++;

        return result;
    }

    private boolean timeIsUp() {
        flagAboutToFall = flagAboutToFall ||
                (nodes % 10000 == 0) && (System.currentTimeMillis() - startTime >= MOVE_TIME_MS);
        return flagAboutToFall;
    }

}
