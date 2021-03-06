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

/**
 * A simple interface for measuring and logging timings of operations.
 *
 * @version $Revision$
 */
public interface TimedLogger {

   /**
    * Starts the timer.
    */
   void startTimer();

   /**
    * Stops the timer and increments the operation count. Should measure the time elapsed since {@link #startTimer()}
    * was called.
    */
   void stopTimerAndIncrement();

   /**
    * Stops the timer and increments the operation count. Should measure the time elapsed since {@link #startTimer()}
    * was called.
    *
    * @param byCount the count to increment operation count by.
    */
   void stopTimerAndIncrement(int byCount);

   /**
    * Logs current averages and operattion count.
    */
   void log();

   /**
    * Resets the timer.
    */
   void reset();
}
