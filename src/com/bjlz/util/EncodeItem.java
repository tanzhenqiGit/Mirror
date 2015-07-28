/**
 * @author tan_zhenq E-mail: tan_zhenqi@163.com
 * @date 创建时间：2015-7-25 下午1:53:50 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 */
package com.bjlz.util;

/**
 * @author lz100
 *
 */
public class EncodeItem {
	public EncodeItem(int index, byte[] buffer)
	{
		mIndex = index;
		mBuffer = buffer;
	}
	
	public int getIndex() {return mIndex;}
	public byte[] getBuffer(){ return mBuffer;}
	private int mIndex;
	private byte[] mBuffer = null;
}
