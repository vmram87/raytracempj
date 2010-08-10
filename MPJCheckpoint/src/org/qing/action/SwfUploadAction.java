package org.qing.action;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.struts2.ServletActionContext;
import org.qing.action.base.BaseActionInterface;

public class SwfUploadAction extends BaseActionInterface{
	/**
     * 文件上传处理
     * @return
     */
    private File[] files; // 多个文件对象数组 写个GET/SET 3个属性
	private String[] filesFileName; // 文件对应的真实文件名 单个文件上传时不需要此属性定义
	private String[] filesContentType;
	
	
    
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
//			保证图片名唯一
			String tName = filesFileName[0];
			System.out.println("文件上传原图片名字-----："+tName);
			String endPhoto = tName.substring(tName.lastIndexOf("."),tName.length()).toLowerCase();
//			处理中文图片名字不显示
			String fn = String.valueOf(System.currentTimeMillis()+filesFileName[0].hashCode());
			System.out.println("中文处理后的图片名字："+fn);
			System.out.println("保存的图片："+fn+" "+endPhoto);
			fileName = fn+endPhoto;
			//文件转移到 upload 下面
			destPathString = ServletActionContext.getRequest().getRealPath("upload")+File.separator+fileName;
			System.out.println("临时文件的保存路径:"+file.getPath()+"\n"+"图片保存路径:"+destPathString);
			FileUtils.copyFile(file, new File(destPathString));
		}
    	try {
			ServletActionContext.getResponse().getWriter().write("successed");
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return SUCCESS;
    }
    
    
}
