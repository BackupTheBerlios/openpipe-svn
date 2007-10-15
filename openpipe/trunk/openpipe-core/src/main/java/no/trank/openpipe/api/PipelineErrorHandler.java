package no.trank.openpipe.api;

import no.trank.openpipe.api.document.Document;

/**
 * @version $Revision$
 */
public interface PipelineErrorHandler {
   void handleException(boolean finish, PipelineException ex);

   void handleException(Document document, PipelineException ex);
}
