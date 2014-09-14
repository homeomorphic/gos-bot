package gos.bot.engine;

import gos.bot.protocol.Player;

import java.util.HashMap;
import java.util.List;

final class MoveSearcher {

    private final State state;
    private static final int DEPTH = 4;
    private final HashMap<State, Float> tt;
    private double nps;

    public MoveSearcher(State state) {
        this.state = state;
        this.tt = new HashMap<>();
    }

    public Move search() {
        final long startTime = System.currentTimeMillis();
        final SearchResult result = search(state, DEPTH, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
        final long endTime = System.currentTimeMillis();
        nps = tt.size() / ((endTime - startTime) / 1000.0);
        return result.bestMove;
    }

    public double nps() {
        return nps;
    }
    public long nodes() {
        return tt.size();
    }

    private static final class SearchResult {
        public final float eval;
        public final Move bestMove;

        public SearchResult(float eval, Move bestMove) {
            this.eval = eval;
            this.bestMove = bestMove;
        }
    }

    private SearchResult search(State state, int remainingDepth, float alpha, float beta) {
        final List<Move> moves = state.possibleMoves();
        final Player winner;
        final SearchResult result;

        final Float cachedEval = tt.get(state);
        if (cachedEval != null) {
            return new SearchResult(cachedEval, null);
        }

        if (moves.size() == 0) {
            winner = state.getPlayerToMove().opponent();
        } else {
            winner = state.loser();
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
                final SearchResult childResult = search(childState, remainingDepth - 1, alpha, beta);
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
                final SearchResult childResult = search(childState, remainingDepth - 1, alpha, beta);
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

        tt.put(state, result.eval);

        return result;
    }

}
