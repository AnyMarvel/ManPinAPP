package com.google.android.apps.photolab.storyboard.pipeline;

import android.util.Log;
import com.google.android.apps.photolab.storyboard.activity.ComicActivity;
import com.google.android.apps.photolab.storyboard.activity.ComicPanel;
import java.util.ArrayList;
import java.util.Locale;

public class PanelDebugInfo {
    private ArrayList<String> debugCategories;
    private ArrayList<String> debugRules;
    private ArrayList<ArrayList<Number>> debugScores;
    private ComicPanel panel;
    public ArrayList<Integer> possibleWins;
    public int winIndex = -1;

    public PanelDebugInfo(ComicPanel panel) {
        this.panel = panel;
    }

    public void debugReasoning(String ruleName, ArrayList<ObjectDetection> dets, float[] scores) {
        if (ComicActivity.SHOW_DEBUG_INFO) {
            int i;
            if (this.debugCategories == null) {
                this.debugCategories = new ArrayList();
                this.debugScores = new ArrayList();
                this.debugRules = new ArrayList();
                for (i = 0; i < dets.size(); i++) {
                    this.debugCategories.add(((ObjectDetection) dets.get(i)).category);
                }
            }
            this.debugRules.add(ruleName);
            ArrayList<Number> listScores = new ArrayList();
            for (float valueOf : scores) {
                listScores.add(Float.valueOf(valueOf));
            }
            this.debugScores.add(listScores);
        }
    }

    public void traceReasoning() {
        if (ComicActivity.SHOW_DEBUG_INFO && this.debugCategories != null) {
            int i;
            String str;
            StringBuilder sb = new StringBuilder();
            sb.append("\t\t\t\t");
            for (i = 0; i < this.debugRules.size(); i++) {
                sb.append(String.valueOf((String) this.debugRules.get(i)).concat("\t"));
            }
            sb.append("total");
            sb.append("\n");
            i = 0;
            while (i < this.debugCategories.size()) {
                String valueOf;
                if (this.winIndex == i) {
                    sb.append("** ");
                } else if (this.possibleWins != null && this.possibleWins.indexOf(Integer.valueOf(i)) > -1) {
                    sb.append("*");
                }
                sb.append(String.valueOf((String) this.debugCategories.get(i)).concat("\t"));
                float total = 0.0f;
                for (int j = 0; j < this.debugScores.size(); j++) {
                    float score = ((Number) ((ArrayList) this.debugScores.get(j)).get(i)).floatValue();
                    if (j > 0) {
                        score -= ((Number) ((ArrayList) this.debugScores.get(j - 1)).get(i)).floatValue();
                    }
                    total += score;
                    str = "\t";
                    valueOf = String.valueOf(String.format(Locale.US, "%.2f", new Object[]{Float.valueOf(score)}));
                    if (valueOf.length() != 0) {
                        valueOf = str.concat(valueOf);
                    } else {
                        valueOf = new String(str);
                    }
                    sb.append(valueOf);
                }
                valueOf = String.format(Locale.US, "%.2f", new Object[]{Float.valueOf(total)});
                sb.append(new StringBuilder(String.valueOf(valueOf).length() + 2).append("\t").append(valueOf).append("\n").toString());
                i++;
            }
            str = this.panel.toString();
            Log.i("ContentValues", new StringBuilder(String.valueOf(str).length() + 10).append("******** ").append(str).append("\n").toString());
            Log.i("ContentValues", String.valueOf(sb.toString()).concat("************\n"));
        }
    }
}
