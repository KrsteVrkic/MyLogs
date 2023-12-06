package com.example.smokelogger;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogFragment extends Fragment {

    private DatabaseLogger dbLogger;

    private Events event;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log, container, false);

        dbLogger = new DatabaseLogger(requireContext());

        Button logButton = view.findViewById(R.id.btnSmoke);
        logButton.setOnClickListener(v -> {
            logSmokeBreakToDb();
            int smokeBreakCount = countSmokeBreakFromDbCurrentDate();
            Toast.makeText(requireContext(), "smoke breaks: " + smokeBreakCount, Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    private void logSmokeBreakToDb() {
        event = Events.SMOKE_BREAK;

        SQLiteDatabase db = dbLogger.getWritableDatabase();

        db.execSQL("INSERT INTO log_table (date, event) VALUES (?, ?)",
                new Object[]{getCurrentDate(), event.toString()});
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    private int countSmokeBreakFromDbCurrentDate() {
        int count = 0;
        SQLiteDatabase db = dbLogger.getReadableDatabase();
        event = Events.SMOKE_BREAK;

        String query = "SELECT COUNT(*) FROM log_table WHERE date = ? AND event = ?";
        String[] selectionArgs = {getCurrentDate(), event.toString()};
        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    count = cursor.getInt(0);
                }
            } finally {
                cursor.close();
            }
        }
        return count;
    }
}