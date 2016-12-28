package com.lenss.yzeng;

import android.util.Log;

import com.lenss.qning.greporter.topology.Processor;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing;

/**
 * Created by yukun on 12/18/2016.
 */

public class FaceDetector extends Processor {
    private static boolean isFaceObjExist = false;
    private boolean fpFeatureSupported = false;
    private FacialProcessing faceProc = null;
    public final int confidence_value = 58;

    private void setupFaceProc(){
        if (!isFaceObjExist) {
            // Check to see if the FacialProc feature is supported in the device or no.
            fpFeatureSupported = FacialProcessing
                    .isFeatureSupported(FacialProcessing.FEATURE_LIST.FEATURE_FACIAL_PROCESSING);

            // I think we don't have to check faceProc again here
            if (fpFeatureSupported && faceProc == null) {
                Log.e("TAG", "Feature is supported");
                faceProc = FacialProcessing.getInstance();  // Calling the Facial Processing Constructor.
                faceProc.setRecognitionConfidence(confidence_value);
                // we might wanna change the mode to FP_MODE_STILL to increase computing demand
                faceProc.setProcessingMode(FacialProcessing.FP_MODES.FP_MODE_VIDEO);
            } else {
                Log.e("TAG", "Feature is NOT supported");
                return;
            }
        }
    }

    public void prepare(){
        setupFaceProc();
    }

    public void execute(){

    }
}
