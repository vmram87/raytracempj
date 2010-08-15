package org.qing.action;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.struts2.ServletActionContext;
import org.qing.action.base.BaseActionInterface;
import org.qing.object.MyFile;

public class SwfUploadAction extends BaseActionInterface{
	/**
     * 文件上传处理
     * @return
     */
    private File[] files; // 多个文件对象数组 写个GET/SET 3个属性
	private String[] filesFileName; // 文件对应的真实文件名 单个文件上传时不需要此属性定义
	private String[] filesContentType;
	
	private MyFile folder;
	
	public MyFile getFolder() {
		return folder;
	}

	public void setFolder(MyFile folder) {
		this.folder = folder;
	}



	public File[] getFiles() {
		return files;
	}



	public void setFiles(File[] files) {
		this.files = files;
	}



	public String[] getFilesFileName() {
		return filesFileName;
	}



	public void setFilesFileName(String[] filesFileName) {
		this.filesFileName = filesFileName;
	}

	public String[] getFilesContentType() {
		return filesContentType;
	}



	public void setFilesContentType(String[] filesContentType) {
		this.filesContentType = filesContentType;
	}


	@Override
    public String execute() throws Exception
    {
		return SUCCESS;
    }

    public String upload() throws Exception
    {
    	System.out.println("********************swfUpload.action***********************");
    	String destPathString =  "";
		String fileName = "";
    	if(files != null)
		{
			File file = files[0]; // 第一个图片 - one

			String tName = filesFileName[0];
			System.out.println("original-----："+tName);			
			fileMgr.uploadFile(file, tName, folder.getId());
		}
    	try {
			ServletActionContext.getResponse().getWriter().write("successed");
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return SUCCESS;
    }
    
    
}
