package com.matejdro.pebblekeep;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.matejdro.pebblekeep.keepdata.KeepItem;
import com.matejdro.pebblekeep.keepdata.ListNote;
import com.matejdro.pebblekeep.keepdata.TextNote;

public class KeepReaderService extends Service {
	public static KeepReaderService instance;
	public static List<KeepItem> keepEntries;

	public static KeepItem pickedItem;

	private SQLiteDatabase db;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		instance = null;

		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		instance = this;

		System.out.println("service started");

		start();		


		return Service.START_NOT_STICKY;
	}

	public void start()
	{
		File dbFile = RootUtil.copy(this);

		if (!dbFile.exists())
		{
			stopSelf();
			return;
		}


		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		db = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);


		queryNotes();

		sendListPart(0);

		//stopSelf();
	}


	//TX Paketi
	//0 = Note list
	//1 = Note text
	private void sendListPart(int index)
	{
		//0 = ID
		//1 = Index
		//2 = Max
		//3 = Text 1
		//4 = Text 2
		//5 = Text 3

		PebbleDictionary data = new PebbleDictionary();

		data.addInt8(0, (byte) 0); 
		data.addUint8(1, (byte) index);
		data.addUint8(2, (byte) keepEntries.size());

		for (int i = 0; i < 3; i++)
		{
			int entry = index + i;
			if (entry > keepEntries.size() - 1)
				continue;

			data.addString(3 + i, Pebble.prepareString(keepEntries.get(entry).getTitle(), 20));
		}

		PebbleKit.sendDataToPebble(this, DataReceiver.keepUUID, data);
	}

	private void prepareFullNote(int index)
	{
		pickedItem = keepEntries.get(index);
		pickedItem.noteOpened(this);
	}

	private void queryNotes()
	{
		keepEntries = new ArrayList<KeepItem>(50);

		Cursor cursor = db.rawQuery("SELECT _id, type, title FROM tree_entity WHERE is_archived = 0 AND is_deleted = 0 AND (Select Count(*) FROM list_item WHERE list_item.list_parent_id = tree_entity._id LIMIT 1) > 0 ORDER BY order_in_parent DESC LIMIT 50", null);

		while (cursor.moveToNext())
		{
			int id = cursor.getInt(0);
			int type = cursor.getInt(1);
			String title = cursor.getString(2);

			switch (type)
			{
			case 0:
				keepEntries.add(new TextNote(title, id, db));
				break;
			case 1:
				keepEntries.add(new ListNote(title, id, db));
				break;
			}
		}
	}

	public void gotPacketInternal(int id, PebbleDictionary data)
	{
		switch (id)
		{
		case 1:
			int index = data.getUnsignedInteger(1).intValue();
			sendListPart(index);
			break;
		case 2:
			index = data.getUnsignedInteger(1).intValue();
			prepareFullNote(index);
			break;
		case 3:
			pickedItem.dataReceived(data, this);
			break;

		}
	}

	public static void gotPebblePacket(Context context, PebbleDictionary data)
	{
		int id = data.getUnsignedInteger(0).intValue();

		System.out.println(id);

		//RX IDs:
		//0 = App started
		//1 = Send another part of list [1 = part of list];
		//2 = Start sending note
		//3 = Data from pebble / note type specific

		if (id == 0 && instance == null)
		{
			Intent start = new Intent(context, KeepReaderService.class);
			context.startService(start);
		}
		else if (id == 0)
		{
			instance.start();
		}
		else if (instance != null)
		{
			instance.gotPacketInternal(id, data);
		}

	}
}
