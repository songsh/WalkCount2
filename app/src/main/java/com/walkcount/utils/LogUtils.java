package com.walkcount.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

import android.content.Context;
import android.os.Environment;

public class LogUtils {
	
	private static boolean isLog = true;
	public static void file(Context mContext, String value) {
		if (isLog) {
			FileHelper fileHelper = new FileHelper(mContext);
			try {
				File f = fileHelper.createSDFile("log.log");
				FileOutputStream out = new FileOutputStream(f,true);

				value = value + "::" + System.currentTimeMillis() + "\n";
				out.write(value.getBytes("UTF-8"));
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	 public static void file(String s,String FileName){

	        FileName = Environment.getExternalStorageDirectory()+File.separator + FileName;
	        File f = new File(FileName);
	        try {
	            if(!f.exists()){
	                f.createNewFile();
	            }
	                FileWriter writer = new FileWriter(f,true);
	                writer.append(s + "\n");
	                writer.flush();
	                writer.close();

	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }

}
