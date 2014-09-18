package gos.bot.engine;

import gos.bot.protocol.BoardLocation;

final class RayAttacks {

    public static final long[] RAY_N;
    public static final long[] RAY_E;
    public static final long[] RAY_S;
    public static final long[] RAY_SE;
    public static final long[] RAY_W;
    public static final long[] RAY_NW;

    static {
        RAY_N = fillRays(-1, 0);
        RAY_E = fillRays(0, 1);
        RAY_SE = fillRays(1, 1);
        RAY_S = fillRays(1, 0);
        RAY_W = fillRays(0, -1);
        RAY_NW = fillRays(-1, -1);
    }

    private static long[] fillRays(int dx, int dy) {
        final long[] result = new long[Move.N_BOARD_LOCATIONS];
        for (byte from = 0; from < Move.N_BOARD_LOCATIONS; from++) {
            final BoardLocation fromLoc = Move.BOARD_LOCATIONS[from];
            int x = fromLoc.X;
            int y = fromLoc.Y;
            while (true) {
                x += dx;
                y += dy;
                if (BoardLocation.IsLegal(x, y)) {
                    final byte to = Move.fromBoardLocation(new BoardLocation(x, y));
                    result[from] |= (1L << to);
                } else {
                    break;
                }
            }
        }
        return result;
    }

    public static long positions(long occupiedBB, int from) {
        long result = 0;

        /* "Forward" rays */
        {
            final long e = occupiedBB & RAY_E[from];
            final int eb = Long.numberOfTrailingZeros(e);
            result |= (eb < Move.N_BOARD_LOCATIONS) ? (e ^ RAY_E[eb]) : 0L;
        }
        {
            final long se = occupiedBB & RAY_SE[from];
            final int seb = Long.numberOfTrailingZeros(se);
            result |= (seb < Move.N_BOARD_LOCATIONS) ? (se ^ RAY_SE[seb]) : 0L;
        }
        {
            final long s = occupiedBB & RAY_S[from];
            final int sb = Long.numberOfTrailingZeros(s);
            result |= (sb < Move.N_BOARD_LOCATIONS) ? (s ^ RAY_S[sb]) : 0L;
        }

        /* "Backward" rays */
        {
            final long n = occupiedBB & RAY_N[from];
            final int nb = 63 - Long.numberOfLeadingZeros(n);
            result |= (nb >= 0) ? (n ^ RAY_N[nb]) : 0L;
        }
        {
            final long w = occupiedBB & RAY_W[from];
            final int wb = 63 - Long.numberOfLeadingZeros(w);
            result |= (wb >= 0) ? (w ^ RAY_W[wb]) : 0L;
        }
        {
            final long nw = occupiedBB & RAY_NW[from];
            final int nwb = 63 - Long.numberOfLeadingZeros(nw);
            result |= (nwb >= 0) ? (nw ^ RAY_NW[nwb]) : 0L;

        }

        return result;
    }


}
