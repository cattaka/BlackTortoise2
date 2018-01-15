
package net.blacktortoise.android.ai.tagdetector;

import android.graphics.Bitmap;
import android.util.SparseArray;
import android.util.SparseBooleanArray;

import net.blacktortoise.android.ai.Constants;
import net.blacktortoise.android.ai.model.TagItemModel;
import net.blacktortoise.android.ai.tagdetector.TagItem.TagItemFrame;
import net.blacktortoise.android.ai.util.PointUtil;
import net.blacktortoise.android.ai.util.WorkCaches;

import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class TagDetector {
    public enum DetectFlags {
        RECORD_LEVELS, DRAW_GOOD;
    }

    private static final int CACHE_DESCRIPTORS = 4;

    private static final int CACHE_UPGRADE_TAG = 5;

    private static final int CACHE_GRAYSCALE_1 = 6;

    private static final int CACHE_GRAYSCALE_2 = 7;

    private SparseArray<TagItem> mTagItems;

    private WorkCaches mWorkCaches;

    private FeatureDetector mDetector;

    private DescriptorExtractor mExtractor;

    private DescriptorMatcher mMatcher;

    private float mGoodThreshold = 0.6f;

    private TagDetector() {
        mTagItems = new SparseArray<TagItem>();
        mWorkCaches = new WorkCaches();
    }

    public TagDetector(FeatureDetector detector, DescriptorExtractor extractor,
                       DescriptorMatcher matcher) {
        this();
        mDetector = detector;
        mExtractor = extractor;
        mMatcher = matcher;
    }

    public boolean createTagItem(TagItemModel model) {
        TagItem item = new TagItem(model.getWidth(), model.getHeight());
        item.setName(model.getName());
        for (Bitmap bitmap : model.getBitmaps()) {
            upgradeTagItem(item, bitmap);
        }
        mTagItems.put(model.getId().intValue(), item);
        return true;
    }

    public void putTagItem(int key, TagItem tagItem) {
        mTagItems.put(key, tagItem);
    }

    public TagItem getTagItem(int key) {
        return mTagItems.get(key);
    }

    public int getTagItemIdAt(int index) {
        return mTagItems.keyAt(index);
    }

    public TagItem getTagItemAt(int index) {
        return mTagItems.valueAt(index);
    }

    public void removeTagItem(int key) {
        mTagItems.remove(key);
    }

    public FeatureDetector getDetector() {
        return mDetector;
    }

    public DescriptorExtractor getExtractor() {
        return mExtractor;
    }

    public DescriptorMatcher getMatcher() {
        return mMatcher;
    }

    public void upgradeTagItem(TagItem target, Bitmap src) {
        Mat tmp = mWorkCaches.getWorkMat(CACHE_UPGRADE_TAG);
        Utils.bitmapToMat(src, tmp);
        upgradeTagItem(target, tmp);
    }

    public void upgradeTagItem(TagItem target, Mat src) {
        Mat grayscale2 = toGrayscale(src);

        MatOfKeyPoint mokp = new MatOfKeyPoint();
        Mat queryDescriptors = new Mat();
        mDetector.detect(grayscale2, mokp);
        mExtractor.compute(src, mokp, queryDescriptors);

        target.getFrames().add(
                new TagItemFrame(src.width(), src.height(), queryDescriptors, mokp.toArray()));
    }

    public void detectTags(List<TagDetectResult> dst, Mat src, Mat resultMat,
                           EnumSet<DetectFlags> flags) {
        Mat grayscale2 = toGrayscale(src);

        KeyPoint[] keypoints;
        Mat descriptors = mWorkCaches.getWorkMat(CACHE_DESCRIPTORS);
        { // Detect and Extract keypoints
            MatOfKeyPoint mokp = new MatOfKeyPoint();
            mDetector.detect(grayscale2, mokp);
            mExtractor.compute(grayscale2, mokp, descriptors);
            keypoints = mokp.toArray();
        }

        if (resultMat != null) {
            { // Draw Keypoints
                Scalar color = new Scalar(0xFF, 0xFF, 0xFF, 0xFF);
                for (KeyPoint kp : keypoints) {
                    Imgproc.circle(resultMat, kp.pt, (int) (kp.size / 8), color, 1);
                }
            }
        }

        dst.clear();
        for (int j = 0; j < mTagItems.size(); j++) {
            int tagKey = mTagItems.keyAt(j);
            Mat trainDescriptors = descriptors;
            TagDetectResult result = detectTagInner(src, resultMat, tagKey, trainDescriptors,
                    keypoints, flags);
            if (result != null) {
                dst.add(result);
            }
        }
    }

    public TagDetectResult detectTag(Mat src, Mat resultMat, int tagKey, EnumSet<DetectFlags> flags) {
        Mat grayscale2 = toGrayscale(src);

        KeyPoint[] keypoints;
        Mat descriptors = mWorkCaches.getWorkMat(CACHE_DESCRIPTORS);
        { // Detect and Extract keypoints
            MatOfKeyPoint mokp = new MatOfKeyPoint();
            mDetector.detect(grayscale2, mokp);
            mExtractor.compute(grayscale2, mokp, descriptors);
            keypoints = mokp.toArray();
        }
        if (descriptors.rows() > 0 && descriptors.cols() > 0) {
            if (resultMat != null) {
                { // Draw Keypoints
                    Scalar color = new Scalar(0xFF, 0xFF, 0xFF, 0xFF);
                    for (KeyPoint kp : keypoints) {
                        Imgproc.circle(resultMat, kp.pt, (int) (kp.size / 8), color, 1);
                    }
                }
            }

            Mat trainDescriptors = descriptors;
            return detectTagInner(src, resultMat, tagKey, trainDescriptors, keypoints, flags);
        } else {
            return null;
        }
    }

    public TagDetectResult detectTagInner(Mat src, Mat resultMat, int tagKey, Mat trainDescriptors,
                                          KeyPoint[] keypoints, EnumSet<DetectFlags> flags) {
        if (flags == null) {
            flags = EnumSet.noneOf(DetectFlags.class);
        }
        TagItem tagItem = mTagItems.get(tagKey);
        if (tagItem == null) {
            return null;
        }
        List<Point[]> goodPts = new ArrayList<Point[]>();
        SparseBooleanArray detectedLevels = (flags.contains(DetectFlags.RECORD_LEVELS)) ? new SparseBooleanArray()
                : null;
        int level = -1;
        for (TagItemFrame frame : tagItem.getFrames()) {
            level++;
            List<MatOfDMatch> matches = new ArrayList<MatOfDMatch>();
            Mat queryDescriptors = frame.descriptors;
            KeyPoint[] queryKeyPoints = frame.keyPoints;
            mMatcher.knnMatch(queryDescriptors, trainDescriptors, matches, 2);

            List<DMatch> good_matches = new ArrayList<DMatch>();
            for (MatOfDMatch mm : matches) {
                // DMatch[] m = mm.toArray();
                int cn = mm.channels();
                int num = (int) mm.total();
                DMatch[] m = new DMatch[num];
                if (num >= 2) {
                    float buff[] = new float[num * cn];
                    mm.get(0, 0, buff); // TODO: check ret val!
                    for (int i = 0; i < num; i++)
                        m[i] = new DMatch((int) buff[cn * i + 0], (int) buff[cn * i + 1],
                                (int) buff[cn * i + 2], buff[cn * i + 3]);

                    if (m[0].distance < mGoodThreshold * m[1].distance) {
                        good_matches.add(m[0]);
                    }
                }
            }
            MatOfPoint2f dstMop = null;
            float maxDistance = 1;
            if (good_matches.size() >= 4) {
                { // calculate maxDistance
                    for (MatOfDMatch mm : matches) {
                        // DMatch[] m = mm.toArray();
                        int cn = mm.channels();
                        int num = (int) mm.total();
                        if (num > 0) {
                            float buff[] = new float[num * cn];
                            mm.get(0, 0, buff); // TODO: check ret val!
                            for (int i = 0; i < num; i++) {
                                DMatch m = new DMatch((int) buff[cn * i + 0], (int) buff[cn * i + 1],
                                        (int) buff[cn * i + 2], buff[cn * i + 3]);
                                if (maxDistance < m.distance) {
                                    maxDistance = m.distance;
                                }
                            }
                        }
                    }
                }

                Mat homography;
                { // calculate homography
                    MatOfPoint2f srcPoints;
                    MatOfPoint2f dstPoints;
                    { // Pickup good matches to calculate homography
                        List<Point> srcPointList = new ArrayList<Point>();
                        List<Point> dstPointList = new ArrayList<Point>();
                        for (DMatch m : good_matches) {
                            srcPointList.add(queryKeyPoints[m.queryIdx].pt);
                            dstPointList.add(keypoints[m.trainIdx].pt);
                        }
                        srcPoints = new MatOfPoint2f(srcPointList.toArray(new Point[srcPointList
                                .size()]));
                        dstPoints = new MatOfPoint2f(dstPointList.toArray(new Point[dstPointList
                                .size()]));
                    }
                    homography = Calib3d.findHomography(srcPoints, dstPoints, Calib3d.RANSAC, 3);
                }
                Point[] srcPt = new Point[4];
                for (int i = 0; i < srcPt.length; i++) {
                    srcPt[i] = new Point();
                }
                MatOfPoint2f srcMop = new MatOfPoint2f( //
                        new Point(0, 0), //
                        new Point(frame.width, 0), //
                        new Point(frame.width, frame.height), //
                        new Point(0, frame.height));
                dstMop = new MatOfPoint2f(srcPt);
                try {
                    Core.perspectiveTransform(srcMop, dstMop, homography);
                } catch (CvException e) {
                    // FIXME
                    dstMop = null;
                }
            }
            if (resultMat != null) {
                if (dstMop != null) { // Draw line
                    Point[] dstPt = new Point[4];
                    dstPt = dstMop.toArray();
                    boolean valid = isValid(frame.width, frame.height, dstPt);
                    if (valid) {
                        // このレベルのもので利用可能なものを見つけた！！
                        goodPts.add(dstPt);
                        if (detectedLevels != null) {
                            detectedLevels.put(level, true);
                        }
                    }
                    // Scalar color = (valid) ? new Scalar(0xFF, 0xFF, 0, 0)
                    // : new Scalar(0xFF, 0,
                    // 0, 0xFF);
                    // Core.line(resultMat, dstPt[0], dstPt[1], color);
                    // Core.line(resultMat, dstPt[1], dstPt[2], color);
                    // Core.line(resultMat, dstPt[2], dstPt[3], color);
                    // Core.line(resultMat, dstPt[3], dstPt[0], color);
                }
                if (flags.contains(DetectFlags.DRAW_GOOD)) {
                    // Draw good point
                    for (DMatch m : good_matches) {
                        KeyPoint kp = keypoints[m.trainIdx];
                        Scalar color = new Scalar(0xFF * m.distance / maxDistance, 0xFF, 0, 0);
                        Imgproc.circle(resultMat, kp.pt, (int) (kp.size / 8), color, -1);
                    }
                }
            }
        }
        if (goodPts.size() >= 1) {
            Point[] pts = new Point[]{
                    new Point(), new Point(), new Point(), new Point()
            };
            calcAverage(pts, goodPts);
            TagDetectResult result = new TagDetectResult(tagKey, pts);
            if (resultMat != null) {
                Scalar color = new Scalar(0xFF, 0xFF, 0, 0);
                Imgproc.line(resultMat, pts[0], pts[1], color);
                Imgproc.line(resultMat, pts[1], pts[2], color);
                Imgproc.line(resultMat, pts[2], pts[3], color);
                Imgproc.line(resultMat, pts[3], pts[0], color);
            }
            result.setDetectedLevels(detectedLevels);
            return result;
        } else {
            return null;
        }
    }

    private void calcAverage(Point[] dst, List<Point[]> pts) {
        dst[0].x = 0;
        dst[0].y = 0;
        dst[1].x = 0;
        dst[1].y = 0;
        dst[2].x = 0;
        dst[2].y = 0;
        dst[3].x = 0;
        dst[3].y = 0;
        for (Point[] pt : pts) {
            dst[0].x += pt[0].x;
            dst[0].y += pt[0].y;
            dst[1].x += pt[1].x;
            dst[1].y += pt[1].y;
            dst[2].x += pt[2].x;
            dst[2].y += pt[2].y;
            dst[3].x += pt[3].x;
            dst[3].y += pt[3].y;
        }
        int n = pts.size();
        dst[0].x /= n;
        dst[0].y /= n;
        dst[1].x /= n;
        dst[1].y /= n;
        dst[2].x /= n;
        dst[2].y /= n;
        dst[3].x /= n;
        dst[3].y /= n;

    }

    private Mat toGrayscale(Mat src) {
        // if (true) {
        // return src;
        // }
        Mat grayscale2 = mWorkCaches.getWorkMat(CACHE_GRAYSCALE_2, src.width(), src.height(),
                CvType.CV_8U);
        {
            Mat grayscale1 = mWorkCaches.getWorkMat(CACHE_GRAYSCALE_1, src.width(), src.height(),
                    CvType.CV_8U);
            Imgproc.cvtColor(src, grayscale1, Imgproc.COLOR_BGR2GRAY);
            Core.normalize(grayscale1, grayscale2, 0, 255, Core.NORM_MINMAX);
        }
        return grayscale2;
    }

    public boolean isValid(double width, double height, Point[] ps) {
        double minSquare = 0.05;
        double maxSquare = Constants.MIPMAP_RATE;

        double s = PointUtil.getArea(ps) / (width * height);

        return (minSquare <= s && s <= maxSquare);
    }

    public void setGoodThreshold(float goodThreshold) {
        mGoodThreshold = goodThreshold;
    }

}
