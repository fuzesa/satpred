package edu.utcn.ipprrg.satpred.util;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Centralized logging setup using java.util.logging (no external dependency,
 * Java 8 compatible). Writes a separate log file ("satpred.log") in addition
 * to console output.
 */
public final class LogUtil {

    private static final String LOG_FILE = "satpred.log";
    private static volatile boolean configured = false;

    private LogUtil() {
    }

    /**
     * Configures the application logger once. Safe to call multiple times.
     */
    public static synchronized void init() {
        if (configured) {
            return;
        }
        final Logger root = Logger.getLogger("edu.utcn.ipprrg.satpred");
        root.setUseParentHandlers(false);
        root.setLevel(Level.INFO);

        final Formatter formatter = new Formatter() {
            @Override
            public String format(LogRecord record) {
                return String.format("%1$tFT%1$tT %2$-7s %3$s%n",
                        record.getMillis(), record.getLevel().getName(), formatMessage(record));
            }
        };

        final ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(formatter);
        consoleHandler.setLevel(Level.INFO);
        root.addHandler(consoleHandler);

        try {
            final FileHandler fileHandler = new FileHandler(LOG_FILE, true);
            fileHandler.setFormatter(formatter);
            fileHandler.setLevel(Level.INFO);
            root.addHandler(fileHandler);
        } catch (IOException e) {
            root.warning("Could not open log file '" + LOG_FILE + "': " + e.getMessage());
        }

        configured = true;
    }

    public static Logger getLogger(Class<?> clazz) {
        init();
        return Logger.getLogger(clazz.getName());
    }
}
