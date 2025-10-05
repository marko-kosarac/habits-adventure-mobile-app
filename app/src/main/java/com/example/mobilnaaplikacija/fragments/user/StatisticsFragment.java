package com.example.mobilnaaplikacija.fragments.user;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mobilnaaplikacija.R;
import com.example.mobilnaaplikacija.model.enums.DifficultyType;
import com.example.mobilnaaplikacija.model.enums.StatusType;
import com.example.mobilnaaplikacija.model.Task;
import com.example.mobilnaaplikacija.services.TaskService;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsFragment extends Fragment {

    // Charts
    private PieChart pieChart;            // urađeni/neurađeni/otkazani
    private BarChart barChart;            // zavrseni po kategorijama
    private LineChart lineChartXP;        // XP poslednjih 7 dana
    private LineChart lineChartDifficulty;// prosečna težina zadataka
    private TaskService taskService;


    // TextViews
    private TextView tvActiveDays, tvAverageDifficulty, tvLongestStreak, tvMissions;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        // Bind views
        pieChart = view.findViewById(R.id.donutChart);
        barChart = view.findViewById(R.id.barChart);
        lineChartXP = view.findViewById(R.id.lineChartXP);
        lineChartDifficulty = view.findViewById(R.id.lineChartDifficulty);
        tvAverageDifficulty = view.findViewById(R.id.tvAverageDifficulty);

        tvActiveDays = view.findViewById(R.id.tvActiveDays);
        tvLongestStreak = view.findViewById(R.id.tvLongestStreak);
        tvMissions = view.findViewById(R.id.tvMissions);

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        int activeDays = userDoc.contains("activeDays")
                                ? userDoc.getLong("activeDays").intValue()
                                : 0;

                        tvActiveDays.setText("Aktivnih dana: " + activeDays);
                    }
                });
        //pie chart
        taskService = new TaskService(requireContext());
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        updatePieChart(userId);
        //streak
        int longestStreak = taskService.getLongestStreak();
        tvLongestStreak.setText("Najduži niz: "+ longestStreak);
        //bar chart
        taskService = new TaskService(requireContext());
        setupBarChartCompletedByCategory(userId);
        //line chart for difficulty
        setupLineChartDifficulty(userId);

        tvMissions.setText("Specijalne misije: 3 započete / 2 završene");

        return view;
    }

    private void updatePieChart(String userId) {
        // Dohvati statistiku iz baze preko TaskService
        Map<String, Integer> counts = taskService.getTaskCounts(userId);

        int uradjeni = counts.getOrDefault("URAĐENI", 0);
        int neuradjeni = counts.getOrDefault("NEURAĐENI", 0);
        int otkazani = counts.getOrDefault("OTKAZANI", 0);
        int aktivni = counts.getOrDefault("AKTIVNI", 0);

        // Kreirani = aktivni + urađeni + neurađeni + otkazani
        int kreirani = aktivni + uradjeni + neuradjeni + otkazani;

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(kreirani, "Kreirani"));
        entries.add(new PieEntry(uradjeni, "Urađeni"));
        entries.add(new PieEntry(neuradjeni, "Neurađeni"));
        entries.add(new PieEntry(otkazani, "Otkazani"));

        PieDataSet dataSet = new PieDataSet(entries, "Zadaci");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        PieData data = new PieData(dataSet);

        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.invalidate();
    }



    private void setupBarChartCompletedByCategory(String userId) {
        Map<String, Integer> completedWithColors = taskService.getCompletedTasksWithColors(userId);

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        int index = 0;

        for (Map.Entry<String, Integer> e : completedWithColors.entrySet()) {
            String[] parts = e.getKey().split(":");
            String categoryName = parts[0];
            int color = Integer.parseInt(parts[1]);

            labels.add(categoryName);
            entries.add(new BarEntry(index, e.getValue()));
            colors.add(color);
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Završeni zadaci po kategoriji");
        dataSet.setColors(colors);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.9f);

        barChart.setData(data);
        barChart.setFitBars(true);
        barChart.getDescription().setEnabled(false);

        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setGranularityEnabled(true);
        barChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int i = (int) value;
                if (i >= 0 && i < labels.size()) return labels.get(i);
                else return "";
            }
        });

        barChart.invalidate();
    }

    private void setupLineChartDifficulty(String userId) {
        List<Task> tasks = taskService.getCompletedTasks(userId);
        List<Entry> entries = new ArrayList<>();
        int index = 1;
        for (Task t : tasks) {
            int xp = taskService.getXPFromDifficulty(t.getDifficulty());
            entries.add(new Entry(index, xp));
            index++;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Težina završenih zadataka");
        dataSet.setColor(Color.RED);
        dataSet.setCircleColor(Color.BLACK);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);

        LineData lineData = new LineData(dataSet);
        lineChartDifficulty.setData(lineData);
        lineChartDifficulty.getDescription().setEnabled(false);
        lineChartDifficulty.invalidate();

        // Prosečna težina
        float avgXP = taskService.getAverageXPOfCompletedTasks(userId);
        DifficultyType mainDifficulty = taskService.getDifficultyFromXP(avgXP);
        tvAverageDifficulty.setText("Korisnik uglavnom rešava: " + mainDifficulty.name() +
                " (prosek XP: " + avgXP + ")");
    }



}
