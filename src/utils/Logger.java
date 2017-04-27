package utils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Print formatted logs. Error messages will be printed in full_log.txt and
 * error_log.txt.
 */
public class Logger {

    private static boolean verbose = false;
    private static boolean debug = false;
    private String className;
    private final String LOG_FILE = "full_log.txt";
    private final String ERROR_FILE = "error_log.txt";
    private FileOutputStream full_log;
    private FileOutputStream error_log;

    public Logger(String className) {
        this.className = className;
        File full = new File(LOG_FILE);
        File error = new File(ERROR_FILE);
        try {
            full.createNewFile();
            error.createNewFile();
            full_log = new FileOutputStream(full, true);
            error_log = new FileOutputStream(error, true);
        } catch (IOException e) {
        }

    }

    /**
     * Configure the verbose mode.
     * 
     * @param verbose
     * @param debug
     */
    public static void configure(boolean verbose, boolean debug) {
        Logger.verbose = verbose;
        Logger.debug = debug;
    }

    /**
     * [TRACE] trivial things
     * 
     * @param pattern
     * @param components
     */
    public void trace(String pattern, String... components) {
        String log = getTimestamp() + getThread() + getClassName() + "[TRACE] " + getMessage(pattern, components);
        try {
            full_log.write((log + "\n").getBytes());
        } catch (IOException e) {
        }
        if (verbose)
            System.out.println(log);
    }

    /**
     * [WARN] for important things
     * 
     * @param pattern
     * @param components
     */
    public void warn(String pattern, String... components) {
        String log = getTimestamp() + getThread() + getClassName() + "[WARN] " + getMessage(pattern, components);
        try {
            full_log.write((log + "\n").getBytes());
        } catch (IOException e) {
        }
        System.out.println(log);
    }

    /**
     * [DEBUG] for critical things
     * 
     * @param pattern
     * @param components
     */
    public void debug(String pattern, String... components) {
        String log = getTimestamp() + getThread() + getClassName() + "[DEBUG] " + getMessage(pattern, components);
        try {
            full_log.write((log + "\n").getBytes());
        } catch (IOException e) {
        }
        if (debug)
            System.out.println(log);
    }

    /**
     * [ERROR] for errors
     * 
     * @param pattern
     * @param components
     */
    public void error(Exception e, String pattern, String... components) {
        String log = getTimestamp() + getThread() + getClassName() + "[ERROR] " + getMessage(pattern, components);
        try {
            full_log.write((log + "\n").getBytes());
            error_log.write((log + "\n").getBytes());
            if (e != null) {
                PrintWriter writer = new PrintWriter(error_log);
                e.printStackTrace(writer);
                writer.flush();
            }
        } catch (IOException e1) {
        }
        System.err.println(log);
    }

    private String getMessage(String pattern, String... components) {
        for (int i = 0; i < components.length; i++) {
            if (components[i] == null) {
                components[i] = "";
            }
            pattern = pattern.replaceFirst(Pattern.quote("{}"), components[i]);
        }
        return pattern;
    }

    private String getTimestamp() {
        Date now = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("[yyyy.MM.dd hh:mm:ss]");
        return ft.format(now);
    }

    private String getClassName() {
        return "[" + className + "]";
    }

    private String getThread() {
        return "[" + Thread.currentThread().getName() + "]";
    }
}
