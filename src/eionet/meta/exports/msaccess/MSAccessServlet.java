package eionet.meta.exports.msaccess;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import eionet.meta.DDException;
import eionet.meta.exports.mdb.MdbException;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tieto.com">Jaanus Heinlaid</a>
 *
 */
public class MSAccessServlet extends HttpServlet{

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void service(HttpServletRequest req, HttpServletResponse res)
													throws ServletException, IOException{
	
		String datasetId = req.getParameter("dstID");
		if (datasetId==null || datasetId.trim().length()==0){
			throw new ServletException("Missing request parameter: dstID");
		}
		else{
			datasetId = datasetId.trim();
		}
		
		File generatedFile = null;
		OutputStream output = null;
		FileInputStream input = null;
		try{
			DatasetMSAccessFile msAccessFile = DatasetMSAccessFile.create(datasetId);
			res.setContentType("application/vnd.ms-access");
			
			StringBuffer contentDisp = new StringBuffer("attachment; filename=\"").
			append(msAccessFile.getFileNameForDownload()).append("\"");
			res.setHeader("Content-Disposition", contentDisp.toString());

			generatedFile = msAccessFile.getGeneratedFile();
			if (generatedFile!=null && generatedFile.exists()){

				output = res.getOutputStream();
				input = new FileInputStream(generatedFile);
				IOUtils.copy(input, output);
			}
		}
		catch (DDException e){
			throw new ServletException(e.getMessage(), e);
		}
		catch (SQLException e){
			throw new ServletException(e.getMessage(), e);
		}
		finally{
			IOUtils.closeQuietly(output);
			IOUtils.closeQuietly(input);
			try{
				generatedFile.delete();
			}
			catch (SecurityException e){
				new DDException("Security exception when deleting generated MSAccess file", e).printStackTrace();
			}
		}
	}
}
