package com.valdesekamdem.android.tcheckmobile;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.valdesekamdem.android.tcheckmobile.Utils.Constants;
import com.valdesekamdem.android.tcheckmobile.model.Student;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Le {@link Fragment} qui permet de créer un nouvelle étudiant.
 */
public class AddStudentFragment extends Fragment {

    private final static String TAG = AddStudentFragment.class.getSimpleName();

    private TextInputEditText firstNameText;
    private TextInputEditText lastNameText;
    private TextInputEditText phoneNumberText;
    private TextInputEditText emailText;
    private AppCompatSpinner levelSpinner;
    private TextInputEditText commentsText;
    private LinearLayout progressBar;

    private Button saveBtn;

    String[] levels = new String[]{"1", "2", "3", "4", "5"};    // liste des niveaux d'études possible

    public AddStudentFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_student, container, false);

        //  Renseigne les valeurs des variables définies plus haut.
        firstNameText = (TextInputEditText) view.findViewById(R.id.first_name_text);
        lastNameText = (TextInputEditText) view.findViewById(R.id.last_name_text);
        phoneNumberText = (TextInputEditText) view.findViewById(R.id.phone_number_text);
        emailText = (TextInputEditText) view.findViewById(R.id.email_text);
        levelSpinner = (AppCompatSpinner) view.findViewById(R.id.level_spinner);
        commentsText = (TextInputEditText) view.findViewById(R.id.comments_text);
        saveBtn = (Button) view.findViewById(R.id.save_btn);
        progressBar = (LinearLayout) view.findViewById(R.id.progress_bar);

        //  Met les valeurs du Spinner qui contient les niveaux d'etude.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, levels);
        levelSpinner.setAdapter(adapter);

        //  Ecoute le clique sur le bouton de sauvegarde de l'étudiant et appelle la méthode de sauvegarde.
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveStudent();
            }
        });

        return view;
    }

    /**
     * Permet de sauveger un s
     */
    private void saveStudent() {
        if (!validate()) return;

        String firstName = firstNameText.getText().toString();
        String lastName = lastNameText.getText().toString();
        String phoneNumber = phoneNumberText.getText().toString();
        String email = emailText.getText().toString();
        String level = (String) levelSpinner.getSelectedItem();
        String comments = commentsText.getText().toString();

        Student student = new Student(firstName, lastName, email, phoneNumber, level, comments);
        new SaveStudentTask().execute(student);
        showProgress();
    }

    /**
     * Appelée lorsque l'étudiant est sauvegardé avec succès.
     *
     * @param student   le nouvel étudiant.
     */
    private void onSaveSuccess(Student student) {
        hideProgress();

        firstNameText.setText(null);
        lastNameText.setText(null);
        phoneNumberText.setText(null);
        emailText.setText(null);
        commentsText.setText(null);

        if (getActivity() != null) {
            Toast.makeText(getActivity(), "Etudiant sauvegardé avec succès !", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Appelé lorsqu'une erreur est survenue lors de la sauvegarde de l'étudiant
     *
     * @param errorMessage  Le message d'erreur.
     */
    private void onSaveError(String errorMessage) {
        hideProgress();
        if (getActivity() != null) {
            Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Permet de valider les données entrées par l'utilisateur.
     *
     * @return  TRUE si les données sont valides ou FAUX sinon.
     */
    private boolean validate() {
        boolean valid = true;
        String firstName = firstNameText.getText().toString();
        String lastName = lastNameText.getText().toString();
        String phoneNumber = phoneNumberText.getText().toString();
        String email = emailText.getText().toString();

        if (firstName.isEmpty() || firstName.length() < 2) {
            firstNameText.setError("Le prénom doit avoir 2 caractères au minimum");
            valid = false;
        } else {
            firstNameText.setError(null);
        }

        if (lastName.isEmpty() || lastName.length() < 2) {
            lastNameText.setError("Le nom doit avoir 2 caractères au minimum");
            valid = false;
        } else {
            lastNameText.setError(null);
        }

        if (phoneNumber.isEmpty() || phoneNumber.length() != 9) {
            phoneNumberText.setError("Entrer un numéro de téléphone valide !");
            valid = false;
        } else {
            phoneNumberText.setError(null);
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("Entrer une adresse mail valide !");
            valid = false;
        } else {
            emailText.setError(null);
        }

        return valid;
    }

    /**
     * Affiche le {@link ProgressBar} qui montre que la sauvegarde est belle et bien en cours,
     * et cache le bouton de sauvegarde pour prevenir un éventuel clique.
     */
    private void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
        saveBtn.setVisibility(View.GONE);
    }

    /**
     * Cache le {@link ProgressBar} qui montre que la sauvegarde est en cours,
     * et affiche le bouton de sauvegarde pour une prochaine sauvegarde.
     */
    private void hideProgress() {
        progressBar.setVisibility(View.GONE);
        saveBtn.setVisibility(View.VISIBLE);
    }

    /**
     * Cette classe permet de communiquer avec le serveur d'une facon asynchrone afin de sauvegarder le nouvel étudiant.
     * La comminucation est faite par le biais de la classe Java {@link HttpURLConnection}
     */
    private class SaveStudentTask extends AsyncTask<Student, Integer, String> {
        @Override
        protected String doInBackground(Student... students) {

            // Ces deux variables sont déclarées hors du try/catch
            // pour qu'ils puissent être fermées dans le bloque finally.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // L'URL complete pour la sauvegarde.
            String uri = Constants.URL + "?apiKey=" + Constants.API_KEY;
            // Conversion de l'objet Etudiant en JSON brut pour un avoir un format de données compréhensible par le serveur.
            String studentJson = new Gson().toJson(students[0]);

            // Doit contenir la reponse JSON brute comme String.
            String forecastJsonStr;

            try {
                URL url = new URL(uri);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("charset", "utf-8");

                DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
                wr.writeBytes(studentJson);
                wr.flush();

                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                forecastJsonStr = buffer.toString();

                return forecastJsonStr;

            } catch (java.io.IOException e) {
                Log.e(TAG, "Error when loading Students", e);
                return null;
            } finally {
                if (urlConnection != null) urlConnection.disconnect();
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error when closing Stream", e);
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Student student;
            hideProgress();
            if (s != null) {
                student = new Gson().fromJson(s, Student.class);
                onSaveSuccess(student);
            } else
                onSaveError("Une erreur s'est porduite lors de la suvegarde");
        }
    }

}
