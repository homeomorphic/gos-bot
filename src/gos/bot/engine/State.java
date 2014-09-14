package gos.bot.engine;

import gos.bot.protocol.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class State {

    private final boolean firstEverMove;
    private final Player playerToMove;
    private final boolean firstMoveOfSequence;

    private final Player[][] owners;
    private final Stone[][] stoneTypes;
    private final int[][] heights;

    public static final BoardLocation[] BOARD_LOCATIONS;

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
    }

    public Player getPlayerToMove() {
        return playerToMove;
    }

    public int getHeight(BoardLocation loc) {
        return heights[loc.X][loc.Y];
    }
    public Stone getStoneType(BoardLocation loc) {
        return stoneTypes[loc.X][loc.Y];
    }
    public Player getOwner(BoardLocation loc) {
        return owners[loc.X][loc.Y];
    }

    public State() {
        firstEverMove = true;
        playerToMove = Player.White;
        firstMoveOfSequence = true;

        final Board initialBoard = new Board();

        owners = new Player[9][];
        stoneTypes = new Stone[9][];
        heights = new int[9][];
        for (int i = 0; i < 9; i++) {
            owners[i] = new Player[9];
            stoneTypes[i] = new Stone[9];
            heights[i] = new int[9];
            for (int j = 0; j < 9; j++) {
                if (!BoardLocation.IsLegal(i, j)) {
                    continue;
                }
                final BoardLocation loc = new BoardLocation(i, j);
                owners[i][j] = initialBoard.GetOwner(loc);
                stoneTypes[i][j] = initialBoard.GetStone(loc);
                heights[i][j] = initialBoard.GetHeight(loc);
            }
        }
    }

    private State(State prev, Move move) {
        this.firstEverMove = false;
        if (prev.firstEverMove) {
            this.firstMoveOfSequence = true;
            this.playerToMove = Player.Black;
        } else {
            this.firstMoveOfSequence = !prev.firstMoveOfSequence;
            this.playerToMove = prev.firstMoveOfSequence ? prev.playerToMove : prev.playerToMove.opponent();
        }
        owners = new Player[9][];
        stoneTypes = new Stone[9][];
        heights = new int[9][];
        for (int i = 0; i < 9; i++) {
            owners[i] = Arrays.copyOf(prev.owners[i], 9);
            stoneTypes[i] = Arrays.copyOf(prev.stoneTypes[i], 9);
            heights[i] = Arrays.copyOf(prev.heights[i], 9);
        }

        switch (move.type) {
            case Attack:
                owners[move.from.X][move.from.Y] = Player.None;
                owners[move.to.X][move.to.Y] = prev.playerToMove;
                stoneTypes[move.from.X][move.from.Y] = Stone.None;
                stoneTypes[move.to.X][move.to.Y] = prev.stoneTypes[move.from.X][move.from.Y];
                heights[move.from.X][move.from.Y] = 0;
                heights[move.to.X][move.to.Y] = prev.heights[move.from.X][move.from.Y];
                break;
            case Strengthen:
                owners[move.from.X][move.from.Y] = Player.None;
                owners[move.to.X][move.to.Y] = prev.playerToMove;
                stoneTypes[move.from.X][move.from.Y] = Stone.None;
                stoneTypes[move.to.X][move.to.Y] = prev.stoneTypes[move.from.X][move.from.Y];
                heights[move.from.X][move.from.Y] = 0;
                heights[move.to.X][move.to.Y] = prev.heights[move.from.X][move.from.Y] +
                        prev.heights[move.to.X][move.to.Y];
                break;
            case Pass:
                break;
        }
    }

    private BoardLocation nonEmptyInDirection(BoardLocation from, int dx, int dy) {
        int x = from.X, y = from.Y;
        do {
            x += dx;
            y += dy;
        } while (BoardLocation.IsLegal(x, y) && owners[x][y] == Player.None);

        return BoardLocation.IsLegal(x, y) ? new BoardLocation(x, y) : null;
    }

    private List<BoardLocation> toLocations(BoardLocation from) {
        final List<BoardLocation> result = new ArrayList<>();
        final BoardLocation n, s, e, w, nw, se;
        n = nonEmptyInDirection(from, 0, -1);
        s = nonEmptyInDirection(from, 0, 1);
        e = nonEmptyInDirection(from, 1, 0);
        w = nonEmptyInDirection(from, -1, 0);
        nw = nonEmptyInDirection(from, -1, -1);
        se = nonEmptyInDirection(from, 1, 1);
        if (n != null) result.add(n);
        if (s != null) result.add(s);
        if (e != null) result.add(e);
        if (w != null) result.add(w);
        if (nw != null) result.add(nw);
        if (se != null) result.add(se);
        return result;
    }

    public List<Move> possibleMoves() {
        final boolean mustAttack = firstMoveOfSequence;
        final List<Move> result = new ArrayList<>();

        if (!mustAttack) {
            result.add(Move.Pass());
        }
        for (final BoardLocation from : BOARD_LOCATIONS) {
            if (owners[from.X][from.Y] != playerToMove) {
                continue;
            }
            for (final BoardLocation to : toLocations(from)) {
                /* attack? */
                final boolean canAttack =
                        (owners[to.X][to.Y] == playerToMove.opponent()) &&
                                (heights[from.X][from.Y] >= heights[to.X][to.Y]);

                if (canAttack) {
                    result.add(Move.Attack(from, to));
                }

                final boolean canStrengthen =
                        (owners[to.X][to.Y] == playerToMove) && !mustAttack;
                if (canStrengthen) {
                    result.add(Move.Strengthen(from, to));
                }
            }
        }
        return result;
    }

    public State applyMove(final Move move) {
        return new State(this, move);
    }


    public Player loser() {
        final int[] stonesByTypeW = new int[3];
        final int[] stonesByTypeB = new int[3];
        for (final BoardLocation loc : BOARD_LOCATIONS) {
            final int x = loc.X, y = loc.Y;
            switch (owners[x][y]) {
                case White:
                    stonesByTypeW[stoneTypes[x][y].value - 1]++;
                    break;
                case Black:
                    stonesByTypeB[stoneTypes[x][y].value - 1]++;
                    break;
            }
        }

        if (stonesByTypeW[0] * stonesByTypeW[1] * stonesByTypeW[2] == 0) {
            return Player.White;
        } else if (stonesByTypeB[0] * stonesByTypeB[1] * stonesByTypeB[2] == 0) {
            return Player.Black;
        } else {
            return Player.None;
        }
    }
}
