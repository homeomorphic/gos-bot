package gos.bot.engine;

import gos.bot.protocol.BoardLocation;
import gos.bot.protocol.Player;

final class Evaluator {
    public static float evaluate(State state) {
        return evaluate(state, Player.White) - evaluate(state, Player.Black);
    }

    private static float evaluate(State state, Player player) {
        final int[] stoneDistribution = new int[4];
        final int[] heightDistribution = new int[4];
        for (final BoardLocation loc : State.BOARD_LOCATIONS) {
            if (player == state.getOwner(loc)) {
                stoneDistribution[state.getStoneType(loc).value]++;
                heightDistribution[state.getStoneType(loc).value] =
                        Math.max(heightDistribution[state.getStoneType(loc).value], state.getHeight(loc));
            }
        }
        final double stoneDistributionScore =
                Math.sqrt(stoneDistribution[1]) + Math.sqrt(stoneDistribution[2]) + Math.sqrt(stoneDistribution[3]);
        final double heightDistributionScore =
                heightDistribution[1] + heightDistribution[2] + heightDistribution[3];

        final double score = stoneDistributionScore + heightDistributionScore;

        return (float)score;
    }
}
