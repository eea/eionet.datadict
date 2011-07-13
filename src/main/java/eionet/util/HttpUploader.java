/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 * 
 * The Original Code is Data Dictionary.
 * 
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by TietoEnator Estonia are
 * Copyright (C) 2003 European Environment Agency. All
 * Rights Reserved.
 * 
 * Contributor(s): 
 */
package eionet.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;


public class HttpUploader {
	
	private static final int BUF_SIZE = 1024;
	
	public static void upload(HttpServletRequest req, File file) throws IOException{
		
		RandomAccessFile raFile = new RandomAccessFile(file, "rw");
		String contentType = req.getContentType();
		ServletInputStream in = req.getInputStream();
		if (contentType.toLowerCase().startsWith("multipart/form-data")){
			writeFile(raFile, in, extractBoundary(contentType));
		}
		else{
			writeFile(raFile, in);
		}
	}
	
	public static void upload(InputStream in, File file) throws IOException{
		RandomAccessFile raFile = new RandomAccessFile(file, "rw");
		writeFile(raFile, in);
	}
	
	private static void writeFile(RandomAccessFile raFile, InputStream in) throws IOException{
        
		byte[] buf = new byte[BUF_SIZE];
		int i;
		while ((i=in.read(buf, 0, buf.length)) != -1){
			raFile.write(buf, 0, i);
		}
            
		raFile.close();
		in.close();
	}

	private static void writeFile(RandomAccessFile raFile, ServletInputStream in, String boundary)
																			throws IOException{
		byte[] buf = new byte[BUF_SIZE];
		int i;
        
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		boolean fileStart = false;
		boolean pastContentType = false;
		do{
			int b = in.read();
			if (b == -1) break; // if end of stream, break
            
			bout.write(b);
            
			if (!pastContentType){ // if Content-Type not passed, no check of LNF
				String s = bout.toString();
				if (s.indexOf("Content-Type") != -1)
					pastContentType = true;
			}
			else{
				// Content-Type is passed, after next double LNF is file start
				byte[] bs = bout.toByteArray();
				if (bs != null && bs.length >= 4){
					if (bs[bs.length-1]==10 &&
						bs[bs.length-2]==13 &&
						bs[bs.length-3]==10 &&
						bs[bs.length-4]==13){
                        
						fileStart = true;
					}
				}
			}
		}
		while(!fileStart);
        
		while ((i=in.readLine(buf, 0, buf.length)) != -1){
			String line = new String(buf, 0, i);
			if (boundary != null && line.startsWith(boundary))
				break;
			raFile.write(buf, 0, i);
		}
            
		raFile.close();
		in.close();
	}
	
	private static File initFile(String filePath, HttpServletRequest req) throws IOException{
		return initFile(filePath, req.getRequestedSessionId().replace('-', '_'));
	}

	private static File initFile(String filePath, String fileID) throws IOException{
		
		if (filePath == null) filePath = System.getProperty("user.dir");
		if (!filePath.endsWith(File.separator)) filePath = filePath + File.separator;

		StringBuffer absPath = new StringBuffer(filePath).
		append(fileID).append("_").append(System.currentTimeMillis()).append(".upl");

		return new File(absPath.toString());
	}

	/**
	* Extract the boundary string in multipart request
	*/
	private static String extractBoundary(String contentType){
		int i = contentType.indexOf("boundary=");
		if (i == -1) return null;
		String boundary = contentType.substring(i + 9); // 9 for "boundary="
		return "--" + boundary; // the real boundary is always preceded by an extra "--"
	}

	public static void main(String[] args) {
		
		try{
			URL url = new URL("http://localhost:8080/datadict/public/kala.doc");
			HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();
			File file = new File("d:\\tmp\\Euro 2004 matches.doc");
			InputStream in = url.openStream();
			HttpUploader.upload(in, file);
			System.out.println("DONE!");
		}
		catch (Exception e){
			e.printStackTrace(System.out);
		}
	}
}
