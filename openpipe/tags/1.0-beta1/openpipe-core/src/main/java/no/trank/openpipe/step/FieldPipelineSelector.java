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

import no.trank.openpipe.api.document.Document;

/**
 * A {@link no.trank.openpipe.api.PipelineStep} that selects a sub-pipeline based on a field in the document.
 *
 * @version $Revision$
 */
public class FieldPipelineSelector extends PipelineSelector {
   private String operationField = "operation";

   public FieldPipelineSelector() {
      super("FieldPipelineSelector");
   }

   @Override
   protected String getSwitchValue(Document doc) {
      return doc.getFieldValue(operationField);
   }

   public String getRevision() {
      return "$Revision$";
   }

   public String getOperationField() {
      return operationField;
   }

   public void setOperationField(String operationField) {
      this.operationField = operationField;
   }
}
