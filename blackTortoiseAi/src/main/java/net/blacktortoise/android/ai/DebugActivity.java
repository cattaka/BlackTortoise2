
package net.blacktortoise.android.ai;

import java.util.List;

import net.blacktortoise.android.ai.action.ConsoleDto;
import net.blacktortoise.android.ai.core.ActionUtil;
import net.blacktortoise.android.ai.core.ActionUtil.IActionUtilListener;
import net.blacktortoise.android.ai.core.BlackTortoiseServiceWrapperEx;
import net.blacktortoise.android.ai.core.MyPreferences;
import net.blacktortoise.android.ai.db.DbHelper;
import net.blacktortoise.android.ai.model.TagItemModel;
import net.blacktortoise.android.ai.tagdetector.TagDetector;
import net.blacktortoise.android.ai.thread.ActionThread;
import net.blacktortoise.android.ai.util.IndicatorDrawer;
import net.blacktortoise.android.ai.util.MyCapture;
import net.blacktortoise.android.ai.util.WorkCaches;
import net.blacktortoise.androidlib.BlackTortoiseFunctions;
import net.cattaka.libgeppa.IActiveGeppaService;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.WindowManager;
import android.widget.ImageView;

public class DebugActivity extends Activity {
    private int CACHE_ON_UPDATE_CONSOLE = 1;

    private DbHelper mDbHelper;

    private WorkCaches mWorkCaches;

    private ImageView mCaptureImageView;

    private ImageView mMoveIndicator;

    private ImageView mHeadIndicator;

    private TagDetector mTagDetector;

    private ActionUtil mActionUtil;

    private BlackTortoiseServiceWrapperEx mServiceWrapper;

    private MyCapture mMyCapture;

    private ActionThread mActionThread;

    private WakeLock mWakeLock;

    private IActionUtilListener mActionUtilListener = new IActionUtilListener() {
        private IndicatorDrawer mIndicatorDrawer = new IndicatorDrawer();

        private Bitmap mMoveBitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_4444);

        private Bitmap mHeadBitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_4444);

        @Override
        public void onUpdateConsole(ConsoleDto dto) {
            mIndicatorDrawer.drawMove(mMoveBitmap, dto);
            mIndicatorDrawer.drawHead(mHeadBitmap, dto);
            mMoveIndicator.setImageBitmap(mMoveBitmap);
            mHeadIndicator.setImageBitmap(mHeadBitmap);
            if (dto.getResultMat() != null) {
                Mat m = dto.getResultMat();
                Bitmap bm = mWorkCaches.getWorkBitmap(CACHE_ON_UPDATE_CONSOLE, m.width(),
                        m.height());
                Utils.matToBitmap(m, bm);
                mCaptureImageView.setImageBitmap(bm);
            }
        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceWrapper = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IActiveGeppaService btService = IActiveGeppaService.Stub.asInterface(service);
            mServiceWrapper = new BlackTortoiseServiceWrapperEx(btService);
            prepareActionThread();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        mWorkCaches = new WorkCaches();
        mCaptureImageView = (ImageView)findViewById(R.id.captureImageView);
        mMoveIndicator = (ImageView)findViewById(R.id.moveIndicator);
        mHeadIndicator = (ImageView)findViewById(R.id.headIndicator);

        PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, Constants.TAG);
    }

    @Override
    protected void onResume() {
        super.onResume();

        MyPreferences pref = new MyPreferences(this);

        Intent service = BlackTortoiseFunctions.createServiceIntent();
        bindService(service, mServiceConnection, Context.BIND_AUTO_CREATE);

        {
            if (mDbHelper != null) {
                mDbHelper.close();
            }
            mDbHelper = new DbHelper(this);
        }
        {
            mTagDetector = pref.getTagDetectorAlgorism().createTagDetector();
            mTagDetector.setGoodThreshold(pref.getGoodThreshold());
            {
                List<TagItemModel> models = mDbHelper.findTagItemModel(false);
                for (TagItemModel model : models) {
                    TagItemModel fullModel = mDbHelper.findTagItemModelById(model.getId());
                    mTagDetector.createTagItem(fullModel);
                }
            }
            VideoCapture capture = new VideoCapture();
            mMyCapture = new MyCapture(mWorkCaches, capture);
            mMyCapture.open(pref.isRotateCamera(), pref.isReverseCamera(),
                    pref.getPreviewSizeAsSize());
            prepareActionThread();
        }
        mWakeLock.acquire();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCaptureImageView.setImageBitmap(null);
        {
            mActionThread.stopSafety();
            mActionThread = null;
        }

        if (mActionUtil != null) {
            mActionUtil = null;
        }

        if (mServiceWrapper != null) {
            unbindService(mServiceConnection);
            ;
            mServiceWrapper = null;
        }

        if (mTagDetector != null) {
            // TODO リークしてないか？
            mTagDetector = null;
        }

        if (mMyCapture != null) {
            mMyCapture.release();
            mMyCapture = null;
        }

        mWorkCaches.release();
        {
            if (mDbHelper != null) {
                mDbHelper.close();
            }
        }
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mWakeLock.release();
    }

    private void prepareActionThread() {
        if (mActionUtil == null) {
            if (mMyCapture != null && mServiceWrapper != null) {
                mActionUtil = new ActionUtil(mWorkCaches, mMyCapture, mTagDetector,
                        mServiceWrapper, mActionUtilListener);
                mActionUtil.setResultMat(new Mat());
                mActionThread = new ActionThread(mActionUtil);
                mActionThread.start();
            }
        }

    }
}
