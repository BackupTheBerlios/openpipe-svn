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
package no.trank.openpipe.step;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import no.trank.openpipe.api.BasePipelineStep;
import no.trank.openpipe.api.PipelineException;
import no.trank.openpipe.api.PipelineStepStatus;
import no.trank.openpipe.api.document.Document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This step converts date formats using {@link SimpleDateFormat}.
 * 
 * @version $Revision$
 */
public class ConvertDate extends BasePipelineStep {
   private static Logger log = LoggerFactory.getLogger(ConvertDate.class);
   private Map<String, String> fieldNameMap;
   private LinkedHashMap<String, String> patternMap;
   private List<FormatPair> formats;
   private boolean failOnError;
   private boolean blankError;

   public ConvertDate() {
      super("ConvertDate");
   }

   @Override
   public PipelineStepStatus execute(Document doc) throws PipelineException {
      if (fieldNameMap != null) {
         for (Map.Entry<String, String> pair : fieldNameMap.entrySet()) {
            process(doc, pair.getKey(), pair.getValue());
         }
      }

      return PipelineStepStatus.DEFAULT;
   }

   @Override
   public void prepare() throws PipelineException {
      formats = new ArrayList<FormatPair>(patternMap.size());
      try {
         for (Entry<String, String> e : patternMap.entrySet()) {
            formats.add(new FormatPair(e.getKey(), e.getValue()));
         }
      } catch (RuntimeException e) {
         throw new PipelineException(e);
      }
   }

   @Override
   public void finish(boolean success) throws PipelineException {
      formats = null;
   }

   private void process(Document doc, String input, String output) throws PipelineException {
      String old = doc.getFieldValue(input);
      if (old != null && old.length() > 0) {

         for (FormatPair format : formats) {
            try {
               final SimpleDateFormat from = format.getFrom();
               final SimpleDateFormat to = format.getTo();

               doc.setFieldValue(output, to.format(from.parse(old)));

               if (log.isDebugEnabled()) {
                  log.debug("Parsed field '" + input + "' with pattern '" + from.toPattern() +
                        ". Wrote field '" + output + "' with pattern '" + to.toPattern());
               }

               return;
            } catch (ParseException e) {
               // Doing nothing
            }
         }

         if (failOnError) {
            throw new PipelineException("Could not parse date " + old);
         } else {
            log.debug("Was not able to parse field '{}'", input);
         }
      } else if (blankError) {
         throw new PipelineException("Field '" + input + "' is " + (old == null ? "null" : "''"));
      }
   }

   @Override
   public String getRevision() {
      return "$Revision$";
   }

   /**
    * Returns the names of the input/output field pairs.
    * 
    * @return the name map
    */
   public Map<String, String> getFieldNameMap() {
      return fieldNameMap;
   }

   /**
    * Sets the names of the input/output field pairs.
    * 
    * @param fieldNameMap
    */
   public void setFieldNameMap(Map<String, String> fieldNameMap) {
      this.fieldNameMap = fieldNameMap;
   }

   /**
    * Sets the ordered map of from/to date format pairs. When applied to the input, consecutive pairs act as a fallback
    * should the previous one generate an error. The step only errors if the last pair errors.
    * 
    * @param patternMap an ordered map containing the from/to format pairs
    */
   public void setPatternMap(LinkedHashMap<String, String> patternMap) {
      this.patternMap = patternMap;
   }

   /**
    * Returns whether an exception will be thrown if an error occurs.
    * 
    * @return true if an exception will be thrown, false otherwise
    */
   public boolean isFailOnError() {
      return failOnError;
   }

   /**
    * Sets whether an exception will be thrown if an error occurs.
    * 
    * @param failOnError
    */
   public void setFailOnError(boolean failOnError) {
      this.failOnError = failOnError;
   }
   

   /**
    * Returns whether a blank input field will be treated as an error.
    * 
    * @return true if a blank input field will be treated as an error, false otherwise
    */
   public boolean isBlankError() {
      return blankError;
   }

   /**
    * Sets whether a blank input field will be treated as an error.
    * 
    * @param blankError
    */
   public void setBlankError(boolean blankError) {
      this.blankError = blankError;
   }

   private static final class FormatPair {
      private final SimpleDateFormat from;
      private final SimpleDateFormat to;

      public FormatPair(String fromPattern, String toPattern) {
         from = new SimpleDateFormat(fromPattern);
         to = new SimpleDateFormat(toPattern);
      }

      public SimpleDateFormat getFrom() {
         return from;
      }

      public SimpleDateFormat getTo() {
         return to;
      }
   }
}