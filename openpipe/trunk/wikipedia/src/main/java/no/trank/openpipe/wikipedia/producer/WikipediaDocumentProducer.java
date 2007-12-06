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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import javax.xml.stream.XMLStreamException;

import org.apache.tools.bzip2.CBZip2InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.trank.openpipe.api.document.Document;
import no.trank.openpipe.api.document.DocumentProducer;

/**
 * Produces documents from a mediawiki dump.
 *
 * @version $Revision$
 */
public class WikipediaDocumentProducer implements DocumentProducer, DownloadProgressListener {
   private static final Logger log = LoggerFactory.getLogger(WikipediaDocumentProducer.class);
   private boolean bunzip2 = true;
   private HttpDownloader httpDownloader;
   private WikiDocumentSplitter documentSplitter;
   private Integer maxDocs;
   private String contentField = "wikiPage";
   private boolean isNew = false;
   private boolean indexOnlyNew = true;

   public void init() {
      log.info("Initializing");
         isNew = downloadWiki();
      try {
         FileInputStream in =  new FileInputStream(httpDownloader.getTargetFile());
         log.debug("Opening wikipedia dump at: " + httpDownloader.getTargetFile().getAbsolutePath());
         if (bunzip2) {
            // Have to strip away the two first bytes in the .bz2 file if they are 'BZ'. A bug in CBZip2InputStream?
            documentSplitter = new WikiDocumentSplitter(new BufferedInputStream(new CBZip2InputStream(new InputStreamPrefixStripper(in, new byte[] {'B', 'Z'}))));
         } else {
            documentSplitter = new WikiDocumentSplitter(new BufferedInputStream(in));
         }
      } catch (XMLStreamException e) {
         throw new RuntimeException("Could not download file", e);
      } catch (IOException e) {
         log.error("Could not read file: " + httpDownloader.getTargetFile().getAbsoluteFile(), e);
      }

   }

   private boolean downloadWiki() {
      try {
         if (!httpDownloader.isLastVersion()) {
            try {
               log.info("Downloading " + httpDownloader.getSourceUrl());
               httpDownloader.addProgressListener(this);
               final int status = httpDownloader.downloadFile();
               if (status < 200 || status >= 300) {
                  throw new RuntimeException("Could not download file: http returned status: " + status);
               }
               return true;
            } catch (IOException e) {
               throw new RuntimeException("Could not download file", e);
            }
         } else {
            log.info("Found local file with correct md5. Skipping download.");
         }
      } catch (IOException e) {
         log.error("Could not determine last version.", e);
      }
      return false;
   }

   public void close() {
      if (documentSplitter != null) {
         try {
            documentSplitter.close();
         } catch (Exception e) {
            // Do nothing
         }
      }
   }

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
    * Gets the HttpDownloader for this producer.
    *
    * @return the downloader that is used to get the wikipedia dump.
    */
   public HttpDownloader getHttpDownloader() {
      return httpDownloader;
   }

   /**
    * Sets the HttpDownloader for this producer.
    *
    * @param httpDownloader the downloader that is used to get the wikipedia dump.
    */
   public void setHttpDownloader(HttpDownloader httpDownloader) {
      this.httpDownloader = httpDownloader;
   }

   /**
    * Gets the maximum number of documents to produce from the dump.
    *
    * @return the maximum number of documents to produce from the dump.
    */
   public Integer getMaxDocs() {
      return maxDocs;
   }

   /**
    * Sets the maximum number of documents to produce from the dump.
    *
    * Default is null. All documents in the dump will be produced.
    *
    * @param maxDocs the maximum number of documents to produce from the dump.
    */
   public void setMaxDocs(Integer maxDocs) {
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

   public Iterator<Document> iterator() {
      if (indexOnlyNew && !isNew) {
         log.info("Current wiki dump is up to date. Producing 0 documents. (Set indexOnlyNew to false to force indexing.)");
         return new WikiDocumentIterator(0);
      } else {
         return new WikiDocumentIterator(maxDocs);
      }
   }

   /**
    * Gets if the downloaded file is expected to be a bzip2 file. Default is true.
    *
    * @return true if the downloaded file is expected to be a bzip2 file
    */
   public boolean isBunzip2() {
      return bunzip2;
   }

   /**
    * Sets if the downloaded file is expected to be a bzip2 file.
    *
    * If this is true this producer will run the input through a unpacker befor splitting the file into documents.
    *
    * @param bunzip2 true if the downloaded file is expected to be a bzip2 file.
    */
   public void setBunzip2(boolean bunzip2) {
      this.bunzip2 = bunzip2;
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

   public void progress(long totalUnits, long doneUnits) {
      log.info("Download progress: " + doneUnits + " of " + totalUnits + " ("  + Math.round(((float)doneUnits / totalUnits) * 100) + "%)" );
   }

   private class WikiDocumentIterator implements Iterator<Document> {
      private int processedDocs = 0;
      private final Integer maxDocs;

      private WikiDocumentIterator(Integer maxDocs) {
         this.maxDocs = maxDocs;
      }

      public boolean hasNext() {
         return (maxDocs == null || maxDocs > processedDocs) && documentSplitter.hasNext();
      }

      public Document next() {
         String content = documentSplitter.next();
         if (content != null) {
            Document doc = new Document();
            doc.addFieldValue(contentField, content);
            processedDocs++;
            return doc;
         } else {
            return null;
         }
      }

      public void remove() {
         throw new UnsupportedOperationException("Remove not supported");
      }
   }
}
