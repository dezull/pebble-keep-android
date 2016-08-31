package com.matejdro.pebblekeep;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import org.json.JSONException;

import java.util.UUID;

import static com.getpebble.android.kit.Constants.APP_UUID;
import static com.getpebble.android.kit.Constants.MSG_DATA;
import static com.getpebble.android.kit.Constants.TRANSACTION_ID;


public class DataReceiver extends BroadcastReceiver {

	public final static UUID keepUUID = UUID.fromString("0D1DB88B-D34D-434C-BCAB-1B12BC88E36D");

    public void receiveData(final Context context, final int transactionId, final PebbleDictionary data)
    {    	
    	PebbleKit.sendAckToPebble(context, transactionId & 0xFF);
    	
    	KeepReaderService.gotPebblePacket(context, data);
    }
	
	public void onReceive(final Context context, final Intent intent) {

		final UUID receivedUuid = (UUID) intent.getSerializableExtra(APP_UUID);

        // Pebble-enabled apps are expected to be good citizens and only inspect broadcasts containing their UUID
        if (!receivedUuid.equals(keepUUID)) {
            return;
        }

        final int transactionId = intent.getIntExtra(TRANSACTION_ID, -1);
        final String jsonData = intent.getStringExtra(MSG_DATA);
        if (jsonData == null || jsonData.isEmpty()) {
            return;
        }

        try {
            final PebbleDictionary data = PebbleDictionary.fromJson(jsonData);
            receiveData(context, transactionId & 0xFF, data);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
	}
	
}
