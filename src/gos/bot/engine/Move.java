package gos.bot.engine;

import gos.bot.protocol.BoardLocation;
import gos.bot.protocol.MoveType;

public final class Move {

    public final MoveType type;
    public final BoardLocation from;
    public final BoardLocation to;

    private Move(MoveType type, BoardLocation from, BoardLocation to) {
        this.type = type;
        this.from = from;
        this.to = to;
    }

    public static Move Attack(BoardLocation from, BoardLocation to) {
        return new Move(MoveType.Attack, from, to);
    }

    public static Move Strengthen(BoardLocation from, BoardLocation to) {
        return new Move(MoveType.Strengthen, from, to);
    }

    @Override
    public String toString() {
        return "Move{" +
                "type=" + type +
                ", from=" + from +
                ", to=" + to +
                '}';
    }

    public static Move Pass() {
        return new Move(MoveType.Pass, null, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Move move = (Move) o;

        if (from != null ? !from.equals(move.from) : move.from != null) return false;
        if (to != null ? !to.equals(move.to) : move.to != null) return false;
        if (type != move.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + (from != null ? from.hashCode() : 0);
        result = 31 * result + (to != null ? to.hashCode() : 0);
        return result;
    }
}
