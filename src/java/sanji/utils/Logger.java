/**
 * @(#)Logger.java, 2012-10-16. 
 * 
 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 */
package sanji.utils;

import toolbox.misc.LogFormatter;

/**
 * std logger
 * 
 * @author leo
 */
public class Logger {

    private static final java.util.logging.Logger logger = LogFormatter.getLogger("sanji");

    public static void info(String msg) {
        logger.info(msg);
    }

    public static void warning(String msg) {
        logger.warning(msg);
    }

    public static void severe(String msg) {
        logger.severe(msg);
    }
}
