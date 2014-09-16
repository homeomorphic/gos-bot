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

    final long occupiedBB;

    public Player getPlayerToMove() {
        return playerToMove;
    }

    public int getHeight(byte loc) {
        return heights[loc];
    }
    public Stone getStoneType(byte loc) {
        return stoneTypes[loc];
    }
    public Player getOwner(byte loc) {
        return owners[loc];
    }

    public boolean mustAttack() {
        return firstMoveOfSequence;
    }

    public State() {
        firstEverMove = true;
        playerToMove = Player.White;
        firstMoveOfSequence = true;

        final Board initialBoard = new Board();

        owners = new Player[Move.N_BOARD_LOCATIONS];
        stoneTypes = new Stone[Move.N_BOARD_LOCATIONS];
        heights = new int[Move.N_BOARD_LOCATIONS];
        for (int i = 0; i < Move.N_BOARD_LOCATIONS; i++) {
            final BoardLocation loc = Move.BOARD_LOCATIONS[i];
            owners[i] = initialBoard.GetOwner(loc);
            stoneTypes[i] = initialBoard.GetStone(loc);
            heights[i] = initialBoard.GetHeight(loc);
        }
        occupiedBB = occupiedBitBoard();
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
                owners[move.from] = Player.None;
                owners[move.to] = prev.playerToMove;
                stoneTypes[move.from] = Stone.None;
                stoneTypes[move.to] = prev.stoneTypes[move.from];
                heights[move.from] = 0;
                heights[move.to] = prev.heights[move.from];
                break;
            case Strengthen:
                owners[move.from] = Player.None;
                owners[move.to] = prev.playerToMove;
                stoneTypes[move.from] = Stone.None;
                stoneTypes[move.to] = prev.stoneTypes[move.from];
                heights[move.from] = 0;
                heights[move.to] = prev.heights[move.from] + prev.heights[move.to];
                break;
            case Pass:
                break;
        }
        occupiedBB = occupiedBitBoard();
    }

    private long occupiedBitBoard() {
        long result = 0;
        for (byte loc = 0; loc < Move.N_BOARD_LOCATIONS; loc++) {
            if (owners[loc] != Player.None) {
                result |= (1L << loc);
            }
        }
        return result;
    }


    public List<Move> possibleMoves() {
        final boolean mustAttack = mustAttack();
        final List<Move> result = new ArrayList<>();

        if (!mustAttack) {
            result.add(Move.PASS);
        }
        for (byte from = 0; from < Move.N_BOARD_LOCATIONS; from++) {
            if (owners[from] != playerToMove) {
                continue;
            }
            final long toBB = RayAttacks.positions(occupiedBB, from);
            for (byte to = 0; to < Move.N_BOARD_LOCATIONS; to++) {
                if ((toBB & (1L << to)) == 0) {
                    continue;
                }
                final boolean canAttack =
                        (owners[to] == playerToMove.opponent()) &&
                                (heights[from] >= heights[to]);

                if (canAttack) {
                    result.add(Move.Attack(from, to));
                }

                final boolean canStrengthen = !mustAttack && (owners[to] == playerToMove);
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
        for (byte loc = 0; loc < Move.N_BOARD_LOCATIONS; loc++) {
            switch (owners[loc]) {
                case White:
                    stonesByTypeW[stoneTypes[loc].value - 1]++;
                    break;
                case Black:
                    stonesByTypeB[stoneTypes[loc].value - 1]++;
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
