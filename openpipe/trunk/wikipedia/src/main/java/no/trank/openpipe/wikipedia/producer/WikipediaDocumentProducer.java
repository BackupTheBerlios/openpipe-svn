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
package no.trank.openpipe.wikipedia.producer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.xml.stream.XMLStreamException;

import org.apache.tools.bzip2.CBZip2InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.trank.openpipe.api.document.Document;
import no.trank.openpipe.api.document.DocumentProducer;
import no.trank.openpipe.util.Iterators;
import no.trank.openpipe.wikipedia.WikipediaDumpHandler;

/**
 * Produces documents from a mediawiki dump.
 *
 * @version $Revision$
 */
public class WikipediaDocumentProducer implements DocumentProducer {
   private static final Logger log = LoggerFactory.getLogger(WikipediaDocumentProducer.class);
   private WikipediaDumpHandler dumpHandler;
   private WikiDocumentSplitter documentSplitter;
   private int maxDocs = -1;
   private String contentField = "wikiPage";
   private boolean indexOnlyNew = true;

   @Override
   public void init() {
      if (dumpHandler.isNewDump() || !indexOnlyNew) {
         final File file = dumpHandler.getDumpFile();
         try {
            FileInputStream in = new FileInputStream(file);
            log.debug("Opening wikipedia dump at: {}", file.getAbsolutePath());
            if (isBunzip2(file)) {
               // Have to strip away the two first bytes in the .bz2 file if they are 'BZ'. A bug in CBZip2InputStream?
               documentSplitter = new WikiDocumentSplitter(new BufferedInputStream(new CBZip2InputStream(
                     new BufferedInputStream(new InputStreamPrefixStripper(in, new byte[]{(byte) 'B', (byte) 'Z'})))));
            } else {
               documentSplitter = new WikiDocumentSplitter(new BufferedInputStream(in));
            }
         } catch (XMLStreamException e) {
            throw new RuntimeException("Could not download file", e);
         } catch (IOException e) {
            log.error("Could not read file: " + file.getAbsoluteFile(), e);
         }
      }
   }

   private static boolean isBunzip2(File file) {
      return file.getName().toLowerCase().endsWith(".bz2");
   }

   @Override
   public void close() {
      if (documentSplitter != null) {
         try {
            documentSplitter.close();
         } catch (Exception e) {
            // Do nothing
         }
      }
   }

   @Override
   public void fail() {
      if (documentSplitter != null) {
         try {
            documentSplitter.close();
         } catch (Exception e) {
            // Do nothing
         }
      }
   }

   /**
    * Sets the <tt>WikipediaDumpHandler</tt> that handles the dump-file.
    *
    * @param dumpHandler the handler for the dump-file.
    */
   public void setDumpHandler(WikipediaDumpHandler dumpHandler) {
      this.dumpHandler = dumpHandler;
   }

   /**
    * Gets the maximum number of documents to produce from the dump.
    *
    * @return the maximum number of documents to produce from the dump.
    */
   public int getMaxDocs() {
      return maxDocs;
   }

   /**
    * Sets the maximum number of documents to produce from the dump.
    *
    * Default is -1. All documents in the dump will be produced.
    *
    * @param maxDocs the maximum number of documents to produce from the dump.
    */
   public void setMaxDocs(int maxDocs) {
      this.maxDocs = maxDocs;
   }

   /**
    * Gets the name of the field the document xml will be inserted into.
    *
    * @return the name of the field the document xml will be inserted into.
    */
   public String getContentField() {
      return contentField;
   }

   /**
    * Sets  the name of the field the document xml will be inserted into.
    *
    * @param contentField the name of the field the document xml will be inserted into.
    */
   public void setContentField(String contentField) {
      this.contentField = contentField;
   }

   @Override
   public Iterator<Document> iterator() {
      if (indexOnlyNew && !dumpHandler.isNewDump()) {
         log.info("Current wiki dump is up to date. Skipping produce. (Set indexOnlyNew to false to force indexing.)");
         return Iterators.emptyIterator();
      }
      return new WikiDocumentIterator(maxDocs);
   }

   /**
    * Specifies if the documentProducer should produce documents from an earlier downloaded wikipedia. Set this to false
    * if you want to produce documents from an earlier downloaded wiki. If this is set to true(default) the producer will
    * only produce if there was a new dump available.
    *
    * @return <code>true</code> if the documentProducer should produce documents from an earlier downloaded wikipedia.
    */
   public boolean isIndexOnlyNew() {
      return indexOnlyNew;
   }

   /**
    * Specifies if the documentProducer should produce documents from an earlier downloaded wikipedia. Set this to false
    * if you want to produce documents from an earlier downloaded wiki. If thei is set to true(default) the producer will
    * only produce if there was a new dump available.
    *
    * @param indexOnlyNew <code>true</code> if the documentProducer should produce documents from an earlier downloaded wikipedia.
    */
   public void setIndexOnlyNew(boolean indexOnlyNew) {
      this.indexOnlyNew = indexOnlyNew;
   }

   private class WikiDocumentIterator implements Iterator<Document> {
      private final int maxDocs;
      private int processedDocs = 0;

      private WikiDocumentIterator(int maxDocs) {
         this.maxDocs = maxDocs;
      }

      @Override
      public boolean hasNext() {
         return (maxDocs < 0 || maxDocs > processedDocs) && documentSplitter.hasNext();
      }

      @Override
      public Document next() {
         if (!hasNext()) {
            throw new NoSuchElementException();
         }
         final Document doc = new Document();
         doc.addFieldValue(contentField, documentSplitter.next());
         processedDocs++;
         return doc;
      }

      @Override
      public void remove() {
         throw new UnsupportedOperationException("Remove not supported");
      }
   }
}
