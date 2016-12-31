package com.lenss.yzeng;

import android.net.LocalServerSocket;
import android.net.LocalSocket;

import com.lenss.qning.greporter.greporter.core.ComputingNode;
import com.lenss.qning.greporter.topology.Distributor;
import com.lenss.yzeng.utils.FaceDetector;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 * Created by yukun on 12/22/2016.
 */

public class CamStreamDistributor extends Distributor {
    // variables inherited from MyDistributor
    private static final int BUF_SIZE = 1024 * 1024;
    private static final String LOCAL_ADDRESS = "com.android.greporter";

    public void prepare(){
        new Thread(new PullStreamThreadLocal()).start();
    }

    public void execute(){
        // whether we still needs to find GoP here?
        // TODO 1st, we ignore GoP and do one frame at a time
        byte[] frameData = ComputingNode.retrieveIncomingQueue(getTaskID());
        try{
            ComputingNode.emit(frameData, getTaskID(), FaceDetectionProcessor.class.getName());
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    public void postExecute(){

    }

    // connect to com.android.greporter
    // retrieve data from it and distribute into ComputingNode
    class PullStreamThreadLocal implements Runnable {
        private LocalServerSocket serverSocket;
        private LocalSocket clientSocket;
        private ObjectInputStream ois;
        private byte[] buf = new byte[BUF_SIZE];

        public void run() {
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
                    byte[] frameData = frameObject.getData();
                    ComputingNode.collect(getTaskID(), frameData);
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
