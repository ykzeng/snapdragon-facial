package com.lenss.yzeng;

import android.net.LocalServerSocket;
import android.net.LocalSocket;

import com.lenss.qning.greporter.greporter.core.ComputingNode;
import com.lenss.qning.greporter.topology.Distributor;

import java.io.IOException;
import java.io.InputStream;

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
        byte[] frames = new byte[BUF_SIZE];
        int datalen = 0;
        int rem_pics = 0;

        while (!Thread.currentThread().isInterrupted()) {
            // get new frame data from incoming queue and add into to remaining frame data
            byte[] frameData = ComputingNode.retrieveIncomingQueue(getTaskID());
            if (frameData!=null) {
                System.arraycopy(frameData, 0, frames, rem_pics, frameData.length);
                datalen += frameData.length;

                // find GoP in frames, and emit them into outgoing queue
                int rec = datalen;
                int count = 0;
                int idr_count = 0;
                int cur_idr = 0;
                int last_start = 0;
                for (int i = 0; i < rec - 4; i++) {
                    if (frames[i] == 0 && frames[i + 1] == 0 && frames[i + 2] == 0 && frames[i + 3] == 1 && (frames[i + 4] & 0x1f) == 5) {
                        last_start = i;
                        break;
                    }
                }
                for (int i = last_start; i < rec - 3; i++) {
                    if (frames[i] == 0 && frames[i + 1] == 0 && frames[i + 2] == 0 && frames[i + 3] == 1) {
                        count++;
                        if ((frames[i + 4] & 0x1f) == 5) {
                            idr_count++;
                            cur_idr++;
                            if (cur_idr == 1) {
                                cur_idr = 0;
                                if (i - last_start > 0) {
                                    byte[] gop = new byte[i - last_start];
                                    System.arraycopy(frames, last_start, gop, 0, i - last_start);
                                    last_start = i;
                                    try {
                                        String component = FaceDetector.class.getName();
                                        if(gop!=null) {
                                            ComputingNode.emit(gop, getTaskID(), component);
                                        }
                                        System.out.println("emit data ********************************************************");
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
                // delete the frame data already emitted
                datalen -= last_start;
                System.arraycopy(frames, last_start, frames, 0, datalen);
                rem_pics = datalen;
            }
        }
    }

    public void postExecute(){

    }

    // connect to com.android.greporter
    // retrieve data from it and distribute into ComputingNode
    class PullStreamThreadLocal implements Runnable {
        private LocalServerSocket serverSocket;
        private LocalSocket clientSocket;
        private InputStream input;
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
                input = clientSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    int dataReaded = input.read(buf);
                    if (dataReaded > 0) {
                        byte[] frameData = new byte[dataReaded];
                        System.arraycopy(buf, 0, frameData, 0, dataReaded);
                        try {
                            ComputingNode.collect(getTaskID(),frameData);
                            System.out.println("collect frames ****************************************************"+dataReaded);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
