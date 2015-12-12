
package net.blacktortoise.android.ai;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import net.blacktortoise.android.ai.core.MyPreferences;
import net.blacktortoise.android.ai.db.DbHelper;
import net.blacktortoise.android.ai.model.TagItemModel;
import net.blacktortoise.android.ai.tagdetector.TagDetectResult;
import net.blacktortoise.android.ai.tagdetector.TagDetector;
import net.blacktortoise.android.ai.tagdetector.TagDetector.DetectFlags;
import net.blacktortoise.android.ai.tagdetector.TagItem;
import net.blacktortoise.android.ai.util.MyCapture;
import net.blacktortoise.android.ai.util.WorkCaches;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.Handler;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class TakeTagActivity extends Activity implements OnClickListener, MyCapture.IMyCaptureListener {
    public static final String EXTRA_TEST_MODE = "testMode";

    private WorkCaches mWorkCaches;

    private MyCapture mMyCapture;

    private ImageView mCaptureImageView;

    private TextView mDetectingLevelText;

    private int mSeqCapMat;

    private int mSeqResultMat;

    private int mSeqResizeMat;

    private TagItem mTagItem;

    private List<Bitmap> mBitmaps;

    private int mMipmapLevel = Constants.MIPMAP_LEVEL;

    private MyPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_tag);

        mPref = new MyPreferences(this);

        mBitmaps = new ArrayList<Bitmap>();
        mWorkCaches = new WorkCaches();
        mSeqCapMat = mWorkCaches.getNextWorkCachesSeq();
        mSeqResultMat = mWorkCaches.getNextWorkCachesSeq();
        mSeqResizeMat = mWorkCaches.getNextWorkCachesSeq();

        mDetectingLevelText = (TextView)findViewById(R.id.detectingLevelText);
        mCaptureImageView = (ImageView)findViewById(R.id.captureImageView);
        mCaptureImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTagTaker.resetMatch = true;
            }
        });

        if (getIntent().getBooleanExtra(EXTRA_TEST_MODE, false)) {
            findViewById(R.id.saveButton).setVisibility(View.INVISIBLE);
        }

        findViewById(R.id.saveButton).setOnClickListener(this);


        SurfaceView surfaceView = new SurfaceView(this);
        addContentView(surfaceView, new ViewGroup.LayoutParams(1, 1));
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    mMyCapture = new MyCapture(mWorkCaches);
                    mMyCapture.open(holder, mPref.isRotateCamera(), mPref.isReverseCamera(),
                            mPref.getPreviewSizeAsSize());
                    mMyCapture.setMyCaptureListener(TakeTagActivity.this);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (mMyCapture != null) {
                    try {
                        mMyCapture.release();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    mMyCapture = null;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        MyPreferences pref = new MyPreferences(this);
        {
            mTagDetector = pref.getTagDetectorAlgorism().createTagDetector();
            mTagDetector.setGoodThreshold(pref.getGoodThreshold());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCaptureImageView.setImageBitmap(null);
        mWorkCaches.release();
    }

    private TagDetector mTagDetector;

    private TagTaker mTagTaker = new TagTaker();

    private class TagTaker {
        boolean resetMatch = false;

        int takeMatchNum = 0;
    }

    @Override
    public void onTakePicture(Mat m4) {
        Rect rect = new Rect((int)(m4.width() / 4f), (int)(m4.height() / 4f), m4.width() / 2,
                m4.height() / 2);
        if (mTagTaker.resetMatch) {
            mTagTaker.resetMatch = false;
            mTagTaker.takeMatchNum = 5;
            mTagDetector.removeTagItem(0);
            mTagItem = new TagItem(rect.width, rect.height);
            mTagDetector.putTagItem(0, mTagItem);
            mBitmaps.clear();

            double rate = Math.sqrt(Constants.MIPMAP_RATE);
            int rw = m4.width();
            int rh = m4.height();

            for (int i = 0; i < mMipmapLevel; i++) {
                Rect r = new Rect((m4.width() - rw) / 2, (m4.height() - rh) / 2, rw, rh);
                Mat tmp = mWorkCaches.getWorkMat(mSeqResizeMat, r.width, r.height, m4.type());
                {
                    Imgproc.resize(m4, tmp, new Size(r.width, r.height));
                    Bitmap bitmap = Bitmap.createBitmap(tmp.width(), tmp.height(),
                            Config.ARGB_8888);
                    Utils.matToBitmap(tmp, bitmap);
                    mBitmaps.add(bitmap);
                    mTagDetector.upgradeTagItem(mTagItem, bitmap);
                }

                rw = (int)(rw / rate);
                rh = (int)(rh / rate);
                if (rw == 0 || rh == 0) {
                    break;
                }
            }

        }
        // if (mTagTaker.takeMatchNum > 0) { // Extract keypoints for
        // // tag
        // mTagTaker.takeMatchNum--;
        // Mat tmp = m4.submat(rect);
        // mTagDetector.upgradeTagItem(mTagDetector.getTagItem(0), tmp);
        // {
        // Bitmap bitmap = Bitmap
        // .createBitmap(tmp.width(), tmp.height(), Config.ARGB_8888);
        // Utils.matToBitmap(tmp, bitmap);
        // mBitmaps.add(bitmap);
        // }
        // }

        // ======================

        { // Tag detection
            Mat resultMat = mWorkCaches.getWorkMat(5, m4.height(), m4.width(), m4.type());
            m4.copyTo(resultMat);
            {
//                Imgproc.rectangle(m4, new Point(rect.x, rect.y), new Point(rect.x + rect.width,
//                        rect.y + rect.height), new Scalar(1, 1, 1, 1));
            }
            Bitmap bm = mWorkCaches.getWorkBitmap(0, m4.cols(), m4.rows());
            Utils.matToBitmap(m4, bm);
            TagDetectResult result = mTagDetector.detectTag(m4, resultMat, 0,
                    EnumSet.of(DetectFlags.RECORD_LEVELS));
            Utils.matToBitmap(resultMat, bm);
            mCaptureImageView.setImageBitmap(bm);
            if (result != null) {
                mDetectingLevelText.setText(makeString(result.getDetectedLevels()));
            } else {
                mDetectingLevelText.setText("");
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.saveButton) {
            if (mBitmaps.size() > 0) {
                TagItemModel model = new TagItemModel();
                model.setWidth(mTagItem.getWidth());
                model.setHeight(mTagItem.getHeight());
                model.setBitmaps(mBitmaps.toArray(new Bitmap[mBitmaps.size()]));
                model.updateThumbnail();

                DbHelper dbHelper = new DbHelper(this);
                try {
                    dbHelper.registerTagItemModel(model);
                } finally {
                    dbHelper.close();
                }
                finish();
            }
        }
    }

    private String makeString(SparseBooleanArray array) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.size(); i++) {
            if (array.valueAt(i)) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(array.keyAt(i));
            }
        }
        return sb.toString();
    }
}
