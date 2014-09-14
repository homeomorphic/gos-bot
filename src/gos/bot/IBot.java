package gos.bot;

import gos.bot.protocol.InitiateRequest;
import gos.bot.protocol.Move;
import gos.bot.protocol.MoveRequest;
import gos.bot.protocol.ProcessedMove;

public interface IBot
{
    public void handleInitiate(InitiateRequest request);
    public Move handleMove(MoveRequest request);
    public void handleProcessedMove(ProcessedMove move);
}
