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
 * A simple {@link Fragment} subclass.
 */
public class StudentListFragment extends Fragment {

    private final static String TAG = StudentListFragment.class.getSimpleName();
    public final static String URL = "https://api.mlab.com/api/1/databases/heroku_nn0grzjm/collections/students?apiKey=J3RhSNdvzMfrO5pp2sgYmyMz6FAuraLi";

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

        progressLayout = (LinearLayout) view.findViewById(R.id.progress_layout);
        studentsListView = (ListView) view.findViewById(R.id.students_listview);

        new LoadStudents().execute();
        showProgress();

        return view;
    }


    private void showProgress() {
        progressLayout.setVisibility(View.VISIBLE);
        studentsListView.setVisibility(View.GONE);
    }

    private void hideProgress() {
        progressLayout.setVisibility(View.GONE);
        studentsListView.setVisibility(View.VISIBLE);
    }

    private void setStudentListView(List<Student> students) {

        if (students != null) {
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
            if (getActivity() != null) Toast.makeText(getActivity().getApplicationContext(), "Aucun étudiant", Toast.LENGTH_LONG).show();
        }

//        String s = students != null ? students.toString() : "Erreur lors du chargement des étudiants";
//        if (getActivity() != null) Toast.makeText(getActivity().getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

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

    private class LoadStudents extends AsyncTask<Void, Integer, String> {
        @Override
        protected String doInBackground(Void... voids) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr;

            try {
                URL url = new URL(URL);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();

                //if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_ACCEPTED) {
                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null) {
                        // Nothing to do.
                        return null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                        // But it does make debugging a *lot* easier if you print out the completed
                        // buffer for debugging.
                        buffer.append(line + "\n");
                    }

                    if (buffer.length() == 0) {
                        // Stream was empty.  No point in parsing.
                        return null;
                    }
                    forecastJsonStr = buffer.toString();

                    return forecastJsonStr;

                //}

                //return null;

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
            List<Student> students = null;
            hideProgress();
            if (s != null) {
                Gson gson = new Gson();
                students = gson.fromJson(s, new TypeToken<List<Student>>(){}.getType());
            }
            setStudentListView(students);
        }
    }

}
