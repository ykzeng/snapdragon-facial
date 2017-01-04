package com.lenss.yzeng;

import com.lenss.qning.greporter.topology.Topology;
import com.qualcomm.snapdragon.MainActivity;
import com.qualcomm.snapdragon.sdk.sample.CameraPreviewActivity;

import java.util.ArrayList;

/**
 * Created by yukun on 12/27/2016.
 */

public class FacialTopology {
    public static Topology createTopology(){
        Topology mTopology=new Topology(2);

        // set each component of topology
        CamStreamDistributor csd = new CamStreamDistributor();
        csd.setSourceIP(MainActivity.getLocalAddress());
        mTopology.setDistributor(csd, 1);

        FaceDetectionProcessor fd = new FaceDetectionProcessor();
        fd.setSourceIP(MainActivity.getLocalAddress());
        mTopology.setProcessor(fd, 2, Topology.Local_First);

        // set the relationship between each component in the topology
        ArrayList<Object> downStreamComponents = new ArrayList<Object>();
        downStreamComponents.add(fd);
        mTopology.setDownStreamComponents(csd, downStreamComponents);
        return mTopology;
    }
}
