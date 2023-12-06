package com.example.smokelogger;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class StatsFragment extends Fragment {

    private DatabaseLogger dbLogger;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_stats, container, false);
        dbLogger = new DatabaseLogger(requireContext());

        Button buttonDay = view.findViewById(R.id.btnStatsDay);
        Button buttonWeek = view.findViewById(R.id.btnStatsWeek);
        Button buttonTotal = view.findViewById(R.id.btnStatsTotal);
        Button buttonReset = view.findViewById(R.id.btnStatsReset);

        buttonDay.setOnClickListener(v -> displayStats(view, Events.SMOKE_BREAK, "Days"));
        buttonWeek.setOnClickListener(v -> displayStats(view, Events.SMOKE_BREAK, "Weeks"));
        buttonTotal.setOnClickListener(v -> displayStats(view, Events.SMOKE_BREAK, "Total"));
        buttonReset.setOnClickListener(v -> showResetConfirmationDialog());

        return view;
    }


    private void showResetConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Reset");
        builder.setMessage("Reset all logs?");
        builder.setPositiveButton("Yes", (dialog, which) -> resetDb(requireContext()));
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private String getCurrentDate() {
        return DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.getDefault()).format(new Date());
    }

    private void displayStats(View view, Events event, String timePeriod) {
        if ("Days".equals(timePeriod)) {
            displayStatsForAllDays(view, event);
        } else if ("Weeks".equals(timePeriod)) {
            displayStatsForAllWeeks(view, event);
        } else {
            displayTotalStats(view, event);
        }
    }

    private void displayTotalStats(View view, Events event) {
        StringBuilder result = new StringBuilder();

        SQLiteDatabase db = dbLogger.getReadableDatabase();
        String query = "SELECT COUNT(DISTINCT date) AS totalDays, COUNT(DISTINCT strftime('%W', date)) AS totalWeeks, " +
                "COUNT(*) / COUNT(DISTINCT date) AS avgSmokesPerDay, COUNT(*) / COUNT(DISTINCT strftime('%W', date)) AS avgSmokesPerWeek " +
                "FROM log_table WHERE event = ?";
        String[] selectionArgs = {event.toString()};

        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int totalDays = cursor.getInt(0);
                    int totalWeeks = cursor.getInt(1);
                    int avgSmokesPerDay = cursor.getInt(2);
                    int avgSmokesPerWeek = cursor.getInt(3);

                    result.append("Total Days: ").append(totalDays).append("\n");
                    result.append("Total Weeks: ").append(totalWeeks).append("\n");
                    result.append("Average Smokes Per Day: ").append(avgSmokesPerDay).append("\n");
                    result.append("Average Smokes Per Week: ").append(avgSmokesPerWeek).append("\n");
                }
            } finally {
                cursor.close();
            }
        }

        TextView eventCountText = view.findViewById(R.id.textView_stats);
        eventCountText.setText(result.toString());
    }


    private void displayStatsForAllDays(View view, Events event) {
        StringBuilder result = new StringBuilder();

        SQLiteDatabase db = dbLogger.getReadableDatabase();
        String query = "SELECT date, COUNT(*) FROM log_table WHERE event = ? GROUP BY date";
        String[] selectionArgs = {event.toString()};

        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    String date = cursor.getString(0);
                    int eventCount = cursor.getInt(1);
                    result.append(date).append(": ").append(eventCount).append(" ").append(event.toString()).append("\n");
                }
            } finally {
                cursor.close();
            }
        }

        TextView eventCountText = view.findViewById(R.id.textView_stats);
        eventCountText.setText(result.toString());
    }

    private void displayStatsForAllWeeks(View view, Events event) {
        StringBuilder result = new StringBuilder();

        SQLiteDatabase db = dbLogger.getReadableDatabase();
        String query = "SELECT strftime('%W', date) AS week, COUNT(*) FROM log_table WHERE event = ? GROUP BY week";
        String[] selectionArgs = {event.toString()};

        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    String weekNumber = cursor.getString(0);
                    int eventCount = cursor.getInt(1);
                    result.append("Week ").append(weekNumber).append(": ").append(eventCount).append(" ").append(event.toString()).append("\n");
                }
            } finally {
                cursor.close();
            }
        }

        TextView eventCountText = view.findViewById(R.id.textView_stats);
        eventCountText.setText(result.toString());
    }

    private void resetDb(Context context) {

        SQLiteDatabase db = dbLogger.getWritableDatabase();

        String query = "DELETE FROM log_table";
        db.execSQL(query);
        Toast.makeText(context, "Logs Deleted", Toast.LENGTH_SHORT).show();
    }


    private String getStartOfWeek() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.getDefault());
        return dateFormat.format(cal.getTime());
    }


}