package com.qualcomm.snapdragon;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.format.Formatter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.lenss.qning.greporter.topology.Topology;
import com.lenss.qning.greporter.topology.mStormSubmitter;
import com.lenss.yzeng.FacialTopology;
import com.lenss.yzeng.utils.Utils;
import com.qualcomm.sdk.smartshutterapp.SmartShutterActivity;
import com.qualcomm.snapdragon.sdk.recognition.sample.FacialRecognitionActivity;
import com.qualcomm.snapdragon.sdk.sample.CameraPreviewActivity;
import com.qualcomm.snapdragon.sdk.sample.GalleryProcessing;
import com.qualcomm.snapdragon.sdk.sample.R;

/**
 * Created by yukun on 12/1/2016.
 */

public class MainActivity extends ActionBarActivity {
    public final static String EXTRA_MSG = "com.qualcomm.snapdragon.sdk.sample.MainActivity";
    public static String MASTER_NODE = "10.0.0.9";
    public static String CLUSTER_ID = "1111";
    public static final String apkFileDirectory = "/storage/emulated/0/Upload";
    private mStormSubmitter submitter;
    public static final String apkFileName = "app-debug.apk";
    public static String localAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        Utils.requestPermisssions(this, new String[]{Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.INTERNET});

        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        localAddress= Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        Button facialProcBtn = (Button) this.findViewById(R.id.button_processing);
        facialProcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CameraPreviewActivity.class);
                intent.putExtra(EXTRA_MSG, "call facial proc sample");
                startActivity(intent);
            }
        });

        Button facialRecoBtn = (Button) this.findViewById(R.id.btn_recog);
        facialRecoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FacialRecognitionActivity.class);
                intent.putExtra(EXTRA_MSG, "call facial recog sample");
                startActivity(intent);
            }
        });

        Button galleryBtn = (Button) this.findViewById((R.id.btn_gallery));
        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GalleryProcessing.class);
                intent.putExtra(EXTRA_MSG, "call gallery processing sample");
                startActivity(intent);
            }
        });

        Button shuttleBtn = (Button) this.findViewById(R.id.btn_shuttle);
        shuttleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SmartShutterActivity.class);
                intent.putExtra(EXTRA_MSG, "call smart shuttle sample");
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.mstorm_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_set_masterIP) {
            // Context context = mapView.getContext();
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            final EditText zkBox = new EditText(this);
            zkBox.setHint("Master Node IP address like: " + MASTER_NODE);
            layout.addView(zkBox);


            alert.setView(layout);
            alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // continue with delete
                    MASTER_NODE = zkBox.getText().toString();
                }
            })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert).show();
        } else if (id == R.id.action_set_clusterId) {
            // Context context = mapView.getContext();
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            final EditText zkBox = new EditText(this);
            zkBox.setHint("ClusterID like: " + CLUSTER_ID);
            layout.addView(zkBox);


            alert.setView(layout);
            alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // continue with delete
                    CLUSTER_ID = zkBox.getText().toString();
                }
            })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert).show();
        } else {
            if((!CLUSTER_ID.equals("xxxx"))) {
                Topology topology = FacialTopology.createTopology();
                submitter = new mStormSubmitter(this,MASTER_NODE, MainActivity.getLocalAddress(),CLUSTER_ID, apkFileDirectory);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                submitter.submitTopology(apkFileName,topology);
            }
            else {
                Toast.makeText(this, "Set MasterNode IP and ClusterID First", Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public static String getLocalAddress(){
        return localAddress;
    }
}
