/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.web.action;

import eionet.meta.ActionBeanUtils;
import eionet.meta.exports.codelist.CodelistExporter;
import eionet.meta.exports.codelist.ExportStatics;
import eionet.meta.exports.codelist.ExportStatics.ExportType;
import eionet.meta.exports.codelist.ExportStatics.ObjectType;
import eionet.web.action.CodelistDownloadActionBean.CodelistFormatToContentTypeConverter;
import eionet.web.action.CodelistDownloadActionBean.CodelistFormatToExportTypeConverter;
import eionet.web.action.CodelistDownloadActionBean.CodelistFormatToFileExtensionConverter;
import eionet.web.action.CodelistDownloadActionBean.CodelistOwnerTypeConverter;
import eionet.web.action.di.ActionBeanDependencyInjectionInterceptor;
import eionet.web.action.di.ActionBeanDependencyInjector;
import eionet.web.action.uiservices.ErrorPageService;
import eionet.web.action.uiservices.impl.ErrorPageServiceImpl;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import org.mockito.Spy;

/**
 *
 * @author Dimitrios Papadimitriou <dp@eworx.gr>
 */
public class CodelistDownloadActionBeanTest {

    private static class DependencyInjector implements ActionBeanDependencyInjector {

        private final CodelistExporter codelistPrinter;
        private final CodelistOwnerTypeConverter codelistOwnerTypeConverter;
        private final CodelistFormatToExportTypeConverter codelistFormatConverter;
        private final CodelistFormatToContentTypeConverter codelistContentTypeConverter;
        private final CodelistFormatToFileExtensionConverter codelistFileExtensionConverter;
        private final ErrorPageService errorPageService;

        public DependencyInjector(CodelistExporter codelistPrinter, CodelistOwnerTypeConverter codelistOwnerTypeConverter,
                CodelistFormatToExportTypeConverter codelistFormatConverter, CodelistFormatToContentTypeConverter codelistContentTypeConverter,
                CodelistFormatToFileExtensionConverter codelistFileExtensionConverter, ErrorPageService errorPageService) {
            this.codelistPrinter = codelistPrinter;
            this.codelistOwnerTypeConverter = codelistOwnerTypeConverter;
            this.codelistFormatConverter = codelistFormatConverter;
            this.codelistContentTypeConverter = codelistContentTypeConverter;
            this.codelistFileExtensionConverter = codelistFileExtensionConverter;
            this.errorPageService = errorPageService;
        }

        @Override
        public boolean accepts(ActionBean bean) {
            return bean instanceof CodelistDownloadActionBean;
        }

        @Override
        public void injectDependencies(ActionBean bean) {
            CodelistDownloadActionBean actionBean = (CodelistDownloadActionBean) bean;
            actionBean.setCodelistPrinter(codelistPrinter);
            actionBean.setOwnerTypeConverter(codelistOwnerTypeConverter);
            actionBean.setCodelistFormatConverter(codelistFormatConverter);
            actionBean.setCodelistContentTypeConverter(codelistContentTypeConverter);
            actionBean.setCodelistFileExtensionConverter(codelistFileExtensionConverter);
            actionBean.setErrorPageService(errorPageService);
        }
    }

    @Mock
    private CodelistExporter codelistPrinter;
    @Spy
    private CodelistOwnerTypeConverter codelistOwnerTypeConverter;
    @Spy
    private CodelistFormatToExportTypeConverter codelistFormatToExportTypeConverter;
    @Spy
    private CodelistFormatToContentTypeConverter codelistContentTypeConverter;
    @Spy
    private CodelistFormatToFileExtensionConverter codelistFormatToFileExtensionConverter;
    @Spy
    private ErrorPageServiceImpl errorPageService;

    @Before
    public void setUp() {
        initMocks(this);
        ActionBeanDependencyInjectionInterceptor.dependencyInjector
                = new DependencyInjector(codelistPrinter, codelistOwnerTypeConverter,
                        codelistFormatToExportTypeConverter, codelistContentTypeConverter,
                        codelistFormatToFileExtensionConverter, errorPageService);
    }

    @After
    public void tearDown() {
        ActionBeanDependencyInjectionInterceptor.dependencyInjector = null;
    }

