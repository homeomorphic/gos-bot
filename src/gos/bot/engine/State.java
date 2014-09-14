package gos.bot.engine;

import gos.bot.protocol.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class State {

    private final boolean firstEverMove;
    private final Player playerToMove;
    private final boolean firstMoveOfSequence;

    private final Player[] owners;
    private final Stone[] stoneTypes;
    private final int[] heights;

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
        return heights[loc.asIndex()];
    }
    public Stone getStoneType(BoardLocation loc) {
        return stoneTypes[loc.asIndex()];
    }
    public Player getOwner(BoardLocation loc) {
        return owners[loc.asIndex()];
    }

    public State() {
        firstEverMove = true;
        playerToMove = Player.White;
        firstMoveOfSequence = true;

        final Board initialBoard = new Board();

        owners = new Player[81];
        stoneTypes = new Stone[81];
        heights = new int[81];
        for (final BoardLocation loc : BOARD_LOCATIONS) {
            owners[loc.asIndex()] = initialBoard.GetOwner(loc);
            stoneTypes[loc.asIndex()] = initialBoard.GetStone(loc);
            heights[loc.asIndex()] = initialBoard.GetHeight(loc);
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
        owners = Arrays.copyOf(prev.owners, prev.owners.length);
        stoneTypes = Arrays.copyOf(prev.stoneTypes, prev.stoneTypes.length);
        heights = Arrays.copyOf(prev.heights, prev.heights.length);

        switch (move.type) {
            case Attack:
                owners[move.from.asIndex()] = Player.None;
                owners[move.to.asIndex()] = prev.playerToMove;
                stoneTypes[move.from.asIndex()] = Stone.None;
                stoneTypes[move.to.asIndex()] = prev.stoneTypes[move.from.asIndex()];
                heights[move.from.asIndex()] = 0;
                heights[move.to.asIndex()] = prev.heights[move.from.asIndex()];
                break;
            case Strengthen:
                owners[move.from.asIndex()] = Player.None;
                owners[move.to.asIndex()] = prev.playerToMove;
                stoneTypes[move.from.asIndex()] = Stone.None;
                stoneTypes[move.to.asIndex()] = prev.stoneTypes[move.from.asIndex()];
                heights[move.from.asIndex()] = 0;
                heights[move.to.asIndex()] = prev.heights[move.from.asIndex()] +
                        prev.heights[move.to.asIndex()];
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
        } while (BoardLocation.IsLegal(x, y) && owners[new BoardLocation(x, y).asIndex()] == Player.None);

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
            if (owners[from.asIndex()] != playerToMove) {
                continue;
            }
            for (final BoardLocation to : toLocations(from)) {
                /* attack? */
                final boolean canAttack =
                        (owners[to.asIndex()] == playerToMove.opponent()) &&
                                (heights[from.asIndex()] >= heights[to.asIndex()]);

                if (canAttack) {
                    result.add(Move.Attack(from, to));
                }

                final boolean canStrengthen =
                        (owners[to.asIndex()] == playerToMove) && !mustAttack;
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
            switch (owners[loc.asIndex()]) {
                case White:
                    stonesByTypeW[stoneTypes[loc.asIndex()].value - 1]++;
                    break;
                case Black:
                    stonesByTypeB[stoneTypes[loc.asIndex()].value - 1]++;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        State state = (State) o;

        if (firstEverMove != state.firstEverMove) return false;
        if (firstMoveOfSequence != state.firstMoveOfSequence) return false;
        if (!Arrays.equals(heights, state.heights)) return false;
        if (!Arrays.equals(owners, state.owners)) return false;
        if (playerToMove != state.playerToMove) return false;
        if (!Arrays.equals(stoneTypes, state.stoneTypes)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (firstEverMove ? 1 : 0);
        result = 31 * result + playerToMove.hashCode();
        result = 31 * result + (firstMoveOfSequence ? 1 : 0);
        result = 31 * result + Arrays.hashCode(owners);
        result = 31 * result + Arrays.hashCode(stoneTypes);
        result = 31 * result + Arrays.hashCode(heights);
        return result;
    }
}
