package com.matejdro.pebblekeep.keepdata;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.matejdro.pebblekeep.DataReceiver;
import com.matejdro.pebblekeep.Pebble;

public class TextNote extends KeepItem {
	private String title;
	private String note;
	
	private String trimmedText;
	
	public TextNote(String title, int parentId, SQLiteDatabase db)
	{
		
		Cursor cursor = db.rawQuery("SELECT text FROM list_item WHERE list_parent_id = ? LIMIT 1", new String[] { Integer.toString(parentId) });
		cursor.moveToNext();
		
		note = cursor.getString(0);
		
		if (title == null || title.trim().length() == 0)
			title = note;
		else
		{
			note = title + "\n\n" + note;
		}
		this.title = title;

		cursor.close();
	}
	
	@Override
	public String getTitle() {
		return title;
	}

	private void sendNote(Context context, int location)
	{
		int length = Math.min(trimmedText.length() - location, 75);

		PebbleDictionary data = new PebbleDictionary();

		data.addInt8(0, (byte) 1); 
		data.addUint16(1, location);
		data.addUint16(2,  length);

		String subString = trimmedText.substring(location, location + length);
		data.addString(4, subString);

		PebbleKit.sendDataToPebble(context, DataReceiver.keepUUID, data);
	}
	
	@Override
	public void noteOpened(Context context) {
		trimmedText = Pebble.prepareString(note, Pebble.FULLSCREEN_TEXT_LIMIT);
		
		sendNote(context, 0);
	}

	@Override
	public void dataReceived(PebbleDictionary data, Context context) {		
		int location = data.getUnsignedInteger(1).intValue();
		sendNote(context, location);

	}
}
