package gos.bot.engine;

import gos.bot.protocol.Player;

import java.util.ArrayList;
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
    private Move[] lastPrincipalVariation;

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
        lastPrincipalVariation = new Move[MAX_DEPTH+1];

        SearchResult lastCompleteSearchResult = null;
        do {
            final SearchResult searchResult =
                    search(rootState, depth, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, null);

            if (timeIsUp()) {
                if (lastCompleteSearchResult == null) {
                    /* we're very slow for some reason. shouldn't happen. */
                    lastCompleteSearchResult = searchResult;
                }
                break;
            }

            lastCompleteSearchResult = searchResult;
            for (int i = 0; i < lastPrincipalVariation.length; i++) {
                lastPrincipalVariation[i] = i < searchResult.pv.size() ? searchResult.pv.get(i) : null;
            }

            emit();
            depth++;
        } while (depth <= MAX_DEPTH);
        endTime = System.currentTimeMillis();
        return lastCompleteSearchResult.bestMove();
    }

    public double nps() {
        return nodes / ((endTime - startTime) / 1000.0);
    }
    public long nodes() {
        return nodes;
    }

    private static final class SearchResult {
        public final int eval;
        public final LinkedList<Move> pv;

        private SearchResult(int eval, LinkedList<Move> pv) {
            this.eval = eval;
            this.pv = pv;
        }

        private SearchResult(int eval) {
            this.eval = eval;
            this.pv = new LinkedList<>();
        }

        public Move bestMove() {
            return pv.isEmpty() ? null : pv.get(0);
        }
    }

    private List<Move> orderMoves(List<Move> moves, State state, int ply, TranspositionTable.Entry existingEntry) {

        final Move pref0 = lastPrincipalVariation[ply];
        final Move pref1 = existingEntry != null ? existingEntry.bestMove : null;

        boolean hasPref0 = false;
        boolean hasPref1 = false;
        for (final Move move : moves) {
            hasPref0 = hasPref0 | move.equals(pref0);
            hasPref1 = hasPref1 | move.equals(pref1);
        }

        final List<Move> result = new ArrayList<Move>(moves.size());
        if (hasPref0) {
            result.add(pref0);
        }
        if (hasPref1) {
            result.add(pref1);
        }

        for (final Move move : moves) {
            if (!move.equals(pref0) && !move.equals(pref1)) {
                result.add(move);
            }
        }

        return result;
    }



    private SearchResult search(State state, int remainingDepth, int ply, int alpha, int beta,
                                    TranspositionTable.Entry existingEntry) {
        final List<Move> possibleMoves = state.possibleMoves();
        final List<Move> moves = orderMoves(possibleMoves, state, ply, existingEntry);

        Player winner;

        winner = state.loser().opponent();
        if (winner == Player.None && moves.size() == 0) {
            /* zugzwang */
            winner = state.getPlayerToMove().opponent();
        }

        final SearchResult result;
        TranspositionTable.EntryType entryType = TranspositionTable.EntryType.EXACT;

        if (winner != Player.None) {
            final int eval = winner == Player.White ? Integer.MAX_VALUE - ply: Integer.MIN_VALUE + ply;
            result = new SearchResult(eval);
        } else if (remainingDepth == 0) {
            final int eval = Evaluator.evaluate(state);
            result = new SearchResult(eval);
        } else if (state.getPlayerToMove() == Player.White) {
            LinkedList<Move> pv = null;
            for (final Move move : moves) {
                final State childState = state.applyMove(move);

                final TranspositionTable.Entry existingChildEntry =
                            transpositionTable.lookup(childState);

                final SearchResult childResult;
                if (existingChildEntry != null && existingChildEntry.evaluation >= beta
                        && existingChildEntry.isUsableForLowerBound(remainingDepth)) {
                    final int childEval = existingChildEntry.evaluation;
                    childResult = new SearchResult(childEval);
                } else {
                    childResult = search(childState, remainingDepth - 1, ply + 1, alpha, beta, existingChildEntry);
                }

                if (childResult.eval > alpha || pv == null) {
                    pv = childResult.pv;
                    pv.addFirst(move);
                }

                alpha = Math.max(alpha, childResult.eval);
                if (beta <= alpha) {                    /* Score will be >= beta. beta-cutoff. */
                    entryType = TranspositionTable.EntryType.LOWER_BOUND;
                    break;
                }
                if (timeIsUp()) {
                    break;
                }
            }
            result = new SearchResult(alpha, pv);
        } else {
            LinkedList<Move> pv = null;
            for (final Move move : moves) {
                final State childState = state.applyMove(move);

                final TranspositionTable.Entry existingChildEntry =
                        transpositionTable.lookup(childState);

                final SearchResult childResult;
                if (existingChildEntry != null && existingChildEntry.evaluation <= alpha
                        && existingChildEntry.isUsableForUpperBound(remainingDepth)) {
                    final int childEval = existingChildEntry.evaluation;
                    childResult = new SearchResult(childEval);
                } else {
                    childResult = search(childState, remainingDepth - 1, ply + 1, alpha, beta, existingChildEntry);
                }

                if (childResult.eval < beta || pv == null) {
                    pv = childResult.pv;
                    pv.addFirst(move);
                }

                beta = Math.min(beta, childResult.eval);
                if (beta <= alpha) {
                    /* Score will be <= alpha. alpha-cutoff. */
                    entryType = TranspositionTable.EntryType.UPPER_BOUND;
                    break;
                }
                if (timeIsUp()) {
                    break;
                }
            }
            result = new SearchResult(beta, pv);
        }

        if (!timeIsUp()) {
            transpositionTable.store(
                    new TranspositionTable.Entry(state, remainingDepth, result.eval, entryType, result.bestMove()));

        }

        nodes++;

        return result;
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
