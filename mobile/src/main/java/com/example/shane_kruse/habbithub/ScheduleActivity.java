package com.example.shane_kruse.habbithub;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Objects;


public class ScheduleActivity extends AppCompatActivity {
    Toolbar mToolbar;
    Button mondayBtn, tuesdayBtn, wednesdayBtn, thursdayBtn,
            fridayBtn, saturdayBtn, sundayBtn, timePickerBtn,
            everyDayBtn, anytimeBtn, pickTimeBtn;
    Switch repeatSwitch, watchSwitch;
    TimePicker duedatePicker;
    TextView save;
    EditText abbrevText;
    com.shawnlin.numberpicker.NumberPicker dailyNumPicker;
    Context context;
    boolean isEverydaySelected = false;

    private String descr;               //Description of Task
    private int goal;                   //Number of times Task should be completed
    private int prog = 0;               //Current progress towards the goal
    private LocalTime due_date;         //Date/Time that the task must be completed by
    private String icon;                //Icon ID
    private boolean completed = false;  //Has the goal been met
    private boolean repeat;             //On or off to repeat task every interval type
    private String color;               //Color hex
    private boolean on_watch = false;           //Is task on smartwatch
    private String abbrev;              //Abbreviation for smartwatch
    private boolean active = true;      //Task in progress
    private LocalDateTime time_completed = null;

    ArrayList<Button> buttonList;

    protected void onCreate (Bundle savedInstance) {
        // Initialize layout
        super.onCreate(savedInstance);

        context = getApplicationContext();

        setContentView(R.layout.activity_schedule_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Initialize toolbar
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");

        // Save Button
        save = findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get list of days selected
                ArrayList<String> intervalList = new ArrayList<>();
                for (Button b : buttonList) {
                    if (b.isSelected()) intervalList.add(b.getText().toString());
                }

                // Set the time the task is due
                int hour = 23;
                int minute = 59;

                due_date = LocalTime.of(hour, minute);

                // Set goal from numberpicker
                goal = dailyNumPicker.getValue();

                // Check if scheduling info was entered

                if (intervalList.isEmpty() || due_date == null) {
                    showPopup();
                    return;
                }


                // Update the smartwatch
                String newTaskMsg = "";
                System.out.println("Task assinged to watch: " + on_watch);
                // Send added task to watch if on watch selected
                if (on_watch) {
                    newTaskMsg = getString(R.string.new_smartwatch_task);
                }

                // Add to database
                DbHandler hand = new DbHandler(ScheduleActivity.this);
                hand.insertTask(descr, goal, prog, due_date, icon, completed,
                                intervalList, repeat, color, on_watch, abbrev);

                // Return to dashboard
                Intent i  = new Intent(ScheduleActivity.this, MainActivity.class);
                i.putExtra("taskMsg", newTaskMsg);
                startActivity(i);
            }
        });

        // Back button
        TextView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        everyDayBtn = findViewById(R.id.interval_everyday_btn);
        everyDayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEverydaySelected = true;
                clearWeekdayBtns(buttonList);
                everyDayBtn.setBackground(getDrawable(R.drawable.rounded_schedule_btn_selected));
            }
        });

        // Switches
        repeatSwitch = findViewById(R.id.repeat_switch);
        repeatSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    repeat = true;
                } else {
                    repeat = false;
                }
            }
        });

        watchSwitch = findViewById(R.id.watch_task_switch);
        watchSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    on_watch = true;
                    LayoutInflater inflater = getLayoutInflater();
                    View popup = inflater.inflate(R.layout.abbrev_popup, null);
                    final AlertDialog alert = new AlertDialog.Builder(ScheduleActivity.this).create();
                    alert.setView(popup);

                    abbrevText = popup.findViewById(R.id.abbrev_text);
                    Button okBtn = popup.findViewById(R.id.ok_button);
                    okBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (String.valueOf(abbrevText.getText()).length() < 6) {
                                abbrev = String.valueOf(abbrevText.getText());
                                alert.dismiss();
                            }
                        }
                    });
                    alert.show();

                } else on_watch = false;
            }
        });

        // TimePicker
        duedatePicker = findViewById(R.id.time_picker);

        // Setup day buttons
        buttonList = new ArrayList<>();

        sundayBtn = findViewById(R.id.interval_sunday_btn);
        buttonList.add(sundayBtn);

        mondayBtn = findViewById(R.id.interval_monday_btn);
        buttonList.add(mondayBtn);

        tuesdayBtn = findViewById(R.id.interval_tuesday_btn);
        buttonList.add(tuesdayBtn);

        wednesdayBtn = findViewById(R.id.interval_wednesday_btn);
        buttonList.add(wednesdayBtn);

        thursdayBtn = findViewById(R.id.interval_thursday_btn);
        buttonList.add(thursdayBtn);

        fridayBtn = findViewById(R.id.interval_friday_btn);
        buttonList.add(fridayBtn);

        saturdayBtn = findViewById(R.id.interval_saturday_btn);
        buttonList.add(saturdayBtn);

        timePickerBtn = findViewById(R.id.pick_time_btn);
        timePickerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        // Setup Buttons
        setupIntervalBtn(buttonList);

        // Setup NumberPickers
        dailyNumPicker = findViewById(R.id.num_picker);

        // Enable animated layout changes
        ((ViewGroup) findViewById(R.id.schedule_layout)).getLayoutTransition()
                .enableTransitionType(LayoutTransition.CHANGING);

        // Pick time button
        pickTimeBtn = findViewById(R.id.pick_time_btn);
        pickTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePopup();
            }
        });

        // Read Task info from create page
        Bundle data = getIntent().getExtras();
        descr = (String) data.get("desc");
        icon = (String) data.get("icon");
        color = (String) data.get("hex");
    }

    void showPopup() {
        LayoutInflater inflater = getLayoutInflater();
        View popup = inflater.inflate(R.layout.not_completed_popup, null);
        final AlertDialog alert = new AlertDialog.Builder(this).create();
        alert.setView(popup);

        Button dismissBtn = popup.findViewById(R.id.dismissBtn);
        dismissBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });

        alert.show();
    }

    void showTimePopup() {
        LayoutInflater inflater = getLayoutInflater();
        View popup = inflater.inflate(R.layout.time_popup_daily, null);
        final AlertDialog alert = new AlertDialog.Builder(this).create();
        alert.setView(popup);

        duedatePicker = popup.findViewById(R.id.time_picker);

        anytimeBtn = popup.findViewById(R.id.anytimeBtn);
        anytimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(anytimeBtn.isSelected()) {
                    anytimeBtn.setSelected(false);
                    anytimeBtn.setBackgroundColor(Color.WHITE);
                }
                else {
                    anytimeBtn.setSelected(true);
                    anytimeBtn.setBackgroundColor(Color.GREEN);
                }
            }
        });

        Button saveBtn = popup.findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });

        alert.show();
    }

    void setupIntervalBtn(ArrayList<Button> buttons) {
        for (int i = 0; i < buttons.size(); i++) {
            final Button b = buttons.get(i);

            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { // TODO fix button not deselecting!!!!
                    if (isEverydaySelected) {
                        everyDayBtn.setBackground(getDrawable(R.drawable.rounded_btn_schedule));
                    }
                    b.setBackground(getDrawable(R.drawable.weekday_selected));
                    
                    if (b.isSelected()) b.setSelected(false);
                    else b.setSelected(true);
                }
            });
        }
    }

    // Set weekday buttons back to default
    void clearWeekdayBtns(ArrayList<Button> buttons) {
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setBackground(getDrawable(R.drawable.round_weekday_btn));
        }
    }
}
