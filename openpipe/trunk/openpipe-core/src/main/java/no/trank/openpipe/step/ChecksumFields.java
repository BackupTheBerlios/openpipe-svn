package no.trank.openpipe.step;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import no.trank.openpipe.api.MultiInputFieldPipelineStep;
import no.trank.openpipe.api.PipelineException;
import no.trank.openpipe.api.PipelineStepStatus;
import no.trank.openpipe.api.document.AnnotatedField;
import no.trank.openpipe.api.document.Document;
import static no.trank.openpipe.util.HexUtil.toHexString;

/**
 * @version $Revision$
 */
public class ChecksumFields extends MultiInputFieldPipelineStep {
   private final static Charset CHARSET = Charset.forName("UTF-8"); 
   private String outField;
   private String algorithm = "MD5";
   private MessageDigest messageDigest;

   public ChecksumFields() {
      super("ChecksumFields");
   }

   @Override
   public PipelineStepStatus execute(Document doc) throws PipelineException {
      messageDigest.reset();
      executeInputFields(doc);
      doc.setFieldValue(outField, toHexString(messageDigest.digest()));
      return PipelineStepStatus.DEFAULT;
   }

   @Override
   protected void process(Document doc, String fieldName, List<AnnotatedField> fieldValues) throws PipelineException {
      for (AnnotatedField fieldValue : fieldValues) {
         messageDigest.update(fieldValue.getValue().getBytes(CHARSET));
      }
   }

   public String getOutField() {
      return outField;
   }

   public void setOutField(String outField) {
      this.outField = outField;
   }

   public String getAlgorithm() {
      return algorithm;
   }

   public void setAlgorithm(String algorithm) {
      this.algorithm = algorithm;
   }

   @Override
   public String getRevision() {
      return "$Revision$";
   }

   @Override
   public void prepare() throws PipelineException {
      super.prepare();
      if (outField == null) {
         throw new PipelineException("No outField configured");
      }
      try {
         messageDigest = MessageDigest.getInstance(algorithm);
      } catch (NoSuchAlgorithmException e) {
         throw new PipelineException(e);
      }
   }

   @Override
   public void finish(boolean success) {
      messageDigest = null;
   }
}