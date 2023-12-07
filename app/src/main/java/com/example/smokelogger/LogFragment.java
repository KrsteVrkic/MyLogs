package com.example.smokelogger;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class LogFragment extends Fragment {

    private DatabaseLogger dbLogger;
    private int counter = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log, container, false);
        dbLogger = new DatabaseLogger(requireContext());

        Button logSmokeButton = view.findViewById(R.id.btnSmoke);
        logSmokeButton.setOnClickListener(v -> logEventToDb(Events.SMOKE_BREAK));

        Button logLateButton = view.findViewById(R.id.btnLate);
        logLateButton.setOnClickListener(v -> logEventToDb(Events.LATE));

        Button logHomeButton = view.findViewById(R.id.btnHome);
        logHomeButton.setOnClickListener(v -> logEventToDb(Events.WORK_HOME));

        Button testButton = view.findViewById(R.id.btnTest);
        testButton.setOnClickListener(v -> logTestEventToDb());

        return view;
    }

    private void logEventToDb(Events event) {
        SQLiteDatabase db = dbLogger.getWritableDatabase();

        db.execSQL("INSERT INTO log_table (date, event) VALUES (?, ?)",
                new Object[]{getCurrentDate(), event.name()});
        Toast.makeText(requireContext(), "Logged: " + event.toString(), Toast.LENGTH_SHORT).show();
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void logTestEventToDb() {
        SQLiteDatabase db = dbLogger.getWritableDatabase();
        Events testEvent = Events.TEST;

        db.execSQL("INSERT INTO log_table (date, event) VALUES (?, ?)",
                new Object[]{getNextDate(counter++), testEvent.name()});
        Toast.makeText(requireContext(), "Logged: " + testEvent.toString(), Toast.LENGTH_SHORT).show();
    }

    private String getNextDate(int daysToAdd) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, daysToAdd);
        return sdf.format(calendar.getTime());
    }
}