package com.valdesekamdem.android.tcheckmobile;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.valdesekamdem.android.tcheckmobile.Utils.Constants;

/**
 * Cette {@link Activity} affiche les détails d'un Etudiant.
 */
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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // Permet d'afficher l'icone de retour sur la toolbar.

        //  Renseigne les valeurs des variables définies plus haut.
        firstNameText = (TextView) findViewById(R.id.first_name_text);
        lastNameText = (TextView) findViewById(R.id.last_name_text);
        phoneNumberText = (TextView) findViewById(R.id.phone_number_text);
        emailText = (TextView) findViewById(R.id.email_text);
        levelText = (TextView) findViewById(R.id.level_text);
        commentsText = (TextView) findViewById(R.id.comments_text);

        //  Recupe les valeurs de l'étudiant passées sont l'intent.
        String firstName = getIntent().getStringExtra(Constants.FIELD_FIRST_NAME);
        String lastName = getIntent().getStringExtra(Constants.FIELD_LAST_NAME);
        String phoneNumber = getIntent().getStringExtra(Constants.FIELD_PHONE_NUMBER);
        String email = getIntent().getStringExtra(Constants.FIELD_EMAIL);
        String level = getIntent().getStringExtra(Constants.FIELD_LEVEL);
        String comments = getIntent().getStringExtra(Constants.FIELD_COMMENTS);

        //  Renseigne les textes corresponds dans les vues de l'activité.
        firstNameText.setText(firstName);
        lastNameText.setText(lastName);
        phoneNumberText.setText(phoneNumber);
        emailText.setText(email);
        levelText.setText(level);
        commentsText.setText(comments);
    }

}
