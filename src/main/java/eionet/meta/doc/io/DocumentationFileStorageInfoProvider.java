package eionet.meta.doc.io;

import eionet.doc.io.FileStorageInfoProvider;
import eionet.meta.DDRuntimeException;
import eionet.util.Props;
import java.io.IOException;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

/**
 *
 * @author Nikolaos Nakas
 */
@Component
@DependsOn("contextAware")
public final class DocumentationFileStorageInfoProvider implements FileStorageInfoProvider {
    
    private static final String DOC_FOLDER_PROPERTY_NAME = "doc.files.folder";
    
    private final String fileStoragePath;
    
    public DocumentationFileStorageInfoProvider() throws IOException {
        this.fileStoragePath = StringUtils.trim(Props.getProperty(DOC_FOLDER_PROPERTY_NAME));
        
        if (StringUtils.isBlank(fileStoragePath)) {
            String msg = String.format("Property '%s' not found", DOC_FOLDER_PROPERTY_NAME);
            throw new DDRuntimeException(msg);
        }
    }
    
    @Override
    public String getFileStoragePath() {
        return this.fileStoragePath;
    }
    
}
