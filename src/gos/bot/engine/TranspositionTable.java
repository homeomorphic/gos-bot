package gos.bot.engine;

final class TranspositionTable {

    private static final int TABLE_SIZE = 100_000_000;

    public static enum EntryType {
        EXACT, UPPER_BOUND, LOWER_BOUND
    }

    public static final class Entry {
        public final State state;
        public final int depth;
        public final int evaluation;
        public final EntryType type;
        public final Move bestMove;

        public Entry(State state, int depth, int evaluation, EntryType type, Move bestMove) {
            this.state = state;
            this.depth = depth;
            this.evaluation = evaluation;
            this.type = type;
            this.bestMove = bestMove;
        }

        public boolean isUsableForUpperBound(int depth) {
            return this.depth >= depth && (type == EntryType.EXACT || type == EntryType.UPPER_BOUND);
        }

        public boolean isUsableForLowerBound(int depth) {
            return this.depth >= depth && (type == EntryType.EXACT || type == EntryType.LOWER_BOUND);
        }
    }

    private final Entry[] pvTable = new Entry[TABLE_SIZE];
    private final Entry[] table = new Entry[TABLE_SIZE];
    private final Entry[] alwaysReplaceTable = new Entry[TABLE_SIZE];


    private int hash(State state) {
        return (((state.hashCode() % TABLE_SIZE) + TABLE_SIZE) % TABLE_SIZE);
    }

    public Entry lookup(State state) {
        return null;
        /*
        final int hash = hash(state);
        final Entry entry0 = pvTable[hash];
        final Entry entry1 = table[hash];
        final Entry entry2 = alwaysReplaceTable[hash];
        if (entry0 != null && state.equals(entry0.state)) {
            return entry0;
        } else if (entry1 != null && state.equals(entry1.state)) {
            return entry1;
        } else if (entry2 != null && state.equals(entry2.state)) {
            return entry2;
        } else {
            return null;
        }*/
    }


    public void store(Entry entry) {
        return; /*
        final int hash = hash(entry.state);
        final Entry existingEntry = table[hash];

        final boolean pvNode = entry.type == EntryType.EXACT;
        final boolean betterDepth = existingEntry == null || (entry.depth >= existingEntry.depth);
        if (pvNode) {
            pvTable[hash] = entry;
        }

        if (betterDepth) {
            table[hash] = entry;
        } else {
            alwaysReplaceTable[hash] = entry;
        }*/
    }


}
