package com.naotaco.twitterClient01;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import android.widget.Toast;

public class AuthInfoManager {
	
	private static final AuthInfoManager instance = new AuthInfoManager();
	
	private String consumerKey;
	private String consumerSecret;
	private String accessToken;
	private String accessSecret;
	private static final int AUTH_ELEMENT_NUM = 4;
	private String[] infoArray = new String[AUTH_ELEMENT_NUM];
	
	// singleton
	private AuthInfoManager(){
		
		for (int i = 0; i < AUTH_ELEMENT_NUM - 1; i++){
			infoArray[i] = null;
		}
		
		// get auth info from SD
		String SDFile = android.os.Environment.getExternalStorageDirectory()
				.getPath() + "/naotacoStream/auth.dat";
		File file = new File(SDFile);
		try {
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
			BufferedReader br = new BufferedReader(isr);
			String s;
			int i = 0;
			while ((s = br.readLine()) != null) {
				infoArray[i] = s;
				i++;
				if (i > AUTH_ELEMENT_NUM - 1){
					
					break;
				}
			}

			br.close();
			isr.close();
			fis.close();
		} catch (FileNotFoundException e) {
			// Toast.makeText(this, "File not found...", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// Toast.makeText(this, "Unsupported encording", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} catch (IOException e) {
			// Toast.makeText(this, "IO error", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		consumerKey = infoArray[0];
		consumerSecret = infoArray[1];
		accessToken = infoArray[2];
		accessSecret = infoArray[3];
	}
	
	public String[] getAuthInfoArray(){
		
		for (int i = 0; i < AUTH_ELEMENT_NUM - 1; i++){
			if (infoArray[i] == null){
				return null;
			}
		}
		
		return infoArray;
	}
	
	public static AuthInfoManager getInstance (){
		return instance;
	}

}
