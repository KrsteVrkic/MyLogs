package com.example.smokelogger;

import org.jetbrains.annotations.NotNull;

public enum Events {
    SMOKE_BREAK, LATE, WORK_HOME, TEST;

    @NotNull
    @Override
    public String toString() {
        switch (this) {
            case SMOKE_BREAK:
                return "Smoke Break";
            case LATE:
                return "Late";
            case WORK_HOME:
                return  "Working @Home";
            default:
                return super.toString();
        }
    }
}
