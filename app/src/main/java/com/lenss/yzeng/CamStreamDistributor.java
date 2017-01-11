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

    @Override
    public void prepare(){
        //Log.e("Distributor.prepare", "entering the prepare function");
        //Obviously we have entered here and started the thread for pulling stream from local address
        new Thread(new PullStreamThreadLocal()).start();
    }

    @Override
    public void execute(){
        // whether we still needs to find GoP here?
        // TODO 1st, we ignore GoP and do one frame at a time
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
                    Log.e("Distributor.execute", "Emitting the " + count + "th byte array!");
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
        private byte[] buf = new byte[BUF_SIZE];

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
                    FaceDetector.FrameData frameObject = (FaceDetector.FrameData)ois.readObject();
                    byte[] frameData = Serializer.serialize(frameObject);
                    FaceDetector.FrameData tmpObject = (FaceDetector.FrameData)Serializer.deserialize(frameData);
                    //byte[] frameData = frameObject.getData();
                    Log.e("Distributor.PullStream", "Collecting" + inputFrameCount + "th FrameData Obj: " + frameObject.toString() + " with " + frameObject.getData().length + " bytes frame data");
                    Log.e("Distributor.PullStream", "After serialization it contains " + frameData.length + " bytes");
                    Log.e("Distributor.PullStream", "Then after deserialization it becomes " + tmpObject.toString() + " with " + tmpObject.getData().length + " bytes frame data");
                    ComputingNode.collect(getTaskID(), frameData);
                    inputFrameCount ++;
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
