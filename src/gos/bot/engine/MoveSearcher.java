package gos.bot.engine;

import gos.bot.protocol.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class MoveSearcher {

    private static final long MOVE_TIME_MS = 1500;
    private static final int MAX_DEPTH = 8;
    private double nps;
    private long nodes;

    private List<Move> principalVariation;


    public Move search(State state) {
        final long startTime = System.currentTimeMillis();
        int depth = 1;
        principalVariation = new ArrayList<>();
        SearchResult result;
        long curTime;
        do {
            result = search(state, depth, 0, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
            curTime = System.currentTimeMillis();
            System.err.println("depth = " + depth + "; best move = " + result + "; PV = " + principalVariation);
            depth++;
        } while (curTime - startTime < MOVE_TIME_MS && depth <= MAX_DEPTH);
        final long endTime = System.currentTimeMillis();
        nps = nodes / ((endTime - startTime) / 1000.0);
        return result.bestMove;
    }

    public double nps() {
        return nps;
    }
    public long nodes() {
        return nodes;
    }

    private static final class SearchResult {
        public final float eval;
        public final Move bestMove;

        public SearchResult(float eval, Move bestMove) {
            this.eval = eval;
            this.bestMove = bestMove;
        }

        @Override
        public String toString() {
            return "SearchResult{" +
                    "eval=" + eval +
                    ", bestMove=" + bestMove +
                    '}';
        }
    }

    private SearchResult search(State state, int remainingDepth, int ply, float alpha, float beta) {
        final List<Move> moves = state.possibleMoves();
        if (ply < principalVariation.size()) {
            Collections.sort(moves, new BetterMoveHeuristic(state, principalVariation, ply));
        }
        final Player winner;
        final SearchResult result;

        if (moves.size() == 0) {
            winner = state.getPlayerToMove().opponent();
        } else {
            winner = state.loser().opponent();
        }

        if (winner != Player.None) {
            final float eval = winner == Player.White ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
            result = new SearchResult(eval, null);
        } else if (remainingDepth == 0) {
            final float eval = Evaluator.evaluate(state);
            result = new SearchResult(eval, null);
        } else if (state.getPlayerToMove() == Player.White) {
            Move bestMove = null;
            for (final Move move : moves) {
                final State childState = state.applyMove(move);
                final SearchResult childResult = search(childState, remainingDepth - 1, ply + 1, alpha, beta);
                if (childResult.eval > alpha || bestMove == null) {
                    bestMove = move;
                }
                alpha = Math.max(alpha, childResult.eval);
                if (beta <= alpha) {
                    break;
                }
            }
            result = new SearchResult(alpha, bestMove);
        } else {
            Move bestMove = null;
            for (final Move move : moves) {
                final State childState = state.applyMove(move);
                final SearchResult childResult = search(childState, remainingDepth - 1, ply + 1, alpha, beta);
                if (childResult.eval < beta || bestMove == null) {
                    bestMove = move;
                }
                beta = Math.min(beta, childResult.eval);
                if (beta <= alpha) {
                    break;
                }
            }
            result = new SearchResult(beta, bestMove);
        }

        nodes++;
        if (ply < principalVariation.size()) {
            principalVariation.set(ply, result.bestMove);
        } else {
            principalVariation.add(result.bestMove);
        }

        return result;
    }

}
