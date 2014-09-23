package gos.bot.engine;

import gos.bot.protocol.BoardLocation;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.Stack;

public class MoveSearcherTest extends TestCase {

    private Stack<State> stack;
    /*
{"Color":"-1"}
{"Player":"1","Move":{"Type":"1","From":{"X":2,"Y":2},"To":{"X":2,"Y":3}},"Winner":"0"}
{"Board":{"state":[[5,5,5,5,-5,0,0,0,0],[-5,6,6,6,-6,-5,0,0,0],[-5,-6,0,7,-7,-6,-5,0,0],[-5,-6,7,5,-5,-7,-6,-5,0],[-5,-6,-7,-5,0,5,7,6,5],[0,5,6,7,5,-5,7,6,5],[0,0,5,6,7,-7,-7,6,5],[0,0,0,5,6,-6,-6,-6,5],[0,0,0,0,5,-5,-5,-5,-5]]},"AllowedMoves":["1"]}
{"Player":"-1","Move":{"Type":"1","From":{"X":0,"Y":1},"To":{"X":1,"Y":1}},"Winner":"0"}
{"Board":{"state":[[5,5,5,5,-5,0,0,0,0],[0,-5,6,6,-6,-5,0,0,0],[-5,-6,0,7,-7,-6,-5,0,0],[-5,-6,7,5,-5,-7,-6,-5,0],[-5,-6,-7,-5,0,5,7,6,5],[0,5,6,7,5,-5,7,6,5],[0,0,5,6,7,-7,-7,6,5],[0,0,0,5,6,-6,-6,-6,5],[0,0,0,0,5,-5,-5,-5,-5]]},"AllowedMoves":["0","1","2"]}
{"Player":"-1","Move":{"Type":"1","From":{"X":1,"Y":4},"To":{"X":2,"Y":5}},"Winner":"0"}
{"Player":"1","Move":{"Type":"1","From":{"X":2,"Y":3},"To":{"X":2,"Y":4}},"Winner":"0"}
{"Player":"1","Move":{"Type":"2","From":{"X":2,"Y":4},"To":{"X":2,"Y":1}},"Winner":"0"}
{"Board":{"state":[[5,5,5,5,-5,0,0,0,0],[0,-5,11,6,-6,-5,0,0,0],[-5,-6,0,7,-7,-6,-5,0,0],[-5,-6,0,5,-5,-7,-6,-5,0],[-5,0,0,-5,0,5,7,6,5],[0,5,-6,7,5,-5,7,6,5],[0,0,5,6,7,-7,-7,6,5],[0,0,0,5,6,-6,-6,-6,5],[0,0,0,0,5,-5,-5,-5,-5]]},"AllowedMoves":["1"]}
{"Player":"-1","Move":{"Type":"1","From":{"X":2,"Y":5},"To":{"X":3,"Y":6}},"Winner":"0"}
{"Board":{"state":[[5,5,5,5,-5,0,0,0,0],[0,-5,11,6,-6,-5,0,0,0],[-5,-6,0,7,-7,-6,-5,0,0],[-5,-6,0,5,-5,-7,-6,-5,0],[-5,0,0,-5,0,5,7,6,5],[0,5,0,7,5,-5,7,6,5],[0,0,5,-6,7,-7,-7,6,5],[0,0,0,5,6,-6,-6,-6,5],[0,0,0,0,5,-5,-5,-5,-5]]},"AllowedMoves":["0","1","2"]}
{"Player":"-1","Move":{"Type":"1","From":{"X":3,"Y":6},"To":{"X":4,"Y":7}},"Winner":"0"}
{"Player":"1","Move":{"Type":"1","From":{"X":3,"Y":1},"To":{"X":4,"Y":2}},"Winner":"0"}
{"Player":"1","Move":{"Type":"2","From":{"X":2,"Y":1},"To":{"X":2,"Y":0}},"Winner":"0"}
{"Board":{"state":[[5,5,15,5,-5,0,0,0,0],[0,-5,0,0,-6,-5,0,0,0],[-5,-6,0,7,6,-6,-5,0,0],[-5,-6,0,5,-5,-7,-6,-5,0],[-5,0,0,-5,0,5,7,6,5],[0,5,0,7,5,-5,7,6,5],[0,0,5,0,7,-7,-7,6,5],[0,0,0,5,-6,-6,-6,-6,5],[0,0,0,0,5,-5,-5,-5,-5]]},"AllowedMoves":["1"]}
{"Player":"-1","Move":{"Type":"1","From":{"X":4,"Y":1},"To":{"X":4,"Y":2}},"Winner":"0"}
{"Board":{"state":[[5,5,15,5,-5,0,0,0,0],[0,-5,0,0,0,-5,0,0,0],[-5,-6,0,7,-6,-6,-5,0,0],[-5,-6,0,5,-5,-7,-6,-5,0],[-5,0,0,-5,0,5,7,6,5],[0,5,0,7,5,-5,7,6,5],[0,0,5,0,7,-7,-7,6,5],[0,0,0,5,-6,-6,-6,-6,5],[0,0,0,0,5,-5,-5,-5,-5]]},"AllowedMoves":["0","1","2"]}
{"Player":"-1","Move":{"Type":"1","From":{"X":6,"Y":3},"To":{"X":7,"Y":4}},"Winner":"0"}
{"Player":"1","Move":{"Type":"1","From":{"X":4,"Y":5},"To":{"X":5,"Y":6}},"Winner":"0"}
{"Player":"1","Move":{"Type":"2","From":{"X":7,"Y":5},"To":{"X":7,"Y":6}},"Winner":"0"}
{"Board":{"state":[[5,5,15,5,-5,0,0,0,0],[0,-5,0,0,0,-5,0,0,0],[-5,-6,0,7,-6,-6,-5,0,0],[-5,-6,0,5,-5,-7,0,-5,0],[-5,0,0,-5,0,5,7,-6,5],[0,5,0,7,0,-5,7,0,5],[0,0,5,0,7,5,-7,10,5],[0,0,0,5,-6,-6,-6,-6,5],[0,0,0,0,5,-5,-5,-5,-5]]},"AllowedMoves":["1"]}
{"Player":"-1","Move":{"Type":"1","From":{"X":6,"Y":6},"To":{"X":6,"Y":5}},"Winner":"0"}
{"Board":{"state":[[5,5,15,5,-5,0,0,0,0],[0,-5,0,0,0,-5,0,0,0],[-5,-6,0,7,-6,-6,-5,0,0],[-5,-6,0,5,-5,-7,0,-5,0],[-5,0,0,-5,0,5,7,-6,5],[0,5,0,7,0,-5,-7,0,5],[0,0,5,0,7,5,0,10,5],[0,0,0,5,-6,-6,-6,-6,5],[0,0,0,0,5,-5,-5,-5,-5]]},"AllowedMoves":["0","1","2"]}
{"Player":"-1","Move":{"Type":"2","From":{"X":5,"Y":2},"To":{"X":6,"Y":2}},"Winner":"0"}
{"Player":"1","Move":{"Type":"1","From":{"X":5,"Y":4},"To":{"X":5,"Y":3}},"Winner":"0"}
{"Player":"1","Move":{"Type":"1","From":{"X":6,"Y":4},"To":{"X":6,"Y":5}},"Winner":"1"}
*/
    @Before
    public void setUp() {
        stack = new Stack<>();
        stack.push(new State());

        push(attack(2, 2, 2, 3));
        push(attack(0, 1, 1, 1));
        push(attack(1, 4, 2, 5));
        push(attack(2, 3, 2, 4));
        push(strengthen(2, 4, 2, 1));
        push(attack(2, 5, 3, 6));
        push(attack(3, 6, 4, 7));
        push(attack(3, 1, 4, 2));
        push(strengthen(2, 1, 2, 0));
        push(attack(4, 1, 4, 2));
        push(attack(6, 3, 7, 4));
        push(attack(4, 5, 5, 6));
        push(strengthen(7, 5, 7, 6));
        push(attack(6, 6, 6, 5));
        push(strengthen(5, 2, 6, 2));
        push(attack(5, 4, 5, 3));
        push(attack(6, 4, 6, 5));
    }

    private State state() {
        return stack.peek();
    }

    private Move attack(int fx, int fy, int tx, int ty) {
        final byte from = Move.fromBoardLocation(new BoardLocation(fx, fy));
        final byte to = Move.fromBoardLocation(new BoardLocation(tx, ty));
        return Move.attack(from, to);
    }
    private Move strengthen(int fx, int fy, int tx, int ty) {
        final byte from = Move.fromBoardLocation(new BoardLocation(fx, fy));
        final byte to = Move.fromBoardLocation(new BoardLocation(tx, ty));
        return Move.strengthen(from, to);
    }

    private void push(Move move) {
        stack.push(state().applyMove(move));
    }


}