
package net.blacktortoise.android.ai.action;

import java.util.ArrayList;
import java.util.List;

import net.blacktortoise.android.ai.tagdetector.TagDetectResult;
import net.blacktortoise.android.ai.tagdetector.TagDetector;
import net.blacktortoise.android.ai.util.WorkCaches;

import org.opencv.core.Mat;

import android.os.SystemClock;

public class DetectAnyTagAction implements IAction<Integer, List<TagDetectResult>> {
    private static int sM4Seq = -1;

    @Override
    public void setup(IActionUtil util) {
        if (sM4Seq < 0) {
            sM4Seq = util.getNextWorkCachesSeq();
        }
    }

    @Override
    public List<TagDetectResult> execute(IActionUtil util, Integer timeout)
            throws InterruptedException {
        long t = SystemClock.elapsedRealtime();
        WorkCaches workCaches = util.getWorkCaches();
        TagDetector tagDetector = util.getTagDetector();
        Mat capMat = workCaches.getWorkMat(sM4Seq);
        List<TagDetectResult> results = new ArrayList<TagDetectResult>();
        do {
            if (util.getPicture(capMat)) {

                // ======================
                { // Tag detection
                    Mat resultMat = util.getResultMat();
                    if (resultMat != null) {
                        capMat.copyTo(resultMat);
                    }
                    results.clear();
                    tagDetector.detectTags(results, capMat, resultMat, null);
                }
                util.updateConsole();
                if (results.size() > 0) {
                    break;
                }
            }
            Thread.sleep(10);
        } while ((SystemClock.elapsedRealtime() - t) < timeout);
        return results;
    }
}
