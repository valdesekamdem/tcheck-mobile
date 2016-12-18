package com.valdesekamdem.android.tcheckmobile;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.valdesekamdem.android.tcheckmobile.Utils.Constants;
import com.valdesekamdem.android.tcheckmobile.model.Student;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;


/**
 * Le {@link Fragment} de la liste des étudiants.
 */
public class StudentListFragment extends Fragment {

    private final static String TAG = StudentListFragment.class.getSimpleName();

    private LinearLayout studentsNotFoundLayout;
    private LinearLayout progressLayout;
    private ListView studentsListView;

    public StudentListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_student_list, container, false);

        studentsNotFoundLayout = (LinearLayout) view.findViewById(R.id.students_not_found_layout);
        progressLayout = (LinearLayout) view.findViewById(R.id.progress_layout);
        studentsListView = (ListView) view.findViewById(R.id.students_listview);

        //  Si l'utilisateur clique sur la fenêtre qui s'affiche lorsque aucun n'etudiant n'est trouvé,
        //  celui la requete est reexecutée pour charger les données du serveur.
        studentsNotFoundLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadStudents();
            }
        });

        loadStudents();     // Chargement la liste des etudians au premier demarage du fragment.

        return view;
    }

    /**
     * Lance l'execution du chargement des données du serveur.
     */
    private void loadStudents() {
        new LoadStudentsTask().execute();
        showProgress();
    }

    /**
     * Affiche le {@link ProgressBar} qui montre que le chargement est belle et bien en cours,
     * et cache la liste des étudiants
     */
    private void showProgress() {
        progressLayout.setVisibility(View.VISIBLE);
        studentsListView.setVisibility(View.GONE);
        studentsNotFoundLayout.setVisibility(View.GONE);
    }

    /**
     * Cache le {@link ProgressBar} qui montre que le chargement est en cours,
     * et montre la liste des etudiants.
     */
    private void hideProgress() {
        progressLayout.setVisibility(View.GONE);
        studentsListView.setVisibility(View.VISIBLE);
    }

    /**
     * Appelée lorsque le chargement de la liste des étudiants du serveur s'est éffectué avec succès.
     * @param students  Les étudiants provenants du serveur.
     */
    private void onLoadSuccess(List<Student> students) {
        if (!students.isEmpty() && getActivity() != null) {
            ArrayAdapter<Student> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, students);
            studentsListView.setAdapter(adapter);
            studentsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Student selectedStudent = (Student) studentsListView.getItemAtPosition(i);
                    showStudentDetails(selectedStudent);
                }
            });
        } else {
            studentsNotFoundLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Appelée lorsque le chargement de la liste des étudiants du serveur a échoué,
     * pour une raison ou une autre.
     * @param errorMessage  Le message d'erreur.
     */
    private void onLoadError(String errorMessage) {
        if (getActivity() != null) Toast.makeText(getActivity().getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
    }

    /**
     * Appelle l'activité {@link StudentDetailsActivity} qui présente les détails de l'étudiant.
     * Elle utilise {@link Intent} pour passer les valeurs de l'étudiant dans l'autre vue.
     *
     * @param student   L'étudiant à présenter les détails.
     */
    private void showStudentDetails(Student student) {
        Intent intent = new Intent(getActivity().getApplicationContext(), StudentDetailsActivity.class);
        intent.putExtra(Constants.FIELD_FIRST_NAME, student.getFirstName());
        intent.putExtra(Constants.FIELD_LAST_NAME, student.getLastName());
        intent.putExtra(Constants.FIELD_PHONE_NUMBER, student.getPhoneNumber());
        intent.putExtra(Constants.FIELD_EMAIL, student.getEmail());
        intent.putExtra(Constants.FIELD_LEVEL, student.getLevel());
        intent.putExtra(Constants.FIELD_COMMENTS, student.getComments());
        startActivity(intent);
    }

    /**
     * Cette classe permet de communiquer avec le serveur d'une facon asynchrone afin de charger la liste des étudiants.
     * La comminucation est faite par le biais de la classe Java {@link HttpURLConnection}
     */
    private class LoadStudentsTask extends AsyncTask<Void, Integer, String> {
        @Override
        protected String doInBackground(Void... voids) {

            // Ces deux variables sont déclarées hors du try/catch
            // pour qu'ils puissent être fermées dans le bloque finally.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String sort = "s={\"firstName\":1}";
            String uri = Constants.URL + "?" + sort + "&apiKey=" + Constants.API_KEY;

            // Doit contenir la reponse JSON brute comme String.
            String studentJsonStr;

            try {
                URL url = new URL(uri);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("charset", "utf-8");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    //  Le buffer est vide. Rien a analyser.
                    return null;
                }
                studentJsonStr = buffer.toString();
                return studentJsonStr;

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
            List<Student> students;
            hideProgress();
            if (s != null) {
                Gson gson = new Gson();
                students = gson.fromJson(s, new TypeToken<List<Student>>(){}.getType());
                onLoadSuccess(students);
            } else {
                onLoadError("Une erreur s'est produite lors du chargement des données");
            }
        }
    }

}
