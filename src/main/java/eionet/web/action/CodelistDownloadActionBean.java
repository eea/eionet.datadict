package eionet.web.action;

import eionet.datadict.errors.ResourceNotFoundException;
import eionet.meta.exports.codelist.CodelistExporter;
import eionet.meta.exports.codelist.ExportStatics;
import eionet.meta.exports.codelist.ExportStatics.ExportType;
import eionet.meta.exports.codelist.ExportStatics.ObjectType;
import java.io.ByteArrayInputStream;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Nikolaos Nakas <nn@eworx.gr>
 */
@UrlBinding("/codelists/{ownerType}/{ownerId}/{format}")
public class CodelistDownloadActionBean extends AbstractActionBean {
    
    public static class CodelistOwnerTypeConverter {
        
        public ObjectType convert(String ownerTypeValue) {
            if (StringUtils.equalsIgnoreCase(ownerTypeValue, "datasets")) {
                return ExportStatics.ObjectType.DST;
            }
            
            if (StringUtils.equalsIgnoreCase(ownerTypeValue, "tables")) {
                return ExportStatics.ObjectType.TBL;
            }
            
            if (StringUtils.equalsIgnoreCase(ownerTypeValue, "dataelements")) {
                return ExportStatics.ObjectType.ELM;
            }
            
            return null;
        }
    }
    
    public static abstract class CodelistFormatConverter<T> {
        
        public final T convert(String formatValue) {
            if (StringUtils.equalsIgnoreCase(formatValue, "csv")) {
                return this.convertCsv();
            }
            else if (StringUtils.equalsIgnoreCase(formatValue, "xml")) {
                return this.convertXml();
            }
            
            return this.convertUnknown();
        }
        
        protected abstract T convertCsv();
        
        protected abstract T convertXml();
        
        protected abstract T convertUnknown();
    }
    
    public static class CodelistFormatToExportTypeConverter extends CodelistFormatConverter<ExportType> {

        @Override
        protected ExportType convertCsv() {
            return ExportType.CSV;
        }

        @Override
        protected ExportType convertXml() {
            return ExportType.XML;
        }

        @Override
        protected ExportType convertUnknown() {
            return ExportType.UNKNOWN;
        }
        
    }
    
    public static class CodelistFormatToContentTypeConverter extends CodelistFormatConverter<String> {

        @Override
        protected String convertCsv() {
            return "text/csv; charset=UTF-8";
        }

        @Override
        protected String convertXml() {
            return "text/xml; charset=UTF-8";
        }

        @Override
        protected String convertUnknown() {
            return null;
        }
        
    }
    
    public static class CodelistFormatToFileExtensionConverter extends CodelistFormatConverter<String> {

        @Override
        protected String convertCsv() {
            return ".csv";
        }

        @Override
        protected String convertXml() {
            return ".xml";
        }

        @Override
        protected String convertUnknown() {
            return null;
        }
        
    }
    
    @SpringBean
    private CodelistExporter codelistPrinter;
    
    private CodelistOwnerTypeConverter ownerTypeConverter;
    private CodelistFormatToExportTypeConverter codelistFormatConverter;
    private CodelistFormatToContentTypeConverter codelistContentTypeConverter;
    private CodelistFormatToFileExtensionConverter codelistFileExtensionConverter;
    
    private String ownerType;
    private String ownerId;
    private String format;
    
    public CodelistDownloadActionBean() {
        this.ownerTypeConverter = new CodelistOwnerTypeConverter();
        this.codelistFormatConverter = new CodelistFormatToExportTypeConverter();
        this.codelistContentTypeConverter = new CodelistFormatToContentTypeConverter();
        this.codelistFileExtensionConverter = new CodelistFormatToFileExtensionConverter();
    }
    
    public Resolution download() {
        ObjectType objectType = this.ownerTypeConverter.convert(this.ownerType);
        
        if (objectType == null) {
            return super.createErrorResolution(ErrorActionBean.ErrorType.NOT_FOUND_404, null);
        }
        
        ExportType exportType = this.codelistFormatConverter.convert(this.format);
        
        if (exportType == ExportType.UNKNOWN) {
            return super.createErrorResolution(ErrorActionBean.ErrorType.NOT_FOUND_404, null);
        }
        
        byte[] exportContent;
        
        try {
            exportContent = this.codelistPrinter.exportCodelist(ownerId, objectType, exportType);
        }
        catch (ResourceNotFoundException ex) {
            String msg = String.format("Could not find %s entity with id %s.", this.ownerType, this.ownerId);
            return super.createErrorResolution(ErrorActionBean.ErrorType.NOT_FOUND_404, msg);
        }
        
        return this.createResolution(exportContent);
    }
    
    protected Resolution createResolution(byte[] exportContent) {
        String contentType = this.codelistContentTypeConverter.convert(this.format);
        ByteArrayInputStream contentStream = null;
        StreamingResolution resolution;
        
        try {
            contentStream = new ByteArrayInputStream(exportContent);
            resolution = new StreamingResolution(contentType, contentStream);
        }
        finally {
            IOUtils.closeQuietly(contentStream);
        }
        
        resolution.setFilename(this.composeFileName());
        
        return resolution;
    }
    
    protected String composeFileName() {
        return "codelist_" + this.ownerType + "_" + this.ownerId + this.codelistFileExtensionConverter.convert(format);
    }
    
    public CodelistExporter getCodelistPrinter() {
        return codelistPrinter;
    }

    public void setCodelistPrinter(CodelistExporter codelistPrinter) {
        this.codelistPrinter = codelistPrinter;
    }

    public CodelistOwnerTypeConverter getOwnerTypeConverter() {
        return ownerTypeConverter;
    }

    public void setOwnerTypeConverter(CodelistOwnerTypeConverter ownerTypeConverter) {
        this.ownerTypeConverter = ownerTypeConverter;
    }

    public CodelistFormatToExportTypeConverter getCodelistFormatConverter() {
        return codelistFormatConverter;
    }

    public void setCodelistFormatConverter(CodelistFormatToExportTypeConverter codelistFormatConverter) {
        this.codelistFormatConverter = codelistFormatConverter;
    }

    public CodelistFormatToContentTypeConverter getCodelistContentTypeConverter() {
        return codelistContentTypeConverter;
    }

    public void setCodelistContentTypeConverter(CodelistFormatToContentTypeConverter codelistContentTypeConverter) {
        this.codelistContentTypeConverter = codelistContentTypeConverter;
    }

    public CodelistFormatToFileExtensionConverter getCodelistFileExtensionConverter() {
        return codelistFileExtensionConverter;
    }

    public void setCodelistFileExtensionConverter(CodelistFormatToFileExtensionConverter codelistFileExtensionConverter) {
        this.codelistFileExtensionConverter = codelistFileExtensionConverter;
    }

    public String getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(String ownerType) {
        this.ownerType = ownerType;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
    
}
