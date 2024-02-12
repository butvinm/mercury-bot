package butvinm.mercury.bot.utils;

/**
 * Opinionated StringBuilder wrapper.
 *
 * From https://raw.githubusercontent.com/butvinm/fsb
 */
public class FancyStringBuilder {
    private StringBuilder sb = new StringBuilder();

    private Integer indent = 0;

    /**
     * Append object string repr to content.
     *
     * If previous last char is line break or empty add an indention.
     *
     * @param s - Object to append, interpreted as string.
     * @return this.
     */
    public synchronized FancyStringBuilder a(Object s) {
        i().append(s);
        return this;
    }

    /**
     * Append formatted string to content.
     *
     * If previous last char is line break or empty add an indention.
     *
     * @param s    - String to append.
     * @param args - Formatting args.
     * @return this.
     */
    public synchronized FancyStringBuilder a(String s, Object... args) {
        i().append(s.formatted(args));
        return this;
    }

    /**
     * Append object string repr with line break to content.
     *
     * If previous last char is line break or empty add an indention.
     *
     * @param s - Object to append, interpreted as string.
     * @return this.
     */
    public synchronized FancyStringBuilder l(Object s) {
        return this.a(s).n();
    }

    /**
     * Append formatted string with line break to content.
     *
     * If previous last char is line break or empty add an indention.
     *
     * @param s    - String to append.
     * @param args - Formatting args.
     * @return this.
     */
    public synchronized FancyStringBuilder l(String s, Object... args) {
        return this.a(s, args).n();
    }

    /**
     * Begin block - increase indention.
     *
     * @return this.
     */
    public synchronized FancyStringBuilder bb() {
        indent++;
        return this;
    }

    /**
     * End block - decrease indention.
     *
     * @return this.
     */
    public synchronized FancyStringBuilder eb() {
        if (indent == 0) {
            throw new RuntimeException(
                "Are you dummy dumb dumb? You fucked up indention!"
            );
        }
        indent--;
        return this;
    }

    /**
     * Append space - " ".
     *
     * @return this.
     */
    public synchronized FancyStringBuilder s() {
        i().append(' ');
        return this;
    }

    /**
     * Append line break - "\n".
     *
     * @return this.
     */
    public synchronized FancyStringBuilder n() {
        i().append('\n');
        return this;
    }

    /**
     * Append tabulation - "\t".
     *
     * @return this.
     */
    public synchronized FancyStringBuilder t() {
        i().append('\t');
        return this;
    }

    /**
     * Return current content.
     */
    @Override
    public synchronized String toString() {
        return sb.toString();
    }

    private synchronized StringBuilder i() {
        if (sb.length() == 0 || sb.charAt(sb.length() - 1) == '\n') {
            return sb.append("\t".repeat(indent));
        }
        return sb;
    }
}
