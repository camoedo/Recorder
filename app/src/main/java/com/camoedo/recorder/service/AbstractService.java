package com.camoedo.recorder.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;

public abstract class AbstractService extends Service {
    static final int MSG_REGISTER_CLIENT = 9991;
    static final int MSG_UNREGISTER_CLIENT = 9992;

    public static final int MSG_REGISTERED = 1;
    public static final int MSG_UNREGISTERED = 2;

    ArrayList<Messenger> mClients = new ArrayList<>(); // Keeps track of all current registered clients.

    final Messenger mMessenger = new Messenger(new IncomingHandler(this)); // Target we publish for clients to send messages to IncomingHandler.

    static class IncomingHandler extends Handler { // Handler of incoming messages from clients.
        private final AbstractService mService;

        public IncomingHandler(AbstractService service) {
            mService = service;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    Log.i("MyService", "Client registered: " + msg.replyTo);
                    mService.getClients().add(msg.replyTo);
                    mService.send(Message.obtain(null, MSG_REGISTERED));
                    break;
                case MSG_UNREGISTER_CLIENT:
                    Log.i("MyService", "Client un-registered: " + msg.replyTo);
                    mService.send(Message.obtain(null, MSG_UNREGISTERED));
                    mService.getClients().remove(msg.replyTo);
                    break;
                default:
                    mService.onReceiveMessage(msg);
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        onStartService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("MyService", "Received start id " + startId + ": " + intent);
        return START_STICKY; // run until explicitly stopped.
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        onStopService();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    protected void send(Message msg) {
        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {
                Log.i("MyService", "Sending message to clients: " + msg);
                mClients.get(i).send(msg);
            } catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                Log.e("MyService", "Client is dead. Removing from list: " + i);
                mClients.remove(i);
            }
        }
    }

    public ArrayList<Messenger> getClients() {
        return mClients;
    }

    public abstract void onStartService();

    public abstract void onStopService();

    public abstract void onReceiveMessage(Message msg);
}
