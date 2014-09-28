package gos.bot.engine;

import gos.bot.protocol.Player;

import java.util.*;

final class MoveSearcher {

    private static final long MOVE_TIME_MS = 1800;
    private static final int MAX_DEPTH = 10;

    private long nodes;
    private long startTime;
    private boolean flagAboutToFall;
    private int depth;
    private long endTime;
    private Move[] lastPrincipalVariation;

    private Move[][] killerMove;

    public Move search(State rootState) {
        nodes = 0;
        startTime = System.currentTimeMillis();
        flagAboutToFall = false;
        depth = 1;
        endTime = 0;
        lastPrincipalVariation = new Move[MAX_DEPTH+1];
        killerMove = new Move[MAX_DEPTH+1][];
        for (int i = 0; i < killerMove.length; i++) {
            killerMove[i] = new Move[2];
        }

        SearchResult lastCompleteSearchResult = null;
        do {
            final SearchResult searchResult =
                    search(rootState, depth, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);

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

            // emit();
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

        public boolean decisive() {
            return eval == Integer.MIN_VALUE || eval == Integer.MAX_VALUE;
        }
    }

    private List<Move> orderMoves(List<Move> moves, State state, int ply) {

        final Move pref0 = lastPrincipalVariation[ply];
        Move pref1 = killerMove[ply][0];
        Move pref2 = killerMove[ply][1];
        if (Objects.equals(pref0, pref1)) {
            pref1 = null;
        }
        if (Objects.equals(pref0, pref2) || Objects.equals(pref1, pref2)) {
            pref2 = null;
        }

        boolean hasPref0 = false;
        boolean hasPref1 = false;
        boolean hasPref2 = false;
        for (final Move move : moves) {
            hasPref0 = hasPref0 || move.equals(pref0);
            hasPref1 = hasPref1 || move.equals(pref1);
            hasPref2 = hasPref2 || move.equals(pref2);

        }

        final List<Move> result = new ArrayList<Move>(moves.size());
        if (hasPref0) {
            result.add(pref0);
        }
        if (hasPref1) {
            result.add(pref1);
        }
        if (hasPref2) {
            result.add(pref2);
        }

        for (final Move move : moves) {
            if (!move.equals(pref0) && !move.equals(pref1) && !move.equals(pref2)) {
                result.add(move);
            }
        }

        return result;
    }



    private SearchResult search(State state, int remainingDepth, int ply, int alpha, int beta) {
        final List<Move> possibleMoves = state.possibleMoves();
        final List<Move> moves = orderMoves(possibleMoves, state, ply);

        Player winner;

        winner = state.loser().opponent();
        if (winner == Player.None && moves.size() == 0) {
            /* zugzwang */
            winner = state.getPlayerToMove().opponent();
        }

        final SearchResult result;

        if (winner != Player.None) {
            final int eval = winner == Player.White ? Integer.MAX_VALUE: Integer.MIN_VALUE;
            result = new SearchResult(eval);
        } else if (remainingDepth == 0) {
            final int eval = Evaluator.evaluate(state);
            result = new SearchResult(eval);
        } else if (state.getPlayerToMove() == Player.White) {
            LinkedList<Move> pv = null;
            for (final Move move : moves) {
                final State childState = state.applyMove(move);

                final SearchResult childResult;
                childResult = search(childState, remainingDepth - 1, ply + 1, alpha, beta);

                if (childResult.eval > alpha || pv == null) {
                    pv = childResult.pv;
                    pv.addFirst(move);
                }

                alpha = Math.max(alpha, childResult.eval);
                if (beta <= alpha) {                    /* Score will be >= beta. beta-cutoff. */
                    if (!move.equals(killerMove[ply][0])) {
                        killerMove[ply][1] = killerMove[ply][0];
                        killerMove[ply][0] = move;
                    }

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

                final SearchResult childResult;
                childResult = search(childState, remainingDepth - 1, ply + 1, alpha, beta);

                if (childResult.eval < beta || pv == null) {
                    pv = childResult.pv;
                    pv.addFirst(move);
                }

                beta = Math.min(beta, childResult.eval);
                if (beta <= alpha) {
                    /* Score will be <= alpha. alpha-cutoff. */
                    if (!move.equals(killerMove[ply][0])) {
                        killerMove[ply][1] = killerMove[ply][0];
                        killerMove[ply][0] = move;
                    }
                    break;
                }
                if (timeIsUp()) {
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

    private void emit() {
        endTime = System.currentTimeMillis();
        System.err.println("" + (endTime - startTime) + "; depth = " + depth);
    }

}
