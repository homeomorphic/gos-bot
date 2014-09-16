package gos.bot.engine;

import gos.bot.protocol.BoardLocation;
import gos.bot.protocol.Player;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class StateTest extends TestCase {

    private State state;

    @Before
    public void setUp() {
        state = new State();
    }

    private BoardLocation nonEmptyInDirection(byte from, int dx, int dy) {
        final BoardLocation loc = Move.BOARD_LOCATIONS[from];
        int x = loc.X, y = loc.Y;
        do {
            x += dx;
            y += dy;
        } while (BoardLocation.IsLegal(x, y) &&
                state.getOwner(Move.fromBoardLocation(new BoardLocation(x, y))) == Player.None);

        return BoardLocation.IsLegal(x, y) ? new BoardLocation(x, y) : null;
    }

    private List<BoardLocation> toLocations(byte from) {
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

    private List<Move> possibleMoves() {
        final boolean mustAttack = state.mustAttack();
        final List<Move> result = new ArrayList<>();

        if (!mustAttack) {
            result.add(Move.PASS);
        }
        for (byte from = 0; from < Move.N_BOARD_LOCATIONS; from++) {
            if (state.getOwner(from) != state.getPlayerToMove()) {
                continue;
            }
            for (final BoardLocation to : toLocations(from)) {
                /* attack? */
                final byte toB = Move.fromBoardLocation(to);
                final boolean canAttack =
                        (state.getOwner(toB) == state.getPlayerToMove().opponent()) &&
                                (state.getHeight(from) >= state.getHeight(toB));

                if (canAttack) {
                    result.add(Move.Attack(from, toB));
                }

                final boolean canStrengthen =
                        (state.getOwner(toB) == state.getPlayerToMove()) && !mustAttack;
                if (canStrengthen) {
                    result.add(Move.Strengthen(from, toB));
                }
            }
        }
        return result;
    }

    private void compareCurrentState() {
        final Set<Move> ref = new HashSet<>();
        ref.addAll(this.possibleMoves());
        final Set<Move> got = new HashSet<>();
        got.addAll(state.possibleMoves());
        assertEquals(ref, got);
    }

    @Test
    public void testRoot() {
        compareCurrentState();
    }

    @Test
    public void testTenMovesDeep() {
        for (int i = 0; i < 10; i++) {
            compareCurrentState();
            final List<Move> moves = state.possibleMoves();
            state = state.applyMove(moves.get(i));
            compareCurrentState();
        }
    }

    @Test
    public void stressTest() {
        for (int i = 0; i < 100; i++) {
            state = new State();
            final Random random = new Random();
            boolean movesLeft;
            do {
                compareCurrentState();
                final List<Move> moves = state.possibleMoves();
                movesLeft = moves.size() > 0;
                if (movesLeft) {
                    final Move randomMove = moves.get(random.nextInt(moves.size()));
                    state = state.applyMove(randomMove);
                }
            } while (movesLeft);
        }
    }

}