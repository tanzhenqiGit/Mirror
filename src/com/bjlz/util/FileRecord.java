/**
 * @author tan_zhenq E-mail: tan_zhenqi@163.com
 * @date 创建时间：2015-7-24 下午8:00:09 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 */
package com.bjlz.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * @author lz100
 * used for read screen data to file, just test the screen data can play as video.
 */
public class FileRecord 
{
	public FileRecord(String path)
	{
		mFile = new File(path);
		try {
			mFos = new FileOutputStream(mFile);
		} catch (FileNotFoundException e) {
			SMLog.d("FileRecord");
			e.printStackTrace();
		}
	}
	
	public boolean writeTofile(byte[] buffer)
	{
		if (mFos != null) {
			try {
				mFos.write(buffer);
			} catch (IOException e) {
				SMLog.d("write error");
				e.printStackTrace();
				
			}
		}
		return true;
	}
	
	private FileOutputStream mFos;
	private File mFile;
}
