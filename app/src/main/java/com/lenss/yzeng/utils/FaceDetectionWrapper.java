package com.lenss.yzeng.utils;

import com.qualcomm.snapdragon.sdk.face.FaceData;

/**
 * Created by yukun on 12/30/2016.
 */

public class FaceDetectionWrapper {
    private FaceData[] faceArray;
    private int numFaces;

    public FaceDetectionWrapper(FaceData[] faceArray, int numFaces) {
        this.faceArray = faceArray;
        this.numFaces = numFaces;
    }

    public FaceData[] getFaceArray() {
        return faceArray;
    }

    public void setFaceArray(FaceData[] faceArray) {
        this.faceArray = faceArray;
    }

    public int getNumFaces() {
        return numFaces;
    }

    public void setNumFaces(int numFaces) {
        this.numFaces = numFaces;
    }
}
