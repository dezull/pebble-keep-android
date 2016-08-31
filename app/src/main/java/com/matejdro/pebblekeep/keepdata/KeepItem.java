package com.matejdro.pebblekeep.keepdata;

import android.content.Context;

import com.getpebble.android.kit.util.PebbleDictionary;

public abstract class KeepItem {
	public abstract String getTitle();
	public abstract void noteOpened(Context context);
	public abstract void dataReceived(PebbleDictionary data, Context context);
}
