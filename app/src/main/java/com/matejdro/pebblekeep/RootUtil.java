package com.matejdro.pebblekeep;

import android.content.Context;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class RootUtil {
	
	public static boolean isRooted()
	{
		try {
			Process proces = Runtime.getRuntime().exec("su");

			DataOutputStream out = new DataOutputStream(proces.getOutputStream());
			BufferedReader in = new BufferedReader(new InputStreamReader(proces.getInputStream()));
			out.writeBytes("id\n");
			out.flush();

			String line = in.readLine();
			if (line == null) return false;

			return true;
		} catch (IOException e) {
			return false;
		}

	}
	
	public static File copy(Context context)
	{
		try {
			File resultFile = new File(context.getCacheDir(), "keep.db");
			String cmdLine = "cat /data/data/com.google.android.keep/databases/keep.db > " + resultFile.getAbsolutePath();
			
			String[] args = new String[] {"su", "-c", cmdLine };
			Process proces = Runtime.getRuntime().exec(args);

			proces.waitFor();
			
			cmdLine = "chmod 777 " + resultFile.getAbsolutePath();
			args = new String[] {"su", "-c", cmdLine };
			proces = Runtime.getRuntime().exec(args);
			proces.waitFor();
			
			return resultFile;
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return null;
	}
}
