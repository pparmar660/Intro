package com.intro.webservice;

import org.json.JSONObject;

import com.intro.utils.GlobalVariables.SERVICE_TAG;

public interface JSONListener {
	public void onRemoteCallComplete(JSONObject jsonFromNet,SERVICE_TAG tag);
}