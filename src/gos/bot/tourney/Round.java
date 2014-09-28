package gos.bot.tourney;

import com.google.gson.Gson;
import gos.bot.protocol.*;

import java.io.*;

public class Round implements AutoCloseable {

    private static final MoveType[] ONLY_ATTACKS = {MoveType.Attack};
    private static final MoveType[] ALL_TYPES = {MoveType.Attack, MoveType.Pass, MoveType.Strengthen};

    private final String white;
    private final String black;
    private final Gson gson = new Gson();
    private final PrintStream moveStream;
    private BufferedReader[] ins;
    private PrintStream[] outs;
    private Board board;
    private Process[] processes;
    private Player winsByForfeit = Player.None;

    public Round(String white, String black, PrintStream moveStream) throws FileNotFoundException {
        this.white = white;
        this.black = black;
        board = new Board();
        processes = new Process[2];
        ins = new BufferedReader[2];
        outs = new PrintStream[2];
        this.moveStream = moveStream;
    }

    private void write(int stream, String line) {
        outs[stream].println(line);
        outs[stream].flush();
    }

    public Player play() throws IOException {
        processes[0] = new ProcessBuilder("java", "-jar", white).start();
        processes[1] = new ProcessBuilder("java", "-jar", black).start();
        for (int i = 0; i < processes.length; i++) {
            ins[i] = new BufferedReader(new InputStreamReader(processes[i].getInputStream()));
            outs[i] = new PrintStream(processes[i].getOutputStream());
        }

        final InitiateRequest whiteRequest = new InitiateRequest(Player.White);
        final InitiateRequest blackRequest = new InitiateRequest(Player.Black);
        write(0, gson.toJson(whiteRequest));
        write(1, gson.toJson(blackRequest));

        firstRound();
        while (currentWinner() == Player.None) {
            nextRound();
        }
        moveStream.println("{ \"Winner\": \"" + (currentWinner() == Player.White ? "1" : "-1") + "\" }");

        return currentWinner();
    }

    private boolean hasWonByLackOfStones(Player player) {
        return board.GetTotalCount(player.opponent(), Stone.A) == 0 ||
                board.GetTotalCount(player.opponent(), Stone.B) == 0 ||
                board.GetTotalCount(player.opponent(), Stone.C) == 0;
    }

    private Player currentWinner() {
        if (winsByForfeit != Player.None) {
            return winsByForfeit;
        }
        if (hasWonByLackOfStones(Player.White)) {
            return Player.White;
        } else if (hasWonByLackOfStones(Player.Black)) {
            return Player.Black;
        } else {
            return Player.None;
        }
    }

    private void allowPlayBy(Player player, MoveType[] types) throws IOException {
        final int streamId = player == Player.White ? 0 : 1;
        final BufferedReader in = ins[streamId];
        final MoveRequest moveRequest = new MoveRequest(board, types);
        write(streamId, gson.toJson(moveRequest));
        moveStream.println(gson.toJson(moveRequest));

        final String line = in.readLine();
        final Move move = (Move) gson.fromJson(line, Move.class);
        if (move == null) {
            /* This is likely because no more moves are possible. */
            winsByForfeit = player.opponent();
        } else {
            applyMove(player, move);

            final ProcessedMove processedMove = new ProcessedMove(player, move, currentWinner());
            final String processedMoveStr = gson.toJson(processedMove);
            write(0, processedMoveStr);
            write(1, processedMoveStr);
        }


    }

    private void applyMove(Player player, Move move) {
        switch (move.Type) {
            case Attack:
                board.SetSpace(move.To, player, board.GetStone(move.From), board.GetHeight(move.From));
                board.ClearSpace(move.From);
                break;
            case Strengthen:
                board.SetSpace(move.To, player, board.GetStone(move.From), board.GetHeight(move.From) +
                    board.GetHeight(move.To));
                board.ClearSpace(move.From);
                break;
        }
    }

    private void firstRound() throws IOException {
        allowPlayBy(Player.White, ONLY_ATTACKS);
    }

    private void nextRound() throws IOException {
        for (int i = 0; i < 2; i++) {
            final Player currentPlayer = i == 0 ? Player.Black : Player.White;
            allowPlayBy(currentPlayer, ONLY_ATTACKS);
            if (currentWinner() != Player.None) {
                return;
            }
            allowPlayBy(currentPlayer, ALL_TYPES);
            if (currentWinner() != Player.None) {
                return;
            }
        }

    }

    @Override
    public void close() throws Exception {
        if (processes[0] != null) {
            processes[0].destroy();
        }
        if (processes[1] != null) {
            processes[1].destroy();
        }
    }
}

