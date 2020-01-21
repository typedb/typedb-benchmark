package grakn.simulation.common;

public class StringPrettyBox {

    public static String simple(String text, char box) {
        int width = text.length() + 4;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < width; ++i) {
            builder.append(box);
        }
        builder.append('\n').append(box).append(' ')
                .append(text)
                .append(' ').append(box).append('\n');
        for (int i = 0; i < width; ++i) {
            builder.append(box);
        }
        return builder.toString();
    }

    private static final String BLOCK_TOP = "\u2580";
    private static final String BLOCK_BOTTOM = "\u2584";
    private static final String BLOCK_FULL = "\u2588";

    public static String blocked(String text) {
        int width = text.length() + 2;
        StringBuilder builder = new StringBuilder();
        builder.append(BLOCK_FULL);
        for (int i = 0; i < width; ++i) {
            builder.append(BLOCK_TOP);
        }
        builder.append(BLOCK_FULL + '\n' + BLOCK_FULL + ' ').append(text).append(' ' + BLOCK_FULL + '\n' + BLOCK_FULL);
        for (int i = 0; i < width; ++i) {
            builder.append(BLOCK_BOTTOM);
        }
        builder.append(BLOCK_FULL);
        return builder.toString();
    }
}