    @Test
    public void testDownloadCodelistInCSVFormat() throws Exception {

        final String ownerType = "datasets";
        final String ownerId = "8";
        final String format = "csv";
        final byte[] exportReturned = new byte[]{1, 2};
        when(codelistPrinter.exportCodelist(ownerId, ObjectType.DST, ExportType.CSV)).thenReturn(exportReturned);

        MockRoundtrip trip = this.prepareRoundtrip(ownerType, ownerId, format);
        trip.execute();

        verify(codelistOwnerTypeConverter, times(1)).convert(ownerType);
        verify(errorPageService, times(0)).createErrorResolution(ErrorActionBean.ErrorType.NOT_FOUND_404, format);
        verify(codelistFormatToExportTypeConverter, times(1)).convert(format);
        verify(errorPageService, times(0)).createErrorResolution(ErrorActionBean.ErrorType.NOT_FOUND_404, format);
        verify(codelistContentTypeConverter, times(1)).convert(format);
        verify(codelistPrinter, times(1)).exportCodelist(ownerId, ObjectType.DST, ExportType.CSV);

        assertEquals("text/csv; charset=UTF-8", trip.getResponse().getContentType());
    }

    @Test
    public void testDownloadCodelistInXMLFormat() throws Exception {

        final String ownerType = "dataelements";
        final String ownerId = "10";
        final String format = "xml";
        final byte[] exportReturned = new byte[10];
        when(codelistPrinter.exportCodelist(ownerId, ObjectType.ELM, ExportType.XML)).thenReturn(exportReturned);

        MockRoundtrip trip = this.prepareRoundtrip(ownerType, ownerId, format);
        trip.execute();

        verify(codelistOwnerTypeConverter, times(1)).convert(ownerType);
        verify(codelistFormatToExportTypeConverter, times(1)).convert(format);
        verify(codelistContentTypeConverter, times(1)).convert(format);
        verify(codelistPrinter, times(1)).exportCodelist(ownerId, ObjectType.ELM, ExportType.XML);
        verify(errorPageService, times(0)).createErrorResolution(eq(ErrorActionBean.ErrorType.NOT_FOUND_404), any(String.class));

        assertEquals("text/xml; charset=UTF-8", trip.getResponse().getContentType());
    }

