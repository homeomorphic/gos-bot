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

    public static Move Pass() {
        return new Move(MoveType.Pass, null, null);
    }

}
