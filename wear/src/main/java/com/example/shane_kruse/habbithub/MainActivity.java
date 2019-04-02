package com.example.shane_kruse.habbithub;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

public class MainActivity extends WearableActivity {

    // ShaneDev branch
    // Shane Kruse

    private PieChart pieChart;
    private PieData pieData;
    private ConstraintLayout background;
    private Handler myHandler; // was protected

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Enables Always-on
        setAmbientEnabled();

        //Create a message handler//
        myHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Bundle stuff = msg.getData();
                return true;
            }
        });

        // Send message to phone to sync related tasks
        new NewThread("/my_path", "REQUEST_UPDATE").start();

        //Register to receive local broadcasts, which we'll be creating in the next step//
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        Receiver messageReceiver = new Receiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);

        //createPieChart();

    }

    //Define a nested class that extends BroadcastReceiver//
    public class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            if (message.length() > 1) {
                message = message.substring(0, message.length() - 1);
                System.out.println("Watch received: " + message);
                parseUpdate(message);
            } else {
                parseUpdate(message);
            }
        }
    }

    class NewThread extends Thread {
        String path;
        String message;

        //Constructor for sending information to the Data Layer//
        NewThread(String p, String m) {
            path = p;
            message = m;
        }

        public void run() {

            System.out.println("Sending message to phone");
            //Retrieve the connected devices, known as nodes//
            Task<List<Node>> mobileDeviceList =
                    Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();
            try {

                List<Node> nodeList = Tasks.await(mobileDeviceList);
                for (Node n : nodeList) {
                    Task<Integer> sendMessageTask =

                            //Send the message//
                            Wearable.getMessageClient(MainActivity.this).sendMessage(n.getId(), path, message.getBytes());

                    try {

                        //Block on a task and get the result synchronously//
                        Integer result = Tasks.await(sendMessageTask);
                        sendmessage(message);
                        System.out.println(message + " sent");

                        //if the Task fails, then…..//
                    } catch (ExecutionException exception) {
                        //TO DO: Handle the exception//
                    } catch (InterruptedException exception) {
                        //TO DO: Handle the exception//
                    }
                }

            } catch (ExecutionException exception) {
                //TO DO: Handle the exception//
            } catch (InterruptedException exception) {
                //TO DO: Handle the exception//
            }

        }

        //Use a Bundle to encapsulate our message//
        public void sendmessage(String messageText) {
            Bundle bundle = new Bundle();
            bundle.putString("messageText", messageText);
            Message msg = myHandler.obtainMessage();
            msg.setData(bundle);
            myHandler.sendMessage(msg);

        }
    }

    public void parseUpdate(String updateMsg) {
        String[] tasks = updateMsg.split("&");
        String[] xData = new String[tasks.length];
        float[] yData = new float[tasks.length];
        ArrayList<Integer> colors = new ArrayList<>();
        if (tasks[0].contains(",")) {
            for (int i = 0; i < tasks.length; i++) {
                String[] data = tasks[i].split(",");

                // data[0]=name, data[1]=color, data[2]=prog, data[3]=goal
                xData[i] = data[0];
                yData[i] = (float)1/tasks.length;
                colors.add(Color.parseColor(data[1]));
            }
        } else {
            xData[0] = "No current tasks";
            yData[0] = 100.0f;
            colors.add(Color.LTGRAY);
        }

        createPieChart(xData, yData, colors);
}

    public void createPieChart(String[] xData, float[] yData, ArrayList<Integer> colors) {

        background = findViewById(R.id.pie_background);
        pieChart = findViewById(R.id.pie_chart);

        background.setBackgroundColor(Color.parseColor("#F5F5F5"));

        ArrayList<PieEntry> entries = new ArrayList<>();

        for (int i = 0; i < yData.length; i++) {
            entries.add(new PieEntry(yData[i], xData[i]));
        }

        //create the data set
        final PieDataSet pieDataSet = new PieDataSet(entries, null);

        background.setBackgroundColor(Color.parseColor("#F5F5F5"));

        pieDataSet.setSliceSpace(2);
        pieDataSet.setValueTextSize(12);

        pieDataSet.setColors(colors);

        pieChart.setDrawEntryLabels(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setEntryLabelTextSize(18f);
        pieChart.setDrawSlicesUnderHole(false);
        pieChart.setHoleRadius(3f);
        pieChart.setTransparentCircleRadius(4f);

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                PieEntry pe = (PieEntry) e;
                int backColor = pieDataSet.getColor(pieDataSet.getEntryIndex(pe));
                Intent intent = new Intent(MainActivity.this, GoalStatusActivity.class);
                intent.putExtra("task", pe.getLabel());
                intent.putExtra("slice_color", backColor);
                startActivity(intent);

                //Sending a message can block the main UI thread, so use a new thread//
                new NewThread("/my_path", pe.getLabel()).start();

                finish();
            }

            @Override
            public void onNothingSelected() { }
        });

        //create pie data object
        pieData = new PieData(pieDataSet);
        pieDataSet.setDrawValues(false);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }
}
