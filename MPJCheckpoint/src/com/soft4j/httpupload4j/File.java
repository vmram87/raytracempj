// Decompiled by Jad v1.5.7g. Copyright 2000 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/SiliconValley/Bridge/8617/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi 
// Source File Name:   File.java

package com.soft4j.httpupload4j;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;

// Referenced classes of package com.jspsmart.upload:
//            SmartUpload, SmartUploadException

public class File
{

    private SmartUpload m_parent;
    private int m_startData;
    private int m_endData;
    private int m_size;
    private String m_fieldname;
    private String m_filename;
    private String m_fileExt;
    private String m_filePathName;
    private String m_contentType;
    private String m_contentDisp;
    private String m_typeMime;
    private String m_subTypeMime;
    private String m_contentString;
    private boolean m_isMissing;
    public static final int SAVEAS_AUTO = 0;
    public static final int SAVEAS_VIRTUAL = 1;
    public static final int SAVEAS_PHYSICAL = 2;

    File()
    {
        m_startData = 0;
        m_endData = 0;
        m_size = 0;
        m_fieldname = new String();
        m_filename = new String();
        m_fileExt = new String();
        m_filePathName = new String();
        m_contentType = new String();
        m_contentDisp = new String();
        m_typeMime = new String();
        m_subTypeMime = new String();
        m_contentString = new String();
        m_isMissing = true;
    }

    public void fileToField(ResultSet resultset, String s)
        throws SQLException, SmartUploadException, IOException, ServletException
    {
        long l = 0L;
        int i = 0x10000;
        int j = 0;
        int k = 0;
        if(resultset == null)
            throw new IllegalArgumentException("The RecordSet cannot be null (1145).");
        if(s == null)
            throw new IllegalArgumentException("The columnName cannot be null (1150).");
        if(s.length() == 0)
            throw new IllegalArgumentException("The columnName cannot be empty (1155).");
        l = BigInteger.valueOf(m_size).divide(BigInteger.valueOf(i)).longValue();
        j = BigInteger.valueOf(m_size).mod(BigInteger.valueOf(i)).intValue();
        try
        {
            for(int i1 = 1; (long)i1 < l; i1++)
            {
                resultset.updateBinaryStream(s, new ByteArrayInputStream(m_parent.m_binArray, k, i), i);
                k = k == 0 ? 1 : k;
                k = i1 * i;
            }

            if(j > 0)
                resultset.updateBinaryStream(s, new ByteArrayInputStream(m_parent.m_binArray, k, j), j);
        }
        catch(SQLException _ex)
        {
            byte abyte0[] = new byte[m_size];
            System.arraycopy(m_parent.m_binArray, m_startData, abyte0, 0, m_size);
            resultset.updateBytes(s, abyte0);
        }
        catch(Exception _ex)
        {
            throw new SmartUploadException("Unable to save file in the DataBase (1130).");
        }
    }

    public byte getBinaryData(int i)
    {
        if(m_startData + i > m_endData)
            throw new ArrayIndexOutOfBoundsException("Index Out of range (1115).");
        if(m_startData + i <= m_endData)
            return m_parent.m_binArray[m_startData + i];
        else
            return 0;
    }

    public String getContentDisp()
    {
        return m_contentDisp;
    }

    public String getContentString()
    {
        String s = new String(m_parent.m_binArray, m_startData, m_size);
        return s;
    }

    public String getContentType()
    {
        return m_contentType;
    }

    protected int getEndData()
    {
        return m_endData;
    }

    public String getFieldName()
    {
        return m_fieldname;
    }

    public String getFileExt()
    {
        return m_fileExt;
    }

    public String getFileName()
    {
        return m_filename;
    }

    public String getFilePathName()
    {
        return m_filePathName;
    }

    public int getSize()
    {
        return m_size;
    }

    protected int getStartData()
    {
        return m_startData;
    }

    public String getSubTypeMIME()
    {
        return m_subTypeMime;
    }

    public String getTypeMIME()
        throws IOException
    {
        return m_typeMime;
    }

    public boolean isMissing()
    {
        return m_isMissing;
    }

    public void saveAs(String s)
        throws SmartUploadException, IOException
    {
        saveAs(s, 0);
    }

    public void saveAs(String s, int i)
        throws SmartUploadException, IOException
    {
        String s1 = new String();
        s1 = m_parent.getPhysicalPath(s, i);
        if(s1 == null)
            throw new IllegalArgumentException("There is no specified destination file (1140).");
        try
        {
            java.io.File file = new java.io.File(s1);
            file.createNewFile();
            try
            {
                FileOutputStream fileoutputstream = new FileOutputStream(file);
                fileoutputstream.write(m_parent.m_binArray, m_startData, m_size);
                fileoutputstream.close();
            }
            catch(Exception _ex)
            {
                throw new IllegalArgumentException("Path's name is invalid or does not exist (1135).");
            }
        }
        catch(IOException _ex)
        {
            throw new SmartUploadException("File can't be saved (1120).");
        }
    }

    protected void setContentDisp(String s)
    {
        m_contentDisp = s;
    }

    protected void setContentType(String s)
    {
        m_contentType = s;
    }

    protected void setEndData(int i)
    {
        m_endData = i;
    }

    protected void setFieldName(String s)
    {
        m_fieldname = s;
    }

    protected void setFileExt(String s)
    {
        m_fileExt = s;
    }

    protected void setFileName(String s)
    {
        m_filename = s;
    }

    protected void setFilePathName(String s)
    {
        m_filePathName = s;
    }

    protected void setIsMissing(boolean flag)
    {
        m_isMissing = flag;
    }

    protected void setParent(SmartUpload smartupload)
    {
        m_parent = smartupload;
    }

    protected void setSize(int i)
    {
        m_size = i;
    }

    protected void setStartData(int i)
    {
        m_startData = i;
    }

    protected void setSubTypeMIME(String s)
    {
        m_subTypeMime = s;
    }

    protected void setTypeMIME(String s)
    {
        m_typeMime = s;
    }
}
