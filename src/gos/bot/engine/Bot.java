package gos.bot.engine;

import gos.bot.IBot;
import gos.bot.protocol.InitiateRequest;
import gos.bot.protocol.MoveRequest;
import gos.bot.protocol.Player;
import gos.bot.protocol.ProcessedMove;

import java.util.Random;

public final class Bot implements IBot {

    private Player me;
    private /* mutable */ State currentState;
    private final MoveSearcher moveSearcher = new MoveSearcher();
    private final Random random = new Random();

    public Bot() {
        currentState = new State();
    }

    @Override
    public void handleInitiate(InitiateRequest request) {
        this.me = request.Color;
    }

    @Override
    public gos.bot.protocol.Move handleMove(MoveRequest request) {
        //currentState.asProtocolBoard().Dump();
        final Move chosen = moveSearcher.search(currentState);
        //System.err.println("nps = " + moveSearcher.nps() + "; # = " + moveSearcher.nodes());
        return chosen.asProtocolMove();
    }

    @Override
    public void handleProcessedMove(ProcessedMove processedMove) {
        final Move moveToApply = Move.of(processedMove.Move);
        currentState = currentState.applyMove(moveToApply);
    }
}
