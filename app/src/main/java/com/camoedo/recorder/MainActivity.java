package com.camoedo.recorder;

import com.camoedo.recorder.service.CameraService;
import com.camoedo.recorder.service.ServiceManager;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView textStatus;

    private ServiceManager mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textStatus = (TextView)findViewById(R.id.status);

        mService = new ServiceManager(this, CameraService.class, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CameraService.MSG_REGISTERED:
                        textStatus.setText("Activity registered!");
                        break;
                    case CameraService.MSG_UNREGISTERED:
                        textStatus.setText("Activity un-registered!");
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mService.isRunning()) {
                    mService.stop();
                } else {
                    mService.start();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mService.isRunning()) {
            mService.bind();
            Log.i(TAG, "Service bound");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            mService.unbind();
        } catch (Throwable t) {
            Log.e(TAG, "Failed to unbind from the service", t);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
