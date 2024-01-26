package butvinm.mercury.bot.exceptions;

import java.io.File;

public class ShareDirMissedException extends Exception {
    public ShareDirMissedException(File shareDir) {
        super("Share directory does not exist at specified path: %s".formatted(
            shareDir
        ));
    }
}
