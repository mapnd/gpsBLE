package com.example.johnpconsidine.blemap;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by johnpconsidine on 4/8/16.
 */
public class BleTransmitService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public BleTransmitService(String name) {
        super("TransmitIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
