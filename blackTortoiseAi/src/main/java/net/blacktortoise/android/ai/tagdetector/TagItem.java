
package net.blacktortoise.android.ai.tagdetector;

import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

public class TagItem {
    public static class TagItemFrame {
        public final int width;

        public final int height;

        public final Mat descriptors;

        public final KeyPoint[] keyPoints;

        public TagItemFrame(int width, int height, Mat descriptors, KeyPoint[] keyPoints) {
            super();
            this.width = width;
            this.height = height;
            this.descriptors = descriptors;
            this.keyPoints = keyPoints;
        }
    }

    private String name;

    private int width;

    private int height;

    public List<TagItemFrame> mElements;

    public TagItem(int width, int height) {
        this.width = width;
        this.height = height;
        this.mElements = new ArrayList<TagItemFrame>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public List<TagItemFrame> getFrames() {
        return mElements;
    }

    public void setFrames(List<TagItemFrame> elements) {
        mElements = elements;
    }

}
