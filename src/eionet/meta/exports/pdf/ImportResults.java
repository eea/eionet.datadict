/*
 * Created on Oct 7, 2003
 */
package eionet.meta.exports.pdf;

import java.io.*;
import com.lowagie.text.*;

/**
 * @author jaanus
 *
 */
public class ImportResults extends PdfHandout {
	
	public ImportResults(OutputStream os){
			this.os = os;
		}
	
	public void write(String text) throws Exception {
		addElement(new Phrase(text, Fonts.get(Fonts.CELL_VALUE)));
    setHeader("Import results");
	}
}
