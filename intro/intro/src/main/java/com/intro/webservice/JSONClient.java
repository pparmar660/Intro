package com.intro.webservice;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;

import org.json.JSONException;
import org.json.JSONObject;

import com.intro.customviews.CustomDialog;
import com.intro.utils.GlobalVariables;
import com.intro.utils.GlobalVariables.SERVICE_TAG;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.WindowManager;

public class JSONClient extends AsyncTask<String, Void, JSONObject> {
	private CustomDialog progressDia;
	JSONListener getJSONListener;
	Context curContext;
	Hashtable<String, String> parameters;
	String method;
	SERVICE_TAG tag;
	boolean hideDialog = false;
	private static HttpURLConnection connection;

	public JSONClient(Context context, Hashtable<String, String> parameters,
			String method, JSONListener listener, SERVICE_TAG tag) {
		this.getJSONListener = listener;
		curContext = context;
		this.method = method;
		this.parameters = parameters;
		this.tag = tag;
	}

	public void setHideDialog(boolean hideDialog) {
		this.hideDialog = hideDialog;
	}

	public static JSONObject connect(String urlHit, String method,
			Hashtable<String, String> parameters) {
		try {
			URL url = new URL(urlHit.toString());
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			connection.setConnectTimeout(30000);
			connection.setRequestMethod(method.toUpperCase());
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			// Send request
			if (parameters != null) {
				DataOutputStream outputStream = new DataOutputStream(
						connection.getOutputStream());
				outputStream.writeBytes(getPostParamString(parameters));
				outputStream.flush();
				outputStream.close();
			}

			// Get Response
			InputStream inputStream = connection.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(inputStream));
			String line;
			StringBuffer response = new StringBuffer();
			while ((line = bufferedReader.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			bufferedReader.close();
			// Get hold of the response entity
			if (response != null) {
				JSONObject json = new JSONObject(response.toString());
				return json;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

		return null;
	}

	/**
	 * Pass Hashtable of key value pair we need to post
	 * 
	 * @param params
	 * @return
	 */
	private static String getPostParamString(Hashtable<String, String> params) {
		if (params.size() == 0)
			return "";
		StringBuffer buf = new StringBuffer();
		Enumeration<String> keys = params.keys();
		while (keys.hasMoreElements()) {
			buf.append(buf.length() == 0 ? "" : "&");
			String key = keys.nextElement();
			buf.append(key).append("=").append(params.get(key));
		}
		return buf.toString();
	}

	@Override
	public void onPreExecute() {
		if (!hideDialog) {

			progressDia = CustomDialog
					.show(curContext, GlobalVariables.loading);

		}
	}

	@Override
	protected JSONObject doInBackground(String... urls) {
		return connect(urls[0], method, parameters);
	}

	@Override
	protected void onPostExecute(JSONObject json) {
		getJSONListener.onRemoteCallComplete(json, tag);
		if (progressDia != null && progressDia.isShowing())
			progressDia.dismiss();
	}
}