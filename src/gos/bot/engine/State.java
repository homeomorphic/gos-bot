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

    private final long occupied;
    private final long occupiedW;
    private final long occupiedB;

    public long getOccupied(Player player) {
        switch (player) {
            case Black: return occupiedB;
            case White: return occupiedW;
            default: throw new IllegalArgumentException("player");
        }
    }

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
        occupiedW = occupation(Player.White);
        occupiedB = occupation(Player.Black);
        occupied = occupiedB | occupiedW;
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
        occupiedW = occupation(Player.White);
        occupiedB = occupation(Player.Black);
        occupied = occupiedB | occupiedW;
    }

    public Board asProtocolBoard() {
        final Board result = new Board();
        for (byte pos = 0; pos < Move.N_BOARD_LOCATIONS; pos++) {
            final BoardLocation loc = Move.BOARD_LOCATIONS[pos];
            if (owners[pos] == Player.None) {
                result.ClearSpace(loc);
            } else {
                result.SetSpace(loc, owners[pos], stoneTypes[pos], heights[pos]);
            }
        }
        return result;
    }

    private long occupation(Player player) {
        long result = 0L;
        for (byte loc = 0; loc < Move.N_BOARD_LOCATIONS; loc++) {
            if (owners[loc] == player) {
                result |= (1L << loc);
            }
        }
        return result;
    }


    public List<Move> possibleMoves() {
        final boolean mustAttack = mustAttack();
        final List<Move> result = new ArrayList<>(Move.N_BOARD_LOCATIONS * 2 + 1);

        final long myPositions = playerToMove == Player.White ? occupiedW : occupiedB;
        final long oppPositions = playerToMove == Player.White ? occupiedB : occupiedW;

        final Player opponent = playerToMove.opponent();

        if (!mustAttack) {
            result.add(Move.PASS);
        }

        long fromPositions = playerToMove == Player.White ? occupiedW : occupiedB;
        while (fromPositions != 0) {
            final int from = Long.numberOfTrailingZeros(fromPositions);
            fromPositions ^= (1L << from);

            long toPositions = RayAttacks.positions(occupied, from);
            if (mustAttack) {
                toPositions &= oppPositions;
            }
            while (toPositions != 0) {
                final int to = Long.numberOfTrailingZeros(toPositions);
                toPositions ^= (1L << to);
                final boolean canAttack =
                        (oppPositions & (1L << to)) != 0 &&
                                (heights[from] >= heights[to]);

                if (canAttack) {
                    result.add(Move.Attack((byte)from, (byte)to));
                }

                final boolean canStrengthen = !mustAttack && (myPositions & (1L << to)) != 0;
                if (canStrengthen) {
                    result.add(Move.Strengthen((byte)from, (byte)to));
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
