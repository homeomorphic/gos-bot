package gos.bot.tourney;

import com.google.gson.Gson;
import gos.bot.protocol.Player;

public class Tourney {

    private final Gson gson = new Gson();
    private final String bot1, bot2;
    private int bot1wins, bot2wins;

    public Tourney(String bot1, String bot2) {
        this.bot1 = bot1;
        this.bot2 = bot2;
    }

    public void start() throws Exception {
        for (int i = 0; i < 50; i++) {
            Player winner;

            try (Round round = new Round(bot1, bot2)) {
                winner = round.play();
                if (winner == Player.White) {
                    bot1wins++;
                } else {
                    bot2wins++;
                }
            }

            System.out.println("**************************************");
            System.out.println("After " + (2*i+1) + " rounds...");
            System.out.println("# wins for " + bot1 + " \t " + bot1wins);
            System.out.println("# wins for " + bot2 + " \t " + bot2wins);
            System.out.println("**************************************");

            try (Round round = new Round(bot2, bot1)) {
                winner = round.play();
                if (winner == Player.White) {
                    bot2wins++;
                } else {
                    bot1wins++;
                }
            }

            System.out.println("**************************************");
            System.out.println("After " + (2*i+2) + " rounds...");
            System.out.println("# wins for " + bot1 + " \t " + bot1wins);
            System.out.println("# wins for " + bot2 + " \t " + bot2wins);
            System.out.println("**************************************");
        }

    }

    public static void main(String[] args) throws Exception {
        final String bot1 = args[0];
        final String bot2 = args[1];
        new Tourney(bot1, bot2).start();

    }



}
