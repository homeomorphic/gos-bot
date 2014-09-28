package gos.bot.engine;

import gos.bot.protocol.BoardLocation;
import gos.bot.protocol.Player;
import gos.bot.protocol.Stone;

final class Evaluator {
    // quite naive
    public static int evaluate(State state) {
        return evaluate(state, Player.White) - evaluate(state, Player.Black);
    }

    private static int evaluate(State state, Player player) {

        final int[][] heightDistribution = new int[4][];
        final int[] maxHeightDistribution = new int[4];
        final int[] stoneDistribution = new int[4];
        for (final Stone stone : Stone.values()) {
            heightDistribution[stone.value] = new int[30];
        }

        long occupied = state.getOccupied(player);
        while (occupied != 0L) {
            final byte pos = (byte)Long.numberOfTrailingZeros(occupied);
            occupied ^= (1L << pos);
            final Stone stone = state.getStoneType(pos);
            final int height = state.getHeight(pos);
            stoneDistribution[stone.value]++;
            heightDistribution[stone.value][height]++;
            maxHeightDistribution[stone.value] = Math.max(maxHeightDistribution[stone.value], height);
        }


        final int scoreA = score(heightDistribution[1], maxHeightDistribution[1]);
        final int scoreB = score(heightDistribution[2], maxHeightDistribution[2]);
        final int scoreC = score(heightDistribution[3], maxHeightDistribution[3]);

        final int minScore = Math.min(scoreA, Math.min(scoreB, scoreC));

        final int score = minScore * 10 + (scoreA + scoreB + scoreC);
        return score;
    }

    private static int score(int[] heightDistribution, int maxHeight) {
        int result = 0;
        for (int i = 0; i <= maxHeight; i++) {
            result += heightDistribution[i] * (i * i);
        }
        return result;
    }
}
