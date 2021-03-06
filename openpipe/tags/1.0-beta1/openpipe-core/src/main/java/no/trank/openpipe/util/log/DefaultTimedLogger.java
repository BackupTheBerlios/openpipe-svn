/*
 * Copyright 2007  T-Rank AS
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.trank.openpipe.util.log;

import java.util.Formatter;
import static java.util.concurrent.TimeUnit.*;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A timed logger that logs with a given interval.
 *
 * @version $Revision$
 */
public class DefaultTimedLogger implements TimedLogger {
   private final StringBuilder buf = new StringBuilder(64);
   private final Formatter formatter = new Formatter(buf);
   private Logger log;
   private String format;
   private long count;
   private long start;
   private long tot;
   private long lastLog = System.nanoTime();
   private long logPeriod = SECONDS.toNanos(10);
   private final TimeUnit timeUnit;

   /**
    * Creates a timed logger.
    *
    * @see #setLog(Logger)
    * @see #setFormat(String)
    * @see #setLogPeriodInSeconds(long)
    */
   public DefaultTimedLogger() {
      this(LoggerFactory.getLogger(DefaultTimedLogger.class), "%1$d operations at %2$.2f millis/operation");
   }

   /**
    * Creates a timed logger with the given logger and format.
    *
    * @param log the logger to use.
    * @param format the format to use.
    */
   public DefaultTimedLogger(Logger log, String format) {
      this(log, format, MILLISECONDS);
   }

   /**
    * Creates a timed logger with the given logger and format.
    *
    * @param log the logger to use.
    * @param format the format to use.
    * @param timeUnit the time unit used for averages and times.
    */
   public DefaultTimedLogger(Logger log, String format, TimeUnit timeUnit) {
      this.log = log;
      this.format = format;
      this.timeUnit = timeUnit;
   }

   @Override
   public void startTimer() {
      start = System.nanoTime();
   }

   /**
    * {@inheritDoc}
    * <p>Logs info if time since last log exceeds {@link #getLogPeriodInSeconds()}</p>
    */
   @Override
   public void stopTimerAndIncrement() {
      stopTimerAndIncrement(1);
   }

   @Override
   public void stopTimerAndIncrement(final int byCount) {
      final long now = System.nanoTime();
      tot += now - start;
      count += byCount;
      if (now - lastLog > logPeriod) {
         log();
         lastLog = now;
      }
   }

   @Override
   public void log() {
      if (count > 0 && log.isInfoEnabled()) {
         formatter.format(format, count, calculateAverage(tot, (double) count));
         log.info(buf.toString());
         buf.setLength(0);
      }
   }

   protected double calculateAverage(long totNanos, double count) {
      return timeUnit.convert(totNanos, NANOSECONDS) / count;
   }

   @Override
   public void reset() {
      count = 0;
      tot = 0;
      lastLog = System.nanoTime();
   }

   /**
    * Gets the logger used for logging.
    *
    * @return the logger used for logging.
    */
   public Logger getLog() {
      return log;
   }

   /**
    * Sets the logger used for logging. Default <tt>LoggerFactory.getLogger(DefaultTimedLogger.class)</tt>.
    *
    * @param log the logger used for logging. <b>Cannot</b> be <tt>null</tt>.
    */
   public void setLog(Logger log) {
      this.log = log;
   }

   /**
    * Gets the format of the log statement.
    *
    * @return the format of the log statement.
    *
    * @see #setFormat(String)
    */
   public String getFormat() {
      return format;
   }

   /**
    * Sets the format of the log statement. Default <tt>&quot;%1$d operations at %2$.2f millis/operation&quot;</tt>.
    *
    * @param format the format of the log statement.
    *
    * @see Formatter
    */
   public void setFormat(String format) {
      this.format = format;
   }

   /**
    * Gets the log period in seconds.
    *
    * @return the log period in seconds.
    */
   public long getLogPeriodInSeconds() {
      return NANOSECONDS.toSeconds(logPeriod);
   }

   /**
    * Sets the log period in seconds. Default is <tt>10</tt> seconds.
    *
    * @param logPeriod the log period in seconds.
    */
   public void setLogPeriodInSeconds(long logPeriod) {
      this.logPeriod = SECONDS.toNanos(logPeriod);
   }

   /**
    * Gets the log period in nanos.
    *
    * @return the log period in nanos.
    */
   public long getLogPeriod() {
      return logPeriod;
   }

   /**
    * Sets the log period in nanos.
    *
    * @param logPeriod the log period in nanos.
    */
   public void setLogPeriod(long logPeriod) {
      this.logPeriod = logPeriod;
   }
}
