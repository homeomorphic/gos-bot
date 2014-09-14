package gos.bot;

import gos.bot.engine.Bot;

public class Main
{

    public static void main(String[] args)
    {
        Bot bot = new Bot();
        try (Engine engine = new Engine(bot)) {
            engine.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
