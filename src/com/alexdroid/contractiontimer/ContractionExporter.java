package com.alexdroid.contractiontimer;

import android.content.Context;
import android.text.format.DateUtils;
import java.util.ArrayList;
import java.text.DateFormat;

public class ContractionExporter {
    public static String exportToCSV(Context context,
                                     ArrayList<Contraction> contractions) {
        StringBuilder csv = new StringBuilder().append(context.getString(R.string.start_label_text) + ";" +
                                                       context.getString(R.string.length_label_text) + ";" +
                                                       context.getString(R.string.interval_label_text) + "\n");

        /* append contractions as CSV */
        Contraction previous = null;
        for (Contraction contraction : contractions) {
            long start_ms = contraction.getStartMillis();
            long length_s = contraction.getLengthMillis() / 1000;
            long interval_s = 0;
            if (previous != null) {
                interval_s = (start_ms - previous.getStartMillis()) / 1000;
            }
            csv.append(DateFormat.getInstance().format(start_ms) + ";" +
                       DateUtils.formatElapsedTime(length_s) + ";" +
                       DateUtils.formatElapsedTime(interval_s) + "\n");
            previous = contraction;
        }
        return csv.toString();
    }
}
