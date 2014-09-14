package gos.bot.engine;

import gos.bot.protocol.MoveType;

import java.util.List;

final class BetterMoveHeuristic implements java.util.Comparator<Move> {

    private final State state;
    private final List<Move> principalVariation;
    private final int ply;

    public BetterMoveHeuristic(State state, List<Move> principalVariation, int ply) {
        this.state = state;
        this.principalVariation = principalVariation;
        this.ply = ply;
    }

    @Override
    public int compare(Move move1, Move move2) {
        if (move1.equals(principalVariation.get(ply))) {
            return -1;
        } else if (move2.equals(principalVariation.get(ply))) {
            return 1;
        } else {
            return 0;
        }
    }

    private int compareTypes(Move move1, Move move2) {
        switch (move1.type) {
            case Attack:
                switch (move2.type) {
                    case Attack:
                        return 0;
                    default:
                        return 1;
                }
            case Strengthen:
                switch (move2.type) {
                    case Attack:
                        return -1;
                    case Strengthen:
                        return 0;
                    case Pass:
                        return 1;
                }
            case Pass:
                switch (move2.type) {
                    case Pass:
                        return 0;
                    default:
                        return -1;
                }
        }
        throw new IllegalArgumentException();
    }
}
