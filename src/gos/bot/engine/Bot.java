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
        final MoveSearcher moveSearcher = new MoveSearcher(currentState);
        final Move chosen = moveSearcher.search();
        //System.err.println("nps = " + moveSearcher.nps() + "; # = " + moveSearcher.nodes());
        return new gos.bot.protocol.Move(chosen.type, chosen.from, chosen.to);
    }

    @Override
    public void handleProcessedMove(ProcessedMove processedMove) {
        final Move moveToApply;
        switch (processedMove.Move.Type) {
            case Attack: moveToApply = Move.Attack(processedMove.Move.From, processedMove.Move.To); break;
            case Strengthen: moveToApply = Move.Strengthen(processedMove.Move.From, processedMove.Move.To); break;
            case Pass: moveToApply = Move.Pass(); break;
            default: throw new IllegalArgumentException("processedMove");
        }
        currentState = currentState.applyMove(moveToApply);
    }
}
