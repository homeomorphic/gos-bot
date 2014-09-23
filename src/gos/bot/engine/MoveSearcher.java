package gos.bot.engine;

import gos.bot.protocol.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

final class MoveSearcher {

    private static final long MOVE_TIME_MS = 1800;
    private static final int MAX_DEPTH = 10;

    private long nodes;
    private long startTime;
    private boolean flagAboutToFall;
    private int depth;

    private long endTime;

    private TranspositionTable transpositionTable;

    public MoveSearcher() {
        transpositionTable = new TranspositionTable();
    }

    public Move search(State rootState) {
        nodes = 0;
        startTime = System.currentTimeMillis();
        flagAboutToFall = false;
        depth = 1;
        endTime = 0;

        Move bestMove = null;
        do {
            final SearchResult searchResult =
                    search(rootState, depth, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, null);

            if (timeIsUp()) {
                if (bestMove == null) {
                    /* we're very slow for some reason. shouldn't happen. */
                    bestMove = searchResult.bestMove;
                }
                break;
            }

            bestMove = searchResult.bestMove;
            emit();
            depth++;
        } while (depth <= MAX_DEPTH);
        endTime = System.currentTimeMillis();
        return bestMove;
    }

    public double nps() {
        return nodes / ((endTime - startTime) / 1000.0);
    }
    public long nodes() {
        return nodes;
    }

    private static final class SearchResult {
        public final int eval;
        public final Move bestMove;

        private SearchResult(int eval, Move bestMove) {
            this.eval = eval;
            this.bestMove = bestMove;
        }
    }

    private void orderMoves(List<Move> moves, Move bestMove) {
        for (int i = 0; i < moves.size(); i++) {
            if (moves.get(i).equals(bestMove)) {
                moves.set(i, moves.get(0));
                moves.set(0, bestMove);
                break;
            }
        }
    }


    private SearchResult search(State state, int remainingDepth, int ply, int alpha, int beta,
                                    TranspositionTable.Entry existingEntry) {
        final List<Move> moves = state.possibleMoves();
        if (existingEntry != null) {
            orderMoves(moves, existingEntry.bestMove);
        }

        Player winner;
        final int eval;

        winner = state.loser().opponent();
        if (winner == Player.None && moves.size() == 0) {
            /* zugzwang */
            winner = state.getPlayerToMove().opponent();
        }

        Move bestMove = null;
        TranspositionTable.EntryType entryType = TranspositionTable.EntryType.EXACT;

        if (winner != Player.None) {
            eval = winner == Player.White ? Integer.MAX_VALUE - ply: Integer.MIN_VALUE + ply;
        } else if (remainingDepth == 0) {
            eval = Evaluator.evaluate(state);
        } else if (state.getPlayerToMove() == Player.White) {
            for (final Move move : moves) {
                final State childState = state.applyMove(move);

                final TranspositionTable.Entry existingChildEntry =
                            transpositionTable.lookup(childState);

                final int childEval;
                if (existingChildEntry != null && existingChildEntry.evaluation >= beta
                        && existingChildEntry.isUsableForLowerBound(remainingDepth)) {
                    childEval = existingChildEntry.evaluation;
                } else {
                    childEval = search(childState, remainingDepth - 1, ply + 1, alpha, beta, existingChildEntry).eval;
                }

                if (childEval > alpha || bestMove == null) {
                    bestMove = move;
                }

                alpha = Math.max(alpha, childEval);
                if (beta <= alpha) {
                    /* Score will be >= beta. beta-cutoff. */
                    entryType = TranspositionTable.EntryType.LOWER_BOUND;
                    break;
                }
                if (timeIsUp()) {
                    break;
                }
            }
            eval = alpha;
        } else {
            for (final Move move : moves) {
                final State childState = state.applyMove(move);

                final TranspositionTable.Entry existingChildEntry =
                        transpositionTable.lookup(childState);

                final int childEval;
                if (existingChildEntry != null && existingChildEntry.evaluation <= alpha
                        && existingChildEntry.isUsableForUpperBound(remainingDepth)) {
                    childEval = existingChildEntry.evaluation;
                } else {
                    childEval = search(childState, remainingDepth - 1, ply + 1, alpha, beta, existingChildEntry).eval;
                }

                if (childEval < beta || bestMove == null) {
                    bestMove = move;
                }

                beta = Math.min(beta, childEval);
                if (beta <= alpha) {
                    /* Score will be <= alpha. alpha-cutoff. */
                    entryType = TranspositionTable.EntryType.UPPER_BOUND;
                    break;
                }
                if (timeIsUp()) {
                    break;
                }
            }
            eval = beta;
        }

        if (!timeIsUp()) {
            transpositionTable.store(
                    new TranspositionTable.Entry(state, remainingDepth, eval, entryType, bestMove));
        }

        nodes++;

        return new SearchResult(eval, bestMove);
    }


    private boolean timeIsUp() {
        flagAboutToFall = flagAboutToFall ||
                (nodes % 10000 == 0) && (System.currentTimeMillis() - startTime >= MOVE_TIME_MS);
        return flagAboutToFall;
    }

    private void emit() {
        endTime = System.currentTimeMillis();
        System.err.println("" + (endTime - startTime) + "; depth = " + depth);
    }

}
