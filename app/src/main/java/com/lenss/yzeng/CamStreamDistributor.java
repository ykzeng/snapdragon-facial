package com.lenss.yzeng;

import android.hardware.camera2.params.Face;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.util.Log;
import android.util.Pair;

import com.google.gson.annotations.Expose;
import com.lenss.qning.greporter.greporter.core.ComputingNode;
import com.lenss.qning.greporter.topology.Distributor;
import com.lenss.yzeng.utils.FaceDetector;
import com.lenss.yzeng.utils.Serializer;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 * Created by yukun on 12/22/2016.
 */

public class CamStreamDistributor extends Distributor {
    // variables inherited from MyDistributor
    @Expose
    private final int BUF_SIZE = 1024 * 1024;

    @Expose
    private final String LOCAL_ADDRESS = "com.android.greporter";

    @Expose
    private FaceDetector faceDetector = null;

    @Expose
    private boolean isFaceObjExist = false;

    @Expose
    private boolean fpFeatureSupported = false;

    @Expose
    private final int confidence_value = 58;

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
        //Log.e("Distributor.prepare", "entering the prepare function");
        //Obviously we have entered here and started the thread for pulling stream from local address
        new Thread(new PullStreamThreadLocal()).start();
        setupFaceProc();
    }

    @Override
    public void execute(){
        // whether we still needs to find GoP here?
        // 1st, we ignore GoP and do one frame at a time
        //Log.e("Distributor.execute", "Entering the execute function");
        int count = 0;
        while(!Thread.currentThread().isInterrupted()){
            //Log.e("Distributor.execute", "Entering the while loop");
            //we have confirmed that the program will go before the next sentence
            Pair<Long, byte[]> incomingQ = ComputingNode.retrieveIncomingQueue(getTaskID());
            if (incomingQ != null) {
                //Log.e("Distributor.execute", "Data retrieved from incoming queue: " + "first-" + incomingQ.first);
                try {
                    ComputingNode.emit(incomingQ.first, incomingQ.second, getTaskID(), FaceDetectionProcessor.class.getName());
                    //Log.e("Distributor.execute", "Emitting the " + count + "th byte array!");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                count++;
            }
        }
    }

    // connect to com.android.greporter
    // retrieve data from it and distribute into ComputingNode
    public class PullStreamThreadLocal implements Runnable {
        private LocalServerSocket serverSocket;
        private LocalSocket clientSocket;
        private ObjectInputStream ois;

        public void run() {
            long inputFrameCount = 0;
            try {
                serverSocket = new LocalServerSocket(LOCAL_ADDRESS);
                System.out.println("************** LocalServerSocket established ****************");
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                clientSocket = serverSocket.accept();
                ois = new ObjectInputStream(clientSocket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    inputFrameCount ++;
                    FaceDetector.FrameData frameObject = (FaceDetector.FrameData)ois.readObject();
                    byte[] frameData = Serializer.serialize(frameObject);
                    ComputingNode.collect(getTaskID(), frameData);
                    long timeBeforeDet = System.currentTimeMillis();
                    faceDetector.detectFaces(frameObject, false);
                    Log.e("Distributor.pull", "The " + inputFrameCount + "th frame is detected in " + (System.currentTimeMillis() - timeBeforeDet) + " ms");
                } catch(IOException e) {
                    e.printStackTrace();
                }catch (ClassNotFoundException e){
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
