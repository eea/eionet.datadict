/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eionet.web.action;

import eionet.meta.exports.VocabularyOutputHelper;
import eionet.meta.exports.codelist.CodeValueHandlerProvider;
import eionet.meta.exports.codelist.Codelist;
import eionet.util.Util;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletResponse;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;

/**
 *
 * @author Dimitrios Papadimitriou <dp@eworx.gr>
 */
@UrlBinding("/download/{type}/{id}/{$event}/{format}")
public class DownloadCodelistActionBean extends AbstractActionBean {

    @SpringBean
    private CodeValueHandlerProvider codeValueHandlerProvider;

    private String type;
    private String id;
    private String format;

    @DefaultHandler
    @HandlesEvent("codelist")
    public Resolution downloadCodelist() {

        return new StreamingResolution("text/plain") {
            String checkType;
            PrintWriter writer = null;
            OutputStreamWriter osw = null;

            @Override
            public void stream(HttpServletResponse response) throws Exception {

                if (Util.isEmpty(id)) {
                    throw new Exception("Missing object id!");
                }
                if (Util.isEmpty(type)) {
                    throw new Exception("Missing object type!");
                }

                if (type.equals("dataset")) {
                    checkType = "DST";
                } else if (type.equals("table")) {
                    checkType = "TBL";
                } else if (type.equals("dataelement")) {
                    checkType = "ELM";
                }

                // prepare output stream and writer
                OutputStream out = response.getOutputStream();
                osw = new OutputStreamWriter(out, "UTF-8");

                //set export type
                Codelist.ExportType exportType = Codelist.ExportType.UNKNOWN;
                String filename = "codelist_" + id + "_" + type;

                // set response content type
                if (format.equals("csv")) {
                    //Issue 29890
                    addBOM(out);
                    exportType = Codelist.ExportType.CSV;
                    response.setContentType("text/csv; charset=UTF-8");
                    response.setHeader("Content-Disposition", "attachment; filename=" + filename + ".csv");
                } else if (format.equals("xml")) {
                    exportType = Codelist.ExportType.XML;
                    response.setContentType("text/xml; charset=UTF-8");
                    response.setHeader("Content-Disposition", "attachment; filename=" + filename + ".xml");
                } else {
                    throw new Exception("Unknown codelist format requested: " + format);
                }

                writer = new PrintWriter(osw);

                // construct codelist writer
                Codelist codelist = new Codelist(exportType, codeValueHandlerProvider);
                // write & flush
                String listStr = codelist.write(id, checkType);

                writer.write(listStr);
                writer.flush();
                osw.flush();
                writer.close();
                osw.close();
            }
        }.setFilename("codelist_" + id + "_" + type);
    }

    /**
     * Writes utf-8 BOM in the given writer.
     *
     * @param out current outputstream
     * @throws IOException if connection fails
     */
    private static void addBOM(OutputStream out) throws IOException {
        byte[] bomByteArray = VocabularyOutputHelper.getBomByteArray();
        for (byte b : bomByteArray) {
            out.write(b);
        }
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the format
     */
    public String getFormat() {
        return format;
    }

    /**
     * @param format the format to set
     */
    public void setFormat(String format) {
        this.format = format;
    }
}
