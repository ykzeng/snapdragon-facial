package com.lenss.yzeng.utils;

import android.hardware.Camera;
import android.util.Log;

import com.qualcomm.snapdragon.sdk.face.FaceData;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing;
import com.qualcomm.snapdragon.sdk.sample.DrawView;

import java.io.Serializable;
import java.util.EnumSet;

/**
 * Created by yukun on 12/30/2016.
 */

public class FaceDetector {
    // TODO how to init those variables
    private FacialProcessing faceProc;
    private String TAG = "yzeng.utils.FD";

    public FaceDetector(FacialProcessing faceProc){
        this.faceProc = faceProc;
    }

    public void release(){
        this.faceProc.release();
    }

    public FaceDetectionWrapper detectFaces(byte[] data, int previewSizeWidth, int previewSizeHeight,
                                            FacialProcessing.PREVIEW_ROTATION_ANGLE angleEnum, boolean isMirrored,
                                            int surfaceWidth, int surfaceHeight) {
        FaceData[] faceArray = null;

        faceProc.setFrame(data, previewSizeWidth, previewSizeHeight, isMirrored, angleEnum);
        // ends here

        int numFaces = faceProc.getNumFaces();

        if (numFaces != 0){
            Log.d(TAG, "Face Detected");
            faceArray = faceProc.getFaceData(EnumSet.of(FacialProcessing.FP_DATA.FACE_RECT,
                    FacialProcessing.FP_DATA.FACE_COORDINATES, FacialProcessing.FP_DATA.FACE_CONTOUR,
                    FacialProcessing.FP_DATA.FACE_SMILE, FacialProcessing.FP_DATA.FACE_ORIENTATION,
                    FacialProcessing.FP_DATA.FACE_BLINK, FacialProcessing.FP_DATA.FACE_GAZE));
            faceProc.normalizeCoordinates(surfaceWidth, surfaceHeight);
        }
        return new FaceDetectionWrapper(faceArray, numFaces);
    }

    public FaceDetectionWrapper detectFaces(FrameData frameData) {
        FaceData[] faceArray = null;
        byte[] data = frameData.getData();
        int previewSizeWidth = frameData.getPreviewSizeWidth(), previewSizeHeight = frameData.getPreviewSizeHeight();
        boolean isMirrored = frameData.isMirrored();
        FacialProcessing.PREVIEW_ROTATION_ANGLE angleEnum = frameData.getAngleEnum();
        int surfaceWidth = frameData.getSurfaceWidth(), surfaceHeight = frameData.getSurfaceHeight();

        faceProc.setFrame(data, previewSizeWidth, previewSizeHeight, isMirrored, angleEnum);

        int numFaces = faceProc.getNumFaces();

        if (numFaces != 0){
            Log.d(TAG, "Face Detected");
            faceArray = faceProc.getFaceData(EnumSet.of(FacialProcessing.FP_DATA.FACE_RECT,
                    FacialProcessing.FP_DATA.FACE_COORDINATES, FacialProcessing.FP_DATA.FACE_CONTOUR,
                    FacialProcessing.FP_DATA.FACE_SMILE, FacialProcessing.FP_DATA.FACE_ORIENTATION,
                    FacialProcessing.FP_DATA.FACE_BLINK, FacialProcessing.FP_DATA.FACE_GAZE));
            faceProc.normalizeCoordinates(surfaceWidth, surfaceHeight);
        }
        return new FaceDetectionWrapper(faceArray, numFaces);
    }

    public static class FrameData implements Serializable{
        private byte[] data;
        private int previewSizeWidth;
        private int previewSizeHeight;
        private FacialProcessing.PREVIEW_ROTATION_ANGLE angleEnum;
        private boolean isMirrored;
        private int surfaceWidth;
        private int surfaceHeight;

        public FrameData(byte[] data, int previewSizeWidth, int previewSizeHeight, FacialProcessing.PREVIEW_ROTATION_ANGLE angleEnum, boolean isMirrored, int surfaceWidth, int surfaceHeight) {
            this.data = data;
            this.previewSizeWidth = previewSizeWidth;
            this.previewSizeHeight = previewSizeHeight;
            this.angleEnum = angleEnum;
            this.isMirrored = isMirrored;
            this.surfaceWidth = surfaceWidth;
            this.surfaceHeight = surfaceHeight;
        }

        public byte[] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        public int getPreviewSizeWidth() {
            return previewSizeWidth;
        }

        public void setPreviewSizeWidth(int previewSizeWidth) {
            this.previewSizeWidth = previewSizeWidth;
        }

        public int getPreviewSizeHeight() {
            return previewSizeHeight;
        }

        public void setPreviewSizeHeight(int previewSizeHeight) {
            this.previewSizeHeight = previewSizeHeight;
        }

        public FacialProcessing.PREVIEW_ROTATION_ANGLE getAngleEnum() {
            return angleEnum;
        }

        public void setAngleEnum(FacialProcessing.PREVIEW_ROTATION_ANGLE angleEnum) {
            this.angleEnum = angleEnum;
        }

        public boolean isMirrored() {
            return isMirrored;
        }

        public void setMirrored(boolean mirrored) {
            isMirrored = mirrored;
        }

        public int getSurfaceWidth() {
            return surfaceWidth;
        }

        public void setSurfaceWidth(int surfaceWidth) {
            this.surfaceWidth = surfaceWidth;
        }

        public int getSurfaceHeight() {
            return surfaceHeight;
        }

        public void setSurfaceHeight(int surfaceHeight) {
            this.surfaceHeight = surfaceHeight;
        }
    }
}
