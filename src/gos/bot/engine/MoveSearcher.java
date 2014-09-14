package gos.bot.engine;

import gos.bot.protocol.Player;

import java.util.List;

final class MoveSearcher {

    public static Move search(State state) {
        final SearchResult result = search(state, 4, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
        return result.bestMove;
    }

    private static final class SearchResult {
        public final float eval;
        public final Move bestMove;

        public SearchResult(float eval, Move bestMove) {
            this.eval = eval;
            this.bestMove = bestMove;
        }
    }

    private static SearchResult search(State state, int remainingDepth, float alpha, float beta) {
        final List<Move> moves = state.possibleMoves();
        final Player winner;

        if (moves.size() == 0) {
            winner = state.getPlayerToMove().opponent();
        } else {
            winner = state.loser();
        }

        if (winner != Player.None) {
            final float eval = winner == Player.White ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
            return new SearchResult(eval, null);
        }

        if (remainingDepth == 0) {
            final float eval = Evaluator.evaluate(state);
            return new SearchResult(eval, null);
        }

        if (state.getPlayerToMove() == Player.White) {
            Move bestMove = null;
            for (final Move move : moves) {
                final SearchResult childResult = search(state.applyMove(move), remainingDepth - 1, alpha, beta);
                if (childResult.eval > alpha || bestMove == null) {
                    bestMove = move;
                }
                alpha = Math.max(alpha, childResult.eval);
                if (beta <= alpha) {
                    break;
                }
            }
            return new SearchResult(alpha, bestMove);
        } else {
            Move bestMove = null;
            for (final Move move : moves) {
                final SearchResult childResult = search(state.applyMove(move), remainingDepth - 1, alpha, beta);
                if (childResult.eval < beta || bestMove == null) {
                    bestMove = move;
                }
                beta = Math.min(beta, childResult.eval);
                if (beta <= alpha) {
                    break;
                }
            }
            return new SearchResult(beta, bestMove);
        }
    }

}
