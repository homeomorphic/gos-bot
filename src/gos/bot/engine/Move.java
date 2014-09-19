package gos.bot.engine;

import gos.bot.protocol.BoardLocation;
import gos.bot.protocol.MoveType;

import java.util.ArrayList;
import java.util.List;

public final class Move {

    /*
           A B C D E F G H I
         -------------------
        0| X X X X X . . . .
        1| X X X X X X . . .
        2| X X X X X X X . .
        3| X X X X X X X X .
        4| X X X X . X X X X
        5| . X X X X X X X X
        6| . . X X X X X X X
        7| . . . X X X X X X
        8| . . . . X X X X X
         */

    private static final byte[] X_OFFSETS =
            { 0, 5, 5+6, 5+6+7, 5+6+7+8, 5+6+7+8+8-1, 5+6+7+8+8+8-2,
                    5+6+7+8+8+8+7-3, 5+6+7+8+8+8+7+6-4 };


    public static final BoardLocation[] BOARD_LOCATIONS;

    public static final byte N_BOARD_LOCATIONS;

    static {
        final List<BoardLocation> boardLocations = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (BoardLocation.IsLegal(i, j)) {
                    boardLocations.add(new BoardLocation(i, j));
                }
            }
        }
        BOARD_LOCATIONS = boardLocations.toArray(new BoardLocation[boardLocations.size()]);
        N_BOARD_LOCATIONS = (byte)BOARD_LOCATIONS.length;

        /* Sanity check. */
        for (byte loc = 0; loc < N_BOARD_LOCATIONS; loc++) {
            final BoardLocation boardLocation = BOARD_LOCATIONS[loc];
            if (fromBoardLocation(boardLocation) != loc) {
                throw new IllegalStateException();
            }
        }
    }


    public final MoveType type;
    public final byte from;
    public final byte to;

    public static final Move PASS = new Move(MoveType.Pass, (byte)-1, (byte)-1);

    public static Move attack(byte from, byte to) {
        return new Move(MoveType.Attack, from, to);
    }

    public static Move strengthen(byte from, byte to) {
        return new Move(MoveType.Strengthen, from, to);
    }

    private Move(MoveType type, byte from, byte to) {
        this.type = type;
        this.from = from;
        this.to = to;
    }

    @Override
    public String toString() {
        if (type == MoveType.Pass) {
            return "{Pass}";
        } else {
            final BoardLocation from = BOARD_LOCATIONS[this.from];
            final BoardLocation to = BOARD_LOCATIONS[this.to];
            return "{" + type +
                    " from=" + from +
                    " to=" + to +
                    '}';
        }
    }

    public gos.bot.protocol.Move asProtocolMove() {
        if (type == MoveType.Pass) {
            return new gos.bot.protocol.Move(type, null, null);
        } else {
            final BoardLocation from = BOARD_LOCATIONS[this.from];
            final BoardLocation to = BOARD_LOCATIONS[this.to];
            return new gos.bot.protocol.Move(type, from, to);
        }
    }

    public static byte fromBoardLocation(BoardLocation boardLocation) {
        byte result = (byte)(X_OFFSETS[boardLocation.X] + boardLocation.Y);
        if (boardLocation.X == 4 && boardLocation.Y >= 4) {
            /* Account for unusable square in the middle of the board. */
            result--;
        }
        return result;
    }

    public static Move of(gos.bot.protocol.Move move) {
        if (move.Type.equals(MoveType.Pass)) {
            return PASS;
        } else {
            final byte from = fromBoardLocation(move.From);
            final byte to = fromBoardLocation(move.To);
            return new Move(move.Type, from, to);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Move move = (Move) o;

        if (from != move.from) return false;
        if (to != move.to) return false;
        if (type != move.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + (int) from;
        result = 31 * result + (int) to;
        return result;
    }
}
