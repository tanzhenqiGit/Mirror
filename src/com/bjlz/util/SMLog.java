/**
 * @author tan_zhenq E-mail: tan_zhenqi@163.com
 * @date 创建时间：2015-7-25 上午11:24:28 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 */
package com.bjlz.util;

import android.util.Log;

/**
 * @author lz100
 * used to control print log
 */
public class SMLog 
{
	
	public static void d(String msg)
	{
		d(TAG, msg);
	}
	
	public static void d(String tag, String info)
	{
		if (DEBUG) {
			Log.d(tag, info);
		}
	}
	
	public static void i(String msg)
	{
		i(TAG, msg);
	}
	public static void i(String tag, String info)
	{
		if (DEBUG) {
			Log.i(tag, info);
		}
	}
	
	public static void e(String msg)
	{
		e(TAG, msg);
	}
	
	public static void e(String tag, String info)
	{
		if (DEBUG) {
			Log.e(tag,info);
		}
	}
	
	private static boolean DEBUG = true;
	private static String TAG = "SM";
	
}
