package com.camoedo.recorder;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.camoedo.recorder.fragment.PreviewFragment;
import com.camoedo.recorder.fragment.RecorderFragment;
import com.camoedo.recorder.service.RecorderService;
import com.camoedo.recorder.service.ServiceManager;

public class MainActivity extends AppCompatActivity
        implements PreviewFragment.OnFragmentInteractionListener {

    private static final String TAG = "MainActivity";

    private ServiceManager mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mService = new ServiceManager(this, RecorderService.class, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case RecorderService.MSG_REGISTERED:
                        break;
                    case RecorderService.MSG_UNREGISTERED:
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        Fragment fragment;
        if (mService.isRunning()) {
            mService.bind();
            Log.i(TAG, "Service bound");
            fragment = new RecorderFragment();
        } else {
            fragment = new PreviewFragment();
        }
        getFragmentManager().beginTransaction()
                .replace(R.id.content, fragment)
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mService != null) {
            try {
                mService.unbind();
            } catch (Throwable t) {
                Log.e(TAG, "Failed to unbind from the service", t);
            }
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

    @Override
    public void onClick() {
        if (!mService.isRunning()) {
            mService.stop();
        } else {
            mService.start();
        }
    }
}
