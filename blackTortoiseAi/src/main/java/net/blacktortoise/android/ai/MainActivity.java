
package net.blacktortoise.android.ai;

import net.blacktortoise.androidlib.BlackTortoiseFunctions;

import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
    private MainActivity me = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.testTagTrackButton).setOnClickListener(this);
        findViewById(R.id.debugButton).setOnClickListener(this);
        findViewById(R.id.tagManagementButtion).setOnClickListener(this);
        findViewById(R.id.settingButtion).setOnClickListener(this);
        findViewById(R.id.selectDeviceButtion).setOnClickListener(this);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading OpenCv");
        progressDialog.setCancelable(false);
        progressDialog.show();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
                new LoaderCallbackInterface() {
                    @Override
                    public void onPackageInstall(int operation, InstallCallbackInterface callback) {

                    }

                    @Override
                    public void onManagerConnected(int status) {
                        if (status == LoaderCallbackInterface.SUCCESS) {
                            progressDialog.dismiss();
                        } else {
                            Toast.makeText(me, "Loading OpenCV failed:" + status,
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.testTagTrackButton) {
            Intent intent = new Intent(this, TakeTagActivity.class);
            intent.putExtra(TakeTagActivity.EXTRA_TEST_MODE, true);
            startActivity(intent);
        } else if (v.getId() == R.id.debugButton) {
            Intent intent = new Intent(this, DebugActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.tagManagementButtion) {
            Intent intent = new Intent(this, TagManagementActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.settingButtion) {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.selectDeviceButtion) {
            Intent intent = BlackTortoiseFunctions.createSelectDeviceActivityIntent();
            startActivityForResult(intent, -1);
        }
    }
}
