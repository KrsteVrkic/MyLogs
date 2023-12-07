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

        buttonDay.setOnClickListener(v -> displayStats(view, "Days"));
        buttonWeek.setOnClickListener(v -> displayStats(view, "Weeks"));
        buttonTotal.setOnClickListener(v -> displayStats(view, "Total"));
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

    private void displayStats(View view, String timePeriod) {
        if ("Days".equals(timePeriod)) {
            displayStatsForAllDays(view);
        } else if ("Weeks".equals(timePeriod)) {
            displayStatsForAllWeeks(view);
        } else {
            displayTotalStats(view, Events.SMOKE_BREAK);
        }
    }

    private void displayTotalStats(View view, Events event) {
        StringBuilder result = new StringBuilder();

        SQLiteDatabase db = dbLogger.getReadableDatabase();
        String query = "SELECT COUNT(DISTINCT date) AS totalDays, COUNT(DISTINCT strftime('%W', date)) AS totalWeeks, " +
                "COUNT(*) / COUNT(DISTINCT date) AS avgSmokesPerDay, COUNT(*) / COUNT(DISTINCT strftime('%W', date)) AS avgSmokesPerWeek " +
                "FROM log_table WHERE event = ?";

        String[] selectionArgs = {event.name()};
        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int totalDays = cursor.getInt(0);
                    int totalWeeks = cursor.getInt(1);
                    int avgSmokesPerDay = cursor.getInt(2);
                    int avgSmokesPerWeek = cursor.getInt(3);

                    result.append("Total Days: ").append(totalDays).append("\n");
                    result.append("Total Weeks: ").append(totalWeeks).append("\n\n");
                    result.append("Average Smoke Breaks Per Day: ").append(avgSmokesPerDay).append("\n\n");
                    result.append("Average Smoke Breaks Per Week: ").append(avgSmokesPerWeek).append("\n\n");
                }
            } finally {
                cursor.close();
            }
        }

        TextView eventCountText = view.findViewById(R.id.textView_stats);
        eventCountText.setText(result.toString());
    }


    private void displayStatsForAllDays(View view) {
        StringBuilder result = new StringBuilder();

        SQLiteDatabase db = dbLogger.getReadableDatabase();
        String query = "SELECT date, event, COUNT(*) AS event_count FROM log_table GROUP BY date, event";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            try {
                String currentDate = null;

                while (cursor.moveToNext()) {
                    String date = cursor.getString(0);
                    Events event = Events.valueOf(cursor.getString(1));
                    int eventCount = cursor.getInt(2);

                    if (!date.equals(currentDate)) {
                        if (currentDate != null) {
                            result.append("\n");
                        }
                        result.append("Date: ").append(date).append("\n\n");
                        currentDate = date;
                    }
                    if (event.equals(Events.LATE)) {
                        result.append(getSpecificEvent(event));
                    } else if (event.equals(Events.WORK_HOME)) {
                        result.append(getSpecificEvent(event));
                    } else {
                        result.append(getSpecificEvent(event)).append(": ").append(eventCount).append("\n");
                    }

                }
            } finally {
                cursor.close();
            }
        }

        TextView eventCountText = view.findViewById(R.id.textView_stats);
        eventCountText.setText(result.toString());
    }

    private String getSpecificEvent(Events event) {
        switch (event) {
            case LATE:
                return "Was late to work\n";
            case WORK_HOME:
                return "Working @Home\n";
            default:
                return event.toString();
        }
    }

    private void displayStatsForAllWeeks(View view) {
        StringBuilder result = new StringBuilder();

        SQLiteDatabase db = dbLogger.getReadableDatabase();
        String query = "SELECT strftime('%W', date) AS week, event, COUNT(*) AS eventCount FROM log_table GROUP BY week, event";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            try {
                String currentWeek = null;

                while (cursor.moveToNext()) {
                    String weekNumber = cursor.getString(0);
                    Events event = Events.valueOf(cursor.getString(1));
                    int eventCount = cursor.getInt(2);

                    if (!weekNumber.equals(currentWeek)) {
                        if (currentWeek != null) {
                            result.append("\n");
                        }
                        result.append("Week ").append(weekNumber).append(":\n\n");
                        currentWeek = weekNumber;
                    }

                    result.append(event.toString()).append(": ").append(eventCount).append("\n");
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
}