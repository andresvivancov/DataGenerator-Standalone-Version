package org.iss4e.datagen.common;



/*
 This file is part of ELKI:
 Environment for Developing KDD-Applications Supported by Index-Structures

 Copyright (C) 2013
 Ludwig-Maximilians-Universität München
 Lehr- und Forschungseinheit für Datenbanksysteme
 ELKI Development Team

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import de.lmu.ifi.dbs.elki.logging.progress.Progress;
import de.lmu.ifi.dbs.elki.logging.progress.ProgressLogRecord;
import de.lmu.ifi.dbs.elki.logging.statistics.*;

import java.util.HashMap;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * This class is a wrapper around {@link java.util.logging.Logger} and
 * {@link java.util.logging.LogManager} offering additional convenience
 * functions.
 * 
 * If a class keeps a static reference to the appropriate {@link Logging}
 * object, performance penalty compared to standard logging should be minimal.
 * 
 * However when using {@link java.util.logging.LogRecord} directly instead of
 * {@link MyLogRecord}, the use of the {@link #log(LogRecord)} method will
 * result in incorrectly logged cause location. Therefore, use
 * {@link MyLogRecord}!
 * 
 * @author Erich Schubert
 * 
 * @apiviz.uses LoggingConfiguration
 * @apiviz.uses ELKILogRecord oneway - - «create»
 * @apiviz.uses Level
 */
public class Logging {


  /**
   * HashMap to keep track of loggers.
   */
  private static HashMap<String, Logging> loggers = new HashMap<String, Logging>();

  /**
   * Wrapped logger of this instance - not static!
   */
  private final Logger logger;

  /**
   * Logging Level class.
   * 
   * @author Erich Schubert
   */
  public static class Level extends java.util.logging.Level {
    /**
     * Additional level for logging: statistics and timing information.
     * 
     * Inbetween of "verbose" and "warning".
     */
    public static final Level STATISTICS = new Level("STATISTICS", (INFO.intValue() + WARNING.intValue()) >> 1);

    /**
     * Alias for the "INFO" logging level: "verbose".
     */
    public static final java.util.logging.Level VERBOSE = INFO;

    /**
     * Additional level for logging: additional verbose messages.
     * 
     * Inbetween of "verbose" and "config", usually 750.
     */
    public static final Level VERYVERBOSE = new Level("VERYVERBOSE", (INFO.intValue() + CONFIG.intValue()) >> 1);

    /**
     * Serial version.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     * 
     * @param name Name
     * @param value Value
     */
    public Level(String name, int value) {
      super(name, value);
    }
  }

  /**
   * Constructor, wrapping a logger.
   * 
   * @param logger Logger to wrap.
   */
  public Logging(final Logger logger) {
    this.logger = logger;
  }

  /**
   * Retrieve logging utility for a particular class.
   * 
   * @param c Class to retrieve logging for
   * @return Logger
   */
  public static Logging getLogger(final Class<?> c) {
    return getLogger(c.getName());
  }

  /**
   * Retrieve logging utility for a particular class.
   * 
   * @param name Class name
   * @return Logger
   */
  public synchronized static Logging getLogger(final String name) {
    Logging logger = loggers.get(name);
    if (logger == null) {
      logger = new Logging(Logger.getLogger(name));
      loggers.put(name, logger);
    }
    return logger;
  }

  /**
   * Verify if logging is enabled at that particular level.
   * 
   * @param lev Logging level
   * @return status
   */
  public boolean isLoggable(Level lev) {
    return logger.isLoggable(lev);
  }

  /**
   * Test whether to log 'statistics'.
   * 
   * @return true if logging statistics
   */
  public boolean isStatistics() {
    return logger.isLoggable(Level.STATISTICS);
  }

  /**
   * Test whether to log 'verbose' aka 'info'.
   * 
   * @return true if verbose
   */
  public boolean isVerbose() {
    return logger.isLoggable(Level.VERBOSE);
  }

  /**
   * Test whether to log 'info' aka 'verbose'.
   * 
   * @return true if verbose
   */
  public boolean isInfo() {
    return logger.isLoggable(Level.INFO);
  }

  /**
   * Test whether to log 'veryverbose'.
   * 
   * @return true if extra verbose
   */
  public boolean isVeryVerbose() {
    return logger.isLoggable(Level.VERYVERBOSE);
  }

  /**
   * Test whether to log 'debug' at 'FINE' level.
   * 
   * This is the same as {@link #isDebuggingFine}
   * 
   * @return true if debug logging enabled
   */
  public boolean isDebugging() {
    return logger.isLoggable(Level.FINE);
  }

