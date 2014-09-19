package gos.bot.engine;

import junit.framework.TestCase;
import org.junit.Test;

public class MoveTest extends TestCase {

    @Test
    public void testAttacks() {
        for (byte from = 0; from < Move.N_BOARD_LOCATIONS; from++) {
            for (byte to = 0; to < Move.N_BOARD_LOCATIONS; to++) {
                final Move attack = Move.attack(from, to);
                attack.asProtocolMove();

            }
        }
    }

    @Test
    public void testStrenghtens() {
        for (byte from = 0; from < Move.N_BOARD_LOCATIONS; from++) {
            for (byte to = 0; to < Move.N_BOARD_LOCATIONS; to++) {
                final Move strengthen = Move.strengthen(from, to);
                strengthen.asProtocolMove();
            }
        }
    }

    @Test
    public void testPass() {
        Move.PASS.asProtocolMove();
    }

}