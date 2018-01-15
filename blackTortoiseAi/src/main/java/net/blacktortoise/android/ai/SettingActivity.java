
package net.blacktortoise.android.ai;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import net.blacktortoise.android.ai.core.MyPreferences;
import net.blacktortoise.android.ai.core.TagDetectorAlgorism;

import java.util.ArrayList;
import java.util.List;

public class SettingActivity extends Activity {
    private Switch mRotateCameraView;

    private Switch mReverseCameraView;

    private Spinner mPreviewSizeView;

    private Spinner mTagDetectorAlgorismView;

    private MyPreferences mPreferences;

    private TextView mGoodThresholdText;

    private SeekBar mGoodThresholdBar;

    private OnSeekBarChangeListener mGoodThresholdBarListener = new OnSeekBarChangeListener() {
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // none
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // none
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mGoodThresholdText.setText(String.valueOf(progress));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mRotateCameraView = (Switch) findViewById(R.id.rotateCameraSwitch);
        mReverseCameraView = (Switch) findViewById(R.id.reverseCameraSwitch);
        mPreviewSizeView = (Spinner) findViewById(R.id.previewSizeSpinner);
        mTagDetectorAlgorismView = (Spinner) findViewById(R.id.tagDetectorAlgorismSpinner);
        mGoodThresholdText = (TextView) findViewById(R.id.goodThresholdText);
        mGoodThresholdBar = (SeekBar) findViewById(R.id.goodThresholdBar);

        mGoodThresholdBar.setOnSeekBarChangeListener(mGoodThresholdBarListener);

        mGoodThresholdBar.setMax(100);
    }

    @Override
    protected void onResume() {
        super.onResume();

        {
            List<String> sizeStrs = new ArrayList<String>();
            {
                Camera mCapture = Camera.open();
                List<Camera.Size> sizes = mCapture.getParameters().getSupportedPictureSizes();
                mCapture.release();
                for (Camera.Size size : sizes) {
                    sizeStrs.add(size.width + "x" + size.height);
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, sizeStrs);
            mPreviewSizeView.setAdapter(adapter);
        }
        {
            ArrayAdapter<TagDetectorAlgorism> adapter = new ArrayAdapter<TagDetectorAlgorism>(this,
                    android.R.layout.simple_list_item_1, TagDetectorAlgorism.values());
            mTagDetectorAlgorismView.setAdapter(adapter);
        }

        {
            mPreferences = new MyPreferences(this);
            mRotateCameraView.setChecked(mPreferences.isRotateCamera());
            mReverseCameraView.setChecked(mPreferences.isReverseCamera());
            setSelection(mPreviewSizeView, mPreferences.getPreviewSize());
            setSelection(mTagDetectorAlgorismView, mPreferences.getTagDetectorAlgorism());
            mGoodThresholdBar.setProgress((int) (mPreferences.getGoodThreshold() * 100));
        }
    }

    public void setSelection(AdapterView<?> view, Object obj) {
        if (obj != null) {
            for (int i = 0; i < view.getCount(); i++) {
                if (obj.equals(view.getItemAtPosition(i))) {
                    view.setSelection(i);
                    break;
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPreferences.edit();
        mPreferences.putRotateCamera(mRotateCameraView.isChecked());
        mPreferences.putReverseCamera(mReverseCameraView.isChecked());
        mPreferences.putPreviewSize(String.valueOf(mPreviewSizeView.getSelectedItem()));
        mPreferences.putTagDetectorAlgorism((TagDetectorAlgorism) mTagDetectorAlgorismView
                .getSelectedItem());
        mPreferences.putGoodThreshold((float) mGoodThresholdBar.getProgress() / 100f);
        mPreferences.commit();
        mPreferences = null;
    }
}