  /**
   * Test whether to log 'debug' at 'FINE' level
   * 
   * This is the same as {@link #isDebugging}
   * 
   * @return true if debug logging enabled
   */
  public boolean isDebuggingFine() {
    return logger.isLoggable(Level.FINE);
  }

  /**
   * Test whether to log 'debug' at 'FINER' level
   * 
   * @return true if debug logging enabled
   */
  public boolean isDebuggingFiner() {
    return logger.isLoggable(Level.FINER);
  }

  /**
   * Test whether to log 'debug' at 'FINEST' level
   * 
   * @return true if debug logging enabled
   */
  public boolean isDebuggingFinest() {
    return logger.isLoggable(Level.FINEST);
  }

  /**
   * Log a log message at the given level.
   * 
   * @param level Level to log at.
   * @param message Message to log.
   */
  public void log(java.util.logging.Level level, CharSequence message) {
    LogRecord rec = new MyLogRecord(level, message);
    logger.log(rec);
  }

  /**
   * Log a log message and exception at the given level.
   * 
   * @param level Level to log at.
   * @param message Message to log.
   * @param e Exception
   */
  public void log(java.util.logging.Level level, CharSequence message, Throwable e) {
    LogRecord rec = new MyLogRecord(level, message);
    rec.setThrown(e);
    logger.log(rec);
  }

  /**
   * Log a given log record (should be a {@link MyLogRecord})
   * 
   * @param rec Log record to log.
   */
  public void log(LogRecord rec) {
    logger.log(rec);
  }

  /**
   * Log a message at the 'severe' level.
   * 
   * @param message Warning log message.
   * @param e Exception
   */
  public void error(CharSequence message, Throwable e) {
    log(Level.SEVERE, message, e);
  }

  /**
   * Log a message at the 'severe' level.
   * 
   * @param message Warning log message.
   */
  public void error(CharSequence message) {
    log(Level.SEVERE, message);
  }

  /**
   * Log a message at the 'warning' level.
   * 
   * @param message Warning log message.
   * @param e Exception
   */
  public void warning(CharSequence message, Throwable e) {
    log(Level.WARNING, message, e);
  }

  /**
   * Log a message at the 'warning' level.
   * 
   * @param message Warning log message.
   */
  public void warning(CharSequence message) {
    log(Level.WARNING, message);
  }

  /**
   * Log a message at the 'STATISTICS' level.
   * 
   * You should check isTime() before building the message.
   * 
   * @param message Informational log message.
   * @param e Exception
   */
  public void statistics(CharSequence message, Throwable e) {
    log(Level.STATISTICS, message, e);
  }

  /**
   * Log a message at the 'STATISTICS' level.
   * 
   * You should check isTime() before building the message.
   * 
   * @param message Informational log message.
   */
  public void statistics(CharSequence message) {
    log(Level.STATISTICS, message);
  }

  /**
   * Log a message at the 'info' ('verbose') level.
   * 
   * You should check isVerbose() before building the message.
   * 
   * @param message Informational log message.
   * @param e Exception
   */
  public void verbose(CharSequence message, Throwable e) {
    log(Level.INFO, message, e);
  }

  /**
   * Log a message at the 'info' ('verbose') level.
   * 
   * You should check isVerbose() before building the message.
   * 
   * @param message Informational log message.
   */
  public void verbose(CharSequence message) {
    log(Level.INFO, message);
  }

  /**
   * Log a message at the 'info' ('verbose') level.
   * 
   * You should check isVerbose() before building the message.
   * 
   * @param message Informational log message.
   * @param e Exception
   */
  public void info(CharSequence message, Throwable e) {
    log(Level.INFO, message, e);
  }

  /**
   * Log a message at the 'info' ('verbose') level.
   * 
   * You should check isVerbose() before building the message.
   * 
   * @param message Informational log message.
   */
  public void info(CharSequence message) {
    log(Level.INFO, message);
  }

  /**
   * Log a message at the 'veryverbose' level.
   * 
   * You should check isVeryVerbose() before building the message.
   * 
   * @param message Informational log message.
   * @param e Exception
   */
  public void veryverbose(CharSequence message, Throwable e) {
    log(Level.VERYVERBOSE, message, e);
  }

  /**
   * Log a message at the 'veryverbose' level.
   * 
   * You should check isVeryVerbose() before building the message.
   * 
   * @param message Informational log message.
   */
  public void veryverbose(CharSequence message) {
    log(Level.VERYVERBOSE, message);
  }

  /**
   * Log a message at the 'fine' debugging level.
   * 
   * You should check isDebugging() before building the message.
   * 
   * @param message Informational log message.
   * @param e Exception
   */
  public void debug(CharSequence message, Throwable e) {
    log(Level.FINE, message, e);
  }

