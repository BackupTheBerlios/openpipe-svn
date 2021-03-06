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
package no.trank.openpipe.api.document;

import junit.framework.TestCase;

/**
 * @version $Revision$
 */
public class DocumentTest extends TestCase {

   public void testCRUDField() throws Exception {
      final Document doc = new Document();
      doc.setFieldValue("field", null);
      assertFalse(doc.containsField("field"));
      assertFalse(doc.removeField("field"));
      doc.setFieldValue("field", "value");
      assertTrue(doc.removeField("field"));
      assertFalse(doc.removeField("field"));
   }
}