package com.valdesekamdem.android.tcheckmobile;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.valdesekamdem.android.tcheckmobile.Utils.Constants;

public class StudentDetailsActivity extends AppCompatActivity {

    private TextView firstNameText;
    private TextView lastNameText;
    private TextView phoneNumberText;
    private TextView emailText;
    private TextView levelText;
    private TextView commentsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_details);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firstNameText = (TextView) findViewById(R.id.first_name_text);
        lastNameText = (TextView) findViewById(R.id.last_name_text);
        phoneNumberText = (TextView) findViewById(R.id.phone_number_text);
        emailText = (TextView) findViewById(R.id.email_text);
        levelText = (TextView) findViewById(R.id.level_text);
        commentsText = (TextView) findViewById(R.id.comments_text);

        String firstName = getIntent().getStringExtra(Constants.FIELD_FIRST_NAME);
        String lastName = getIntent().getStringExtra(Constants.FIELD_LAST_NAME);
        String phoneNumber = getIntent().getStringExtra(Constants.FIELD_PHONE_NUMBER);
        String email = getIntent().getStringExtra(Constants.FIELD_EMAIL);
        String level = getIntent().getStringExtra(Constants.FIELD_LEVEL);
        String comments = getIntent().getStringExtra(Constants.FIELD_COMMENTS);

        //
        firstNameText.setText(firstName);
        lastNameText.setText(lastName);
        phoneNumberText.setText(phoneNumber);
        emailText.setText(email);
        levelText.setText(level);
        commentsText.setText(comments);
    }

}
