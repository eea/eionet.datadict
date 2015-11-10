package eionet.meta.exports.codelist;

import eionet.meta.application.errors.ResourceNotFoundException;
import eionet.meta.exports.VocabularyOutputHelper;
import java.nio.charset.Charset;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
@Service
public class CodelistExporterImpl implements CodelistExporter {

    private final CodeValueHandlerProvider codeValueHandlerProvider;

    @Autowired
    public CodelistExporterImpl(CodeValueHandlerProvider codeValueHandlerProvider) {
        this.codeValueHandlerProvider = codeValueHandlerProvider;
    }
    
    @Override
    public byte[] exportCodelist(String ownerId, ExportStatics.ObjectType objectType, ExportStatics.ExportType exportType) 
            throws ResourceNotFoundException {
        Codelist c = new Codelist(exportType, this.codeValueHandlerProvider);
        String stringContent = c.write(ownerId, objectType.toString());
        
        if (StringUtils.isBlank(stringContent)) {
            throw new ResourceNotFoundException(ownerId);
        }
        
        byte[] byteContent = stringContent.getBytes(Charset.forName("UTF-8"));
        
        if (exportType == ExportStatics.ExportType.CSV) {
            return ArrayUtils.addAll(VocabularyOutputHelper.getBomByteArray(), byteContent);
        }
        
        return byteContent;
    }
    
}
