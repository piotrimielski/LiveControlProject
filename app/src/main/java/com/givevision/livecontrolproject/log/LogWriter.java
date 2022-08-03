package com.givevision.livecontrolproject.log;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.givevision.livecontrolproject.util.Constants;
import com.givevision.livecontrolproject.util.TextResourceReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LogWriter {
	private static final String TAG = "LogWriter";

	private static final String COMMA_DELIMITER = ",";
	private static final String NEW_LINE_SEPARATOR = "\n";
	private static final String FILE_HEADER = "datetime,action,msg";
	private File myStorageDir;
	private SharedPreferences systemSharedData;
	private Context _context;
	private String sep;
	private SimpleDateFormat sdf;
	public LogWriter(Context context) {
		this._context = context;
		this.systemSharedData = this._context.getSharedPreferences(Constants.pref_key_name, 0);
		this.sdf= new SimpleDateFormat(Constants.formats[3], Locale.UK);
	}

	public void init() {
		sep = File.separator;
		String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
//		Log.v(TAG, "LogWriter :: extStorageDirectory= "+extStorageDirectory);
		if (Constants.file_path.equals("")) {
			this.myStorageDir = new File(extStorageDirectory + sep + Constants.folder_name);
		} else {
			this.myStorageDir = new File(Constants.file_path);
		}

		if (!this.myStorageDir.isDirectory() || this.myStorageDir.exists()) {
			this.myStorageDir.mkdir();
		}

		SharedPreferences.Editor editor = this.systemSharedData.edit();
		editor.putString(Constants.pref_key_file_path, this.myStorageDir.getPath());
		editor.commit();
//		Log.v(TAG, "LogWriter :: myStorageDir.getPath()= "+this.myStorageDir.getPath());
	}

	public String writeLog(Pojo data) {
		String filePath = myStorageDir.getPath();
		if (data.getType().contentEquals("error")) {
			filePath=filePath+sep+ "errorSightSport.csv";
		} else if (data.getType().contentEquals("user")) {
			filePath=filePath+sep+ "userSightSport.csv";
		} else {
			return null;
		}
		this.writeCsvFile(data, filePath);
		return filePath;
	}

	private void writeCsvFile(Pojo data, String filePath) {
		File file = new File(filePath);
		if (!file.isFile()) {
			try {
				file.createNewFile();
			} catch (IOException var9) {
				var9.printStackTrace();
			}
		}

		try {
			FileOutputStream fOut = new FileOutputStream(file, true);
			OutputStreamWriter myWriter = new OutputStreamWriter(fOut);
			String timeStamp = sdf.format(new Date());
			myWriter.append(timeStamp);
			myWriter.append(",");
			myWriter.append(data.getAction());
			myWriter.append(",");
			myWriter.append(data.getMessage());
			myWriter.append("\n");
			myWriter.flush();
			fOut.close();
		} catch (FileNotFoundException var7) {
			var7.printStackTrace();
		} catch (IOException var8) {
			var8.printStackTrace();
		}

	}

	public String saveImage(Bitmap finalBitmap) {
		String filePath = this.systemSharedData.getString(Constants.file_path, "");

		String timeStamp = sdf.format(new Date());
		String fname = "Image_" + timeStamp + ".jpg";
		File file = new File(filePath, fname);
		if (file.exists()) {
			file.delete();
		}

		try {
			FileOutputStream out = new FileOutputStream(file);
			finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
			out.flush();
			out.close();
		} catch (Exception var7) {
			var7.printStackTrace();
		}

		return filePath + File.separator + fname;
	}

	public void updatePath(String name) {
		Constants.file_path = name;
	}

	@SuppressLint("WrongConstant")
	public ArrayList<String> readFileFromSDCard(String path, String name) {
		File file = new File(path, name);
		ArrayList<String> results = new ArrayList();
		if (file.exists()) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(file));

				String line;
				while((line = br.readLine()) != null) {
					results.add(line);
				}
			} catch (IOException var7) {
				var7.printStackTrace();
			}
		} else {
			Toast.makeText(this._context, "Sorry system file doesn't exist!!", 0).show();
		}

		return results;
	}

	public void extractLogToFile() {
//		Log.v(TAG, "LogWriter :: extractLogToFile");
		String filePath = this.systemSharedData.getString(Constants.pref_key_file_path, "");
		String currentDatetime = sdf.format(new Date());
		filePath = filePath + File.separator + "exceptionLog.txt";
		PackageManager manager = this._context.getPackageManager();
		PackageInfo info = null;

		try {
			info = manager.getPackageInfo(this._context.getPackageName(), 0);
		} catch (NameNotFoundException var14) {
			var14.printStackTrace();
		}

		String model = Build.MODEL;
		if (!model.startsWith(Build.MANUFACTURER)) {
			model = Build.MANUFACTURER + " " + model;
		}

		File file = new File(filePath);
//        Log.d(TAG, "LogWriter :: file= "+file.getPath());
		InputStreamReader reader = null;
		FileWriter writer = null;


		try {

			String cmd = Build.VERSION.SDK_INT <= 15 ? "logcat -d -v time MyApp:v dalvikvm:v System.err:v *:s" : "logcat -d -v time";
//			Log.d("debug", "LogWriter :: cmd= "+cmd);
			Process process = Runtime.getRuntime().exec(cmd);
			String error = convertStreamToString(process.getInputStream());
//            Log.d(TAG, "LogWriter :: error= "+error.length());
			SharedPreferences.Editor editor = this.systemSharedData.edit();
			List<String> list = new TextResourceReader().readLineInFile(file.getPath());
			if(list.size()>0 && list.get(0).contains("saved at")){
				new TextResourceReader().writeLineInFile(file.getPath(), "\n", false);
				editor.putInt(Constants.PREF_CLN_KEY, 0);
				editor.commit();
			}else{
				if(this.systemSharedData.getInt(Constants.PREF_CLN_KEY,0)<3){
					new TextResourceReader().writeLineInFile(file.getPath(), "\n", true);
					editor.putInt(Constants.PREF_CLN_KEY, this.systemSharedData.getInt(Constants.PREF_CLN_KEY,0)+1);
					editor.commit();
				}else{
					new TextResourceReader().writeLineInFile(file.getPath(), "\n", false);
					editor.putInt(Constants.PREF_CLN_KEY, 0);
					editor.commit();
				}
			}


            new TextResourceReader().writeLineInFile(file.getPath(), "DateTime Log: " + sdf.format(new Date()) + "\n", true);
            new TextResourceReader().writeLineInFile(file.getPath(), "Android version: " + Build.VERSION.SDK_INT + "\n", true);
            new TextResourceReader().writeLineInFile(file.getPath(), "Device: " + model + "\n", true);
            new TextResourceReader().writeLineInFile(file.getPath(), "App version: " + (info == null ? "(null)" : info.versionCode) + "\n", true);
            new TextResourceReader().writeLineInFile(file.getPath(), error, true);

//			writer = new FileWriter(file, true);
//			writer.append("\n");
//			writer.append("DateTime Log: " + sdf.format(new Date()) + "\n");
//			writer.append("Android version: " + Build.VERSION.SDK_INT + "\n");
//			writer.append("Device: " + model + "\n");
//			writer.append("App version: " + (info == null ? "(null)" : info.versionCode) + "\n");
////			writer.append(error);
//			writer.flush();
////			reader.close();
//			writer.close();
			cmd = "su -c chmod 664 "+this.systemSharedData.getString(Constants.pref_key_file_path, "")+sep+"*.*";
//			Log.d(TAG, "LogWriter :: cmd= "+cmd);
			process = Runtime.getRuntime().exec(cmd);
			error = convertStreamToString(process.getInputStream());
//			Log.d(TAG, "LogWriter :: error= "+error);
		} catch (IOException var15) {
			var15.printStackTrace();
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException var13) {
					var13.printStackTrace();
				}
			}

			if (reader != null) {
				try {
					reader.close();
				} catch (IOException var12) {
					var12.printStackTrace();
				}
			}
		}

	}

	private static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;

		try {
			while((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException var13) {
			var13.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException var12) {
				var12.printStackTrace();
			}

		}

		return sb.toString();
	}

	public void batteryInfoToFile(boolean isNewOne, float temp, int level) {
		Log.v(TAG, "LogWriter :: batteryInfoToFile");
		String filePath = this.systemSharedData.getString(Constants.pref_key_file_path, "");
		String currentDatetime = sdf.format(new Date());
		filePath = filePath + File.separator + "batteryLog.txt";
		PackageManager manager = this._context.getPackageManager();
		PackageInfo info = null;

		try {
			info = manager.getPackageInfo(this._context.getPackageName(), 0);
		} catch (NameNotFoundException var14) {
			var14.printStackTrace();
		}

		String model = Build.MODEL;
		if (!model.startsWith(Build.MANUFACTURER)) {
			model = Build.MANUFACTURER + " " + model;
		}

		File file = new File(filePath);
//        Log.d(TAG, "LogWriter :: file= "+file.getPath());
		InputStreamReader reader = null;
		FileWriter writer = null;

		try {
			if(isNewOne){
				new TextResourceReader().writeLineInFile(file.getPath(), "DateTime Log: " + sdf.format(new Date()) + "\n", true);
				new TextResourceReader().writeLineInFile(file.getPath(), "Android version: " + Build.VERSION.SDK_INT + "\n", true);
				new TextResourceReader().writeLineInFile(file.getPath(), "Device: " + model + "\n", true);
				new TextResourceReader().writeLineInFile(file.getPath(), "App version: " + (info == null ? "(null)" : info.versionCode) + "\n", true);
				new TextResourceReader().writeLineInFile(file.getPath(), "temp: level"+ "\n", true);
				String  cmd = "su -c chmod 664 "+this.systemSharedData.getString(Constants.pref_key_file_path, "")+sep+"*.*";
				Process process = Runtime.getRuntime().exec(cmd);
				String error = convertStreamToString(process.getInputStream());
				Log.d(TAG, "LogWriter :: error= "+error);
			}
			new TextResourceReader().writeLineInFile(file.getPath(), temp + ":"+level+ "\n", true);
			Log.d(TAG, "LogWriter :: file.getPath()= "+file.getPath()+ " temp: " + temp + "- level: " + level);
		} catch (IOException var15) {
			var15.printStackTrace();
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException var13) {
					var13.printStackTrace();
				}
			}

			if (reader != null) {
				try {
					reader.close();
				} catch (IOException var12) {
					var12.printStackTrace();
				}
			}
		}

	}
}
