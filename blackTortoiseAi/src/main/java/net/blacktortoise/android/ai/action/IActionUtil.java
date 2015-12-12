
package net.blacktortoise.android.ai.action;

import net.blacktortoise.android.ai.core.BlackTortoiseServiceWrapperEx;
import net.blacktortoise.android.ai.tagdetector.TagDetector;
import net.blacktortoise.android.ai.util.MyCapture;
import net.blacktortoise.android.ai.util.WorkCaches;

import org.opencv.core.Mat;

public interface IActionUtil {
    public TagDetector getTagDetector();

    public BlackTortoiseServiceWrapperEx getServiceWrapper();

    public WorkCaches getWorkCaches();

    public int getNextWorkCachesSeq();

    public Mat getResultMat();

    public MyCapture getCapture();

    public boolean getPicture(Mat dest);

    public void updateConsole() throws InterruptedException;
}