  /**
   * Log a message at the 'fine' debugging level.
   * 
   * You should check isDebugging() before building the message.
   * 
   * @param message Informational log message.
   */
  public void debug(CharSequence message) {
    log(Level.FINE, message);
  }

  /**
   * Log a message at the 'fine' debugging level.
   * 
   * You should check isDebugging() before building the message.
   * 
   * @param message Informational log message.
   * @param e Exception
   */
  public void debugFine(CharSequence message, Throwable e) {
    log(Level.FINE, message, e);
  }

  /**
   * Log a message at the 'fine' debugging level.
   * 
   * You should check isDebugging() before building the message.
   * 
   * @param message Informational log message.
   */
  public void debugFine(CharSequence message) {
    log(Level.FINE, message);
  }

  /**
   * Log a message at the 'fine' debugging level.
   * 
   * You should check isDebugging() before building the message.
   * 
   * @param message Informational log message.
   * @param e Exception
   */
  public void fine(CharSequence message, Throwable e) {
    log(Level.FINE, message, e);
  }

  /**
   * Log a message at the 'fine' debugging level.
   * 
   * You should check isDebugging() before building the message.
   * 
   * @param message Informational log message.
   */
  public void fine(CharSequence message) {
    log(Level.FINE, message);
  }

  /**
   * Log a message at the 'finer' debugging level.
   * 
   * You should check isDebugging() before building the message.
   * 
   * @param message Informational log message.
   * @param e Exception
   */
  public void debugFiner(CharSequence message, Throwable e) {
    log(Level.FINER, message, e);
  }

  /**
   * Log a message at the 'finer' debugging level.
   * 
   * You should check isDebugging() before building the message.
   * 
   * @param message Informational log message.
   */
  public void debugFiner(CharSequence message) {
    log(Level.FINER, message);
  }

  /**
   * Log a message at the 'finer' debugging level.
   * 
   * You should check isDebugging() before building the message.
   * 
   * @param message Informational log message.
   * @param e Exception
   */
  public void finer(CharSequence message, Throwable e) {
    log(Level.FINER, message, e);
  }

  /**
   * Log a message at the 'finer' debugging level.
   * 
   * You should check isDebugging() before building the message.
   * 
   * @param message Informational log message.
   */
  public void finer(CharSequence message) {
    log(Level.FINER, message);
  }

  /**
   * Log a message at the 'finest' debugging level.
   * 
   * You should check isDebugging() before building the message.
   * 
   * @param message Informational log message.
   * @param e Exception
   */
  public void debugFinest(CharSequence message, Throwable e) {
    log(Level.FINEST, message, e);
  }

  /**
   * Log a message at the 'finest' debugging level.
   * 
   * You should check isDebugging() before building the message.
   * 
   * @param message Informational log message.
   */
  public void debugFinest(CharSequence message) {
    log(Level.FINEST, message);
  }

  /**
   * Log a message at the 'finest' debugging level.
   * 
   * You should check isDebugging() before building the message.
   * 
   * @param message Informational log message.
   * @param e Exception
   */
  public void finest(CharSequence message, Throwable e) {
    log(Level.FINEST, message, e);
  }

  /**
   * Log a message at the 'finest' debugging level.
   * 
   * You should check isDebugging() before building the message.
   * 
   * @param message Informational log message.
   */
  public void finest(CharSequence message) {
    log(Level.FINEST, message);
  }

  /**
   * Log a message with exception at the 'severe' level.
   * 
   * @param message Error log message.
   * @param e Exception
   */
  public void exception(CharSequence message, Throwable e) {
    log(Level.SEVERE, message, e);
  }

  /**
   * Log an exception at the 'severe' level.
   * 
   * @param e Exception
   */
  public void exception(Throwable e) {
    final String msg = e.getMessage();
    log(Level.SEVERE, msg != null ? msg : "An exception occurred.", e);
  }

  /**
   * Log a Progress object.
   * 
   * @param pgr Progress to log.
   */
  public void progress(Progress pgr) {
    logger.log(new ProgressLogRecord(Level.INFO, pgr));
  }

  /**
   * Generate a new counter.
   * 
   * @param key Key to use
   * @return Counter.
   */
  public Counter newCounter(String key) {
    return new UnsynchronizedLongCounter(key);
  }

  /**
   * Generate a new duration statistic.
   * 
   * @param key Key to use
   * @return Duration statistic.
   */
  public Duration newDuration(String key) {
    return new MillisTimeDuration(key);
  }

  /**
   * Log a statistics object.
   * 
   * @param stats Statistics object to report.
   */
  public void statistics(Statistic stats) {
    log(Level.STATISTICS, stats.getKey() + ": " + stats.formatValue());
  }

  @Override
  public String toString() {
    return "Logging(" + logger.getName() + ", " + logger.getLevel() + ")";
  }
}

