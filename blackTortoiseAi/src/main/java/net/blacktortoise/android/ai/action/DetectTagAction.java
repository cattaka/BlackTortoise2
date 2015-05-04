
package net.blacktortoise.android.ai.action;

import net.blacktortoise.android.ai.tagdetector.TagDetectResult;
import net.blacktortoise.android.ai.tagdetector.TagDetector;
import net.blacktortoise.android.ai.util.WorkCaches;

import org.opencv.core.Mat;

import android.os.SystemClock;

public class DetectTagAction implements IAction<DetectTagAction.DetectTagArgs, TagDetectResult> {
    public static class DetectTagArgs {
        public final int tagKey;

        public final int timeout;

        public DetectTagArgs(int tagKey, int timeout) {
            super();
            this.tagKey = tagKey;
            this.timeout = timeout;
        }
    }

    private static int sM4Seq = -1;

    @Override
    public void setup(IActionUtil util) {
        if (sM4Seq < 0) {
            sM4Seq = util.getNextWorkCachesSeq();
        }
    }

    @Override
    public TagDetectResult execute(IActionUtil util, DetectTagArgs args)
            throws InterruptedException {
        long t = SystemClock.elapsedRealtime();
        WorkCaches workCaches = util.getWorkCaches();
        TagDetector tagDetector = util.getTagDetector();
        Mat capMat = workCaches.getWorkMat(sM4Seq);
        TagDetectResult result = null;
        do {
            if (util.getCapture().takePicture(capMat)) {

                // ======================
                { // Tag detection
                    Mat resultMat = util.getResultMat();
                    if (resultMat != null) {
                        capMat.copyTo(resultMat);
                    }
                    result = tagDetector.detectTag(capMat, resultMat, args.tagKey, null);
                }
                util.updateConsole();
                if (result != null) {
                    break;
                }
            }
            Thread.sleep(10);
        } while ((SystemClock.elapsedRealtime() - t) < args.timeout);
        return result;
    }
}
