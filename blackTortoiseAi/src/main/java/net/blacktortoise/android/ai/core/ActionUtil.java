
package net.blacktortoise.android.ai.core;

import java.util.concurrent.Semaphore;

import net.blacktortoise.android.ai.action.ConsoleDto;
import net.blacktortoise.android.ai.action.IActionUtil;
import net.blacktortoise.android.ai.tagdetector.TagDetector;
import net.blacktortoise.android.ai.util.MyCapture;
import net.blacktortoise.android.ai.util.WorkCaches;

import org.opencv.core.Mat;

import android.os.Handler;

public class ActionUtil implements IActionUtil {
    public static interface IActionUtilListener {
        public void onUpdateConsole(ConsoleDto dto);
    }

    private static Handler sHandler = new Handler();

    private Semaphore mSemaphore = new Semaphore(1);

    private Object mMutex = new Object();

    private WorkCaches mWorkCaches;

    private MyCapture mMyCapture;

    private TagDetector mTagDetector;

    private BlackTortoiseServiceWrapperEx mServiceWrapper;

    private Mat mResultMat;

    private IActionUtilListener mListener;

    public ActionUtil(WorkCaches workCaches, MyCapture myCapture, TagDetector tagDetector,
            BlackTortoiseServiceWrapperEx serviceWrapper, IActionUtilListener listener) {
        super();
        mWorkCaches = workCaches;
        mMyCapture = myCapture;
        mTagDetector = tagDetector;
        mServiceWrapper = serviceWrapper;
        mListener = listener;
    }

    public ActionUtil() {
        mWorkCaches = new WorkCaches();
    }

    @Override
    public TagDetector getTagDetector() {
        return mTagDetector;
    }

    @Override
    public BlackTortoiseServiceWrapperEx getServiceWrapper() {
        return mServiceWrapper;
    }

    @Override
    public WorkCaches getWorkCaches() {
        return mWorkCaches;
    }

    @Override
    public int getNextWorkCachesSeq() {
        return mWorkCaches.getNextWorkCachesSeq();
    }

    @Override
    public Mat getResultMat() {
        return mResultMat;
    }

    @Override
    public MyCapture getCapture() {
        return mMyCapture;
    }

    @Override
    public void updateConsole() throws InterruptedException {
        if (mListener != null) {
            final ConsoleDto dto = new ConsoleDto();
            dto.setLastForward(mServiceWrapper.getLastForward());
            dto.setLastTurn(mServiceWrapper.getLastTurn());
            dto.setLastYaw(mServiceWrapper.getLastYaw());
            dto.setLastPitch(mServiceWrapper.getLastPitch());
            dto.setResultMat(mResultMat);

            synchronized (mMutex) {
                mSemaphore.acquire();
                sHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onUpdateConsole(dto);
                        mSemaphore.release();
                    }
                });
                mSemaphore.acquire();
                mSemaphore.release();
            }
        }
    }

    public void setResultMat(Mat resultMat) {
        mResultMat = resultMat;
    }

}
