package com.lenss.yzeng;

import android.util.Log;
import android.util.Pair;

import com.google.gson.annotations.Expose;
import com.lenss.qning.greporter.greporter.core.ComputingNode;
import com.lenss.qning.greporter.topology.Processor;
import com.lenss.yzeng.utils.FaceDetectionWrapper;
import com.lenss.yzeng.utils.FaceDetector;
import com.lenss.yzeng.utils.Serializer;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by yukun on 12/18/2016.
 */

public class FaceDetectionProcessor extends Processor {
    @Expose
    private boolean isFaceObjExist = false;

    @Expose
    private boolean fpFeatureSupported = false;

    @Expose
    private FaceDetector faceDetector = null;

    @Expose
    public final int confidence_value = 58;
    // the frame data length of a frame from onPreviewFrame()
    //public final int frameLength = 1474560;

    private void setupFaceProc(){
        if (!isFaceObjExist) {
            // Check to see if the FacialProc feature is supported in the device or no.
            fpFeatureSupported = FacialProcessing
                    .isFeatureSupported(FacialProcessing.FEATURE_LIST.FEATURE_FACIAL_PROCESSING);

            // I think we don't have to check faceProc again here
            if (fpFeatureSupported && faceDetector == null) {
                Log.e("TAG", "Feature is supported");
                FacialProcessing faceProc = FacialProcessing.getInstance();  // Calling the Facial Processing Constructor.
                faceProc.setRecognitionConfidence(confidence_value);
                // we might wanna change the mode to FP_MODE_STILL to increase computing demand
                faceProc.setProcessingMode(FacialProcessing.FP_MODES.FP_MODE_VIDEO);
                faceDetector = new FaceDetector(faceProc);
            } else {
                Log.e("TAG", "Feature is NOT supported");
                return;
            }
        }
    }

    @Override
    public void prepare(){
        setupFaceProc();
    }

    @Override
    public void execute(){
        byte[] data = null;
        FaceDetector.FrameData frameData = null;
        ByteBuffer buffer = ByteBuffer.allocate(1474976);
        long count = 0;
        // we assume that getData will get one data passed by emit() at a time
        // which shall be one frame data in onPreviewFrame(), then we can do face detection using that data
        while (!Thread.interrupted()){
            data = getData(getTaskID());
            if (data != null) {
                int bufRem = buffer.remaining();
                int length = data.length;
                // if the buffer remains unfilled after putting new data
                if (length < bufRem){
                    buffer.put(data);
                }
                // if the buffer happen to be full after putting new data
                else{
                    buffer.put(data, 0, bufRem);
                    Log.e("FaceDP.execute", count + "th frame data received!");
                    long receivedTime = System.currentTimeMillis();
                    try {
                        frameData = (FaceDetector.FrameData) Serializer.deserialize(buffer.array());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    FaceDetectionWrapper fdw = faceDetector.detectFaces(frameData);
                    Log.e("FaceDP.execute", count + "th frame data processed in " + (System.currentTimeMillis() - receivedTime) + " ms");
                    count ++;
                    buffer.clear();
                    if (length > bufRem){
                        buffer.put(data, bufRem, length - bufRem);
                    }
                }
            }
        }
        // yukun: simplified version of the above commented code
//        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
//            landScapeMode = true;
//        else
//            landScapeMode = false;

        //cameraObj.setDisplayOrientation(displayAngle);

        // from here the commented codes are original (unwrapped) for face detection
        //faceProc.setFrame(data, previewSize.width, previewSize.height, false, angleEnum);
//        FaceDetectionWrapper faceWrapper = faceDetector.detectFaces(data, previewSize.width, previewSize.height, angleEnum);
//        // ends here
//        if (faceWrapper == null)
//        {
//            Log.d(TAG, "Face Wrapper Null, We Draw Nothing!");
//            if (drawView != null) {
//                preview.removeView(drawView);
//
//                drawView = new DrawView(this, null, false, 0, 0, null, landScapeMode);
//                preview.addView(drawView);
//            }
//            canvas.drawColor(0, Mode.CLEAR);
//            setUI(0, 0, 0, 0, 0, 0, 0, null, 0, 0);
//        }
//        else{
//            FaceData[] faceArray = faceWrapper.getFaceArray();// Array in which all the face data values will be returned for each face detected.
//            int numFaces = faceWrapper.getNumFaces();
//            Log.d("TAG", "Face Detected");
//            // yukun: is this useful?
//            faceProc.normalizeCoordinates(surfaceWidth, surfaceHeight);
//            preview.removeView(drawView);// Remove the previously created view to avoid unnecessary stacking of Views.
//            drawView = new DrawView(this, faceArray, true, surfaceWidth, surfaceHeight, cameraObj, landScapeMode);
//            preview.addView(drawView);
//
//            for (int j = 0; j < numFaces; j++) {
//                smileValue = faceArray[j].getSmileValue();
//                leftEyeBlink = faceArray[j].getLeftEyeBlink();
//                rightEyeBlink = faceArray[j].getRightEyeBlink();
//                faceRollValue = faceArray[j].getRoll();
//                gazePointValue = faceArray[j].getEyeGazePoint();
//                pitch = faceArray[j].getPitch();
//                yaw = faceArray[j].getYaw();
//                horizontalGaze = faceArray[j].getEyeHorizontalGazeAngle();
//                verticalGaze = faceArray[j].getEyeVerticalGazeAngle();
//            }
//            setUI(numFaces, smileValue, leftEyeBlink, rightEyeBlink, faceRollValue, yaw, pitch, gazePointValue,
//                    horizontalGaze, verticalGaze);
//        }
    }

    @Override
    public void postExecute(){
        this.faceDetector.release();
    }

    private  byte[] getData(int taskID)
    {
        //Log.e("FaceDP.getData", "Doing retrieveIncomingQueue");
        Pair<Long, byte[]> frameDataPair = ComputingNode.retrieveIncomingQueue(taskID);
        if (frameDataPair != null) {
            return frameDataPair.second;
        }
        else {
            //Log.e("FaceDP.getData", "Got null frameDataPair");
            return null;
        }
    }
}
