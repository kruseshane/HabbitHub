package com.example.shane_kruse.habbithub;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.os.CountDownTimer;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GoalStatusActivity extends Activity {

    private TextView goalStatus;
    private TextView taskText;
    private int sliceColor;
    private RelativeLayout taskView;
    private boolean bool;
    private String progStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_status);

        Intent intent = getIntent();
        String task = intent.getStringExtra("task");
        sliceColor = intent.getIntExtra("slice_color", 0);
        bool = intent.getBooleanExtra("updateStatus", false);
        progStatus = intent.getStringExtra("goalStatus");

        taskView = findViewById(R.id.task_view);
        taskText = findViewById(R.id.task_text);
        goalStatus = findViewById(R.id.goal_status);

        taskView.setBackgroundColor(sliceColor);
        taskText.setTextColor(Color.WHITE);
        taskText.setText(task);
        goalStatus.setText(progStatus);

        Timer timer = new Timer(3000, 3000);
        timer.start();
    }

    private class Timer extends CountDownTimer {

        public Timer(long millisLength, long millisUpdate) {
            super(millisLength, millisUpdate);
        }

        @Override
        public void onTick(long l) {

        }

        @Override
        public void onFinish() {
            Intent intent = new Intent(GoalStatusActivity.this, MainActivity.class);
            intent.putExtra("updateStatus", bool);
            startActivity(intent);
            finish();
        }
    }
}
