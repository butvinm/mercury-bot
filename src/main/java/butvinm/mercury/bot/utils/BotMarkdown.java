package butvinm.mercury.bot.utils;

public class BotMarkdown {

    private static final String[] toEscape = {
            "_", "*", "[", "]", "(", ")", "~", "`", ">", "#", "+", "-", "=",
            "|", "{", "}", ".", "!"
    };

    public static String escape(String text) {
        for (var c : toEscape) {
            text = text.replace(c, "\\" + c);
        }
        return text;
    }
}
