package org.qing.action;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.qing.action.base.BaseActionInterface;
import org.qing.object.MyFile;

public class FileDownloadAction extends BaseActionInterface {
	private String    inputPath;
	private String    contentType;
	private MyFile file;
	private String filename;
	    
	public InputStream getInputStream() throws Exception
	{
		if(file != null && file.getIsDirectory() == false)
			return fileMgr.getFileInputStreamById(file.getId());
		else
			return null;
	}
	
	public String execute() throws Exception
	{
		if(file != null  && file.getIsDirectory() == false){
			file = fileMgr.getFileById(file.getId());
			filename=file.getFileName(); 
			contentType=DownloadUtil.getContentType(file.getFileType());
		}
		
		return SUCCESS;
	}


	public MyFile getFile() {
		return file;
	}

	public void setFile(MyFile file) {
		this.file = file;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	
}

class DownloadUtil{
	 private static Map<String, String> map = new HashMap<String,String>();
	 static{
	  map.put("zip", "application/zip");
	  map.put("gif", "image/gif");
	  map.put("jpg", "image/jpeg");
	  map.put("jpeg", "image/jpeg");
	  map.put("js", "application/x-javascript");
	  map.put("swf", "application/x-shockwave-flash");
	  map.put("rmvb", "video/RMVB");
	  map.put("mpeg", "video/mpeg");
	  map.put("txt", "text/plain");
	 }
	 public static String getContentType(String abbreviate){
		 String mine = map.get(abbreviate);
		 if(mine == null)
			 return map.get("txt");
		 else
			 return mine;
	 }
}
