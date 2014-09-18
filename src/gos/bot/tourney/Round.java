package gos.bot.tourney;

import com.google.gson.Gson;
import gos.bot.protocol.*;

import java.io.*;

public class Round {

    private static final MoveType[] ONLY_ATTACKS = {MoveType.Attack};
    private static final MoveType[] ALL_TYPES = {MoveType.Attack, MoveType.Pass, MoveType.Strengthen};

    private final String white;
    private final String black;
    private final Gson gson = new Gson();
    private BufferedReader[] ins;
    private PrintStream[] outs;
    private Board board;
    private Process[] processes;

    public Round(String white, String black) {
        this.white = white;
        this.black = black;
        board = new Board();
        processes = new Process[2];
    }

    public Player play() throws IOException {
        try {
            processes[0] = new ProcessBuilder("java", "-jar", white).start();
            processes[1] = new ProcessBuilder("java", "-jar", black).start();
            ins = new BufferedReader[2];
            outs = new PrintStream[2];
            for (int i = 0; i < processes.length; i++) {
                ins[i] = new BufferedReader(new InputStreamReader(processes[i].getInputStream()));
                outs[i] = new PrintStream(processes[i].getOutputStream());
            }

            final InitiateRequest whiteRequest = new InitiateRequest(Player.White);
            final InitiateRequest blackRequest = new InitiateRequest(Player.Black);
            outs[0].println(gson.toJson(whiteRequest));
            outs[0].flush();
            outs[1].println(gson.toJson(blackRequest));
            outs[1].flush();

            firstRound();
            while (currentWinner() == Player.None) {
                nextRound();
            }

            return currentWinner();
        } finally {
            if (processes[0] != null) {
                processes[0].destroy();
            }
            if (processes[1] != null) {
                processes[1].destroy();
            }
        }
    }

    private boolean hasWon(Player player) {
        return board.GetTotalCount(player.opponent(), Stone.A) == 0 ||
                board.GetTotalCount(player.opponent(), Stone.B) == 0 ||
                board.GetTotalCount(player.opponent(), Stone.C) == 0;
    }

    private Player currentWinner() {
        if (hasWon(Player.White)) {
            System.out.println("White has won");
            return Player.White;
        } else if (hasWon(Player.Black)) {
            System.out.println("Black has won");
            return Player.Black;
        } else {
            return Player.None;
        }
    }

    private void allowPlayBy(Player player, MoveType[] types) throws IOException {
        final BufferedReader in = ins[player == Player.White ? 0 : 1];
        final PrintStream out = outs[player == Player.White ? 0 : 1];
        final MoveRequest moveRequest = new MoveRequest(board, types);
        out.println(gson.toJson(moveRequest));
        out.flush();
        final String line = in.readLine();
        final Move move = (Move) gson.fromJson(line, Move.class);
        System.out.println("move " + line);

        applyMove(player, move);

        final ProcessedMove processedMove = new ProcessedMove(player, move, currentWinner());
        final String processedMoveStr = gson.toJson(processedMove);
        outs[0].println(processedMoveStr);
        outs[0].flush();
        outs[1].println(processedMoveStr);
        outs[1].flush();

        board.Dump();
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
}