    @Test
    public void testFailedToDownloadCodelistBecauseOfUnknownFormat() throws Exception {

        final String ownerType = "datasets";
        final String ownerId = "10";
        final String format = "wrongExportFormat";
        MockRoundtrip trip = this.prepareRoundtrip(ownerType, ownerId, format);
        trip.execute();

        verify(codelistOwnerTypeConverter, times(1)).convert(ownerType);
        verify(codelistFormatToExportTypeConverter, times(1)).convert(format);
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.NOT_FOUND_404), any(String.class));
        verify(codelistPrinter, times(0)).exportCodelist(ownerId, ObjectType.DST, ExportType.UNKNOWN);
    }

    @Test
    public void testFailedToDownloadCodelistBecauseOfUnknownOwnerType() throws Exception {

        final String ownerType = "asdf";
        final String ownerId = "10";
        final String format = "csv";
        MockRoundtrip trip = this.prepareRoundtrip(ownerType, ownerId, format);
        trip.execute();

        verify(codelistOwnerTypeConverter, times(1)).convert(ownerType);
        verify(errorPageService, times(1)).createErrorResolution(eq(ErrorActionBean.ErrorType.NOT_FOUND_404), any(String.class));
        verify(codelistFormatToExportTypeConverter, times(0)).convert(format);
        verify(codelistPrinter, times(0)).exportCodelist(ownerId, null, ExportType.CSV);
    }

    @Test
    public void testTypesOfOwnerConversion() {
        ObjectType expectedType = null;
        ObjectType actualType = null;
        
        expectedType = ExportStatics.ObjectType.DST;
        actualType = codelistOwnerTypeConverter.convert("datasets");
        assertEquals(expectedType, actualType);
        
        expectedType = ExportStatics.ObjectType.TBL;
        actualType = codelistOwnerTypeConverter.convert("tables");
        assertEquals(expectedType, actualType);
        
        expectedType = ExportStatics.ObjectType.ELM;
        actualType = codelistOwnerTypeConverter.convert("dataelements");
        assertEquals(expectedType, actualType);
    }

    @Test
    public void testOwnerTypeIsUknownOrMisspelled() {
        ObjectType expectedType = null;
        ObjectType actualValue;
        
        // Unknown or null owner types
        actualValue = codelistOwnerTypeConverter.convert("someUnknownType");
        assertEquals(expectedType, actualValue);

        actualValue = codelistOwnerTypeConverter.convert(null);
        assertEquals(expectedType, actualValue);

        // Misspelled onwer types
        actualValue = codelistOwnerTypeConverter.convert("dataset");
        assertEquals(expectedType, actualValue);

        actualValue = codelistOwnerTypeConverter.convert("dataelement");
        assertEquals(expectedType, actualValue);

        actualValue = codelistOwnerTypeConverter.convert("table");
        assertEquals(expectedType, actualValue);
    }

    @Test
    public void testCodelistExportedToDifferentTypes() {
        String actualExport = codelistContentTypeConverter.convert("xml");
        assertEquals(codelistContentTypeConverter.convertXml(), actualExport);
        
        actualExport = codelistContentTypeConverter.convert("csv");
        assertEquals(codelistContentTypeConverter.convertCsv(), actualExport);
        
        actualExport = codelistContentTypeConverter.convert("asdf");
        assertEquals(codelistContentTypeConverter.convertUnknown(), actualExport);
    }

    @Test
    public void testCodelistExportedContentIsCSV() {
        when(codelistContentTypeConverter.convertCsv()).thenReturn("csv");
        String actualExport = codelistContentTypeConverter.convert("csv");
        assertEquals(codelistContentTypeConverter.convertCsv(), actualExport);
    }

    @Test
    public void testCodelistExportedContentIsOfUnknownFormatOrNull() {
        when(codelistContentTypeConverter.convertUnknown()).thenReturn(null);
        String actualExport = codelistContentTypeConverter.convert("someUnknownExportType");
        assertEquals(codelistContentTypeConverter.convertUnknown(), actualExport);

        actualExport = codelistContentTypeConverter.convert(null);
        assertEquals(codelistContentTypeConverter.convertUnknown(), actualExport);
    }

    @Test
    public void testExportedCodelistHasXMLFormat() {
        when(codelistFormatToExportTypeConverter.convertXml()).thenReturn(ExportType.XML);
        ExportType actualExportType = codelistFormatToExportTypeConverter.convert("xml");
        assertEquals(codelistFormatToExportTypeConverter.convertXml(), actualExportType);
    }

    @Test
    public void testExportedCodelistHasCSVFormat() {
        when(codelistFormatToExportTypeConverter.convertCsv()).thenReturn(ExportType.CSV);
        ExportType actualExportType = codelistFormatToExportTypeConverter.convert("csv");
        assertEquals(codelistFormatToExportTypeConverter.convertCsv(), actualExportType);
    }

    @Test
    public void testExportedCodelistHasUnknownFormat() {
        when(codelistFormatToExportTypeConverter.convertUnknown()).thenReturn(ExportType.UNKNOWN);
        ExportType actualExportType = codelistFormatToExportTypeConverter.convert("someUnkownFormat");
        assertEquals(codelistFormatToExportTypeConverter.convertUnknown(), actualExportType);
    }

    @Test
    public void testCodelistFileExtensionIsXML() {
        when(codelistFormatToFileExtensionConverter.convertXml()).thenReturn(".xml");
        String actualExtension = codelistFormatToFileExtensionConverter.convert("xml");
        assertEquals(codelistFormatToFileExtensionConverter.convertXml(), actualExtension);
    }

    @Test
    public void testCodelistFileExtensionIsCSV() {
        when(codelistFormatToFileExtensionConverter.convertCsv()).thenReturn(".csv");
        String actualExtension = codelistFormatToFileExtensionConverter.convert("csv");
        assertEquals(codelistFormatToFileExtensionConverter.convertCsv(), actualExtension);
    }

    @Test
    public void testCodelistFileExtensionIsOfUnknownType() {
        when(codelistFormatToFileExtensionConverter.convertUnknown()).thenReturn(null);
        String actualExtension = codelistFormatToFileExtensionConverter.convert("someUnknownType");
        assertEquals(codelistFormatToFileExtensionConverter.convertUnknown(), actualExtension);

        actualExtension = codelistFormatToFileExtensionConverter.convert(null);
        assertEquals(codelistFormatToFileExtensionConverter.convertUnknown(), actualExtension);
    }

    private MockRoundtrip createRoundTrip() {
        MockServletContext ctx = ActionBeanUtils.getServletContext();
        MockRoundtrip trip = new MockRoundtrip(ctx, CodelistDownloadActionBean.class);

        return trip;
    }

    private MockRoundtrip prepareRoundtrip(String ownerType, String ownerId, String format) {
        MockRoundtrip trip = this.createRoundTrip();
        if (ownerType != null) {
            trip.setParameter("ownerType", ownerType);
        }
        if (ownerId != null) {
            trip.setParameter("ownerId", ownerId);
        }
        if (format != null) {
            trip.setParameter("format", format);
        }
        return trip;
    }

    private String composeDownloadPageUrl(String ownerType, String ownerId, String format) {
        return String.format("/codelists/%s/%s/%s", ownerType, ownerId, format);
    }
}
