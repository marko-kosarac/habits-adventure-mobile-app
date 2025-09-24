package com.example.mobilnaaplikacija.fragments.user;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mobilnaaplikacija.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class StatisticsFragment extends Fragment {

    // Charts
    private PieChart pieChart;            // urađeni/neurađeni/otkazani
    private BarChart barChart;            // zavrseni po kategorijama
    private LineChart lineChartXP;        // XP poslednjih 7 dana
    private LineChart lineChartDifficulty;// prosečna težina zadataka

    // TextViews
    private TextView tvActiveDays;
    private TextView tvLongestStreak;
    private TextView tvMissions;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        // Bind views
        pieChart = view.findViewById(R.id.donutChart);
        barChart = view.findViewById(R.id.barChart);
        lineChartXP = view.findViewById(R.id.lineChartXP);
        lineChartDifficulty = view.findViewById(R.id.lineChartDifficulty);

        tvActiveDays = view.findViewById(R.id.tvActiveDays);
        tvLongestStreak = view.findViewById(R.id.tvLongestStreak);
        tvMissions = view.findViewById(R.id.tvMissions);

        // Setup
        setupPieChart();
        setupBarChart();
        setupLineChartXP();
        setupLineChartDifficulty();

        // Tekstualni podaci
        tvActiveDays.setText("Aktivni dani: 34");
        tvLongestStreak.setText("Najduži niz: 12 dana");
        tvMissions.setText("Specijalne misije: 3 započete / 2 završene");

        return view;
    }

    private void setupPieChart() {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(10, "Urađeni"));
        entries.add(new PieEntry(5, "Neurađeni"));
        entries.add(new PieEntry(2, "Otkazani"));

        PieDataSet dataSet = new PieDataSet(entries, "Zadaci");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        PieData data = new PieData(dataSet);

        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.invalidate();
    }

    private void setupBarChart() {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 10)); // Zdravlje
        entries.add(new BarEntry(1, 14)); // Učenje
        entries.add(new BarEntry(2, 6));  // Posao

        BarDataSet dataSet = new BarDataSet(entries, "Kategorije");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        BarData data = new BarData(dataSet);
        barChart.setData(data);
        barChart.getDescription().setEnabled(false);
        barChart.invalidate();
    }

    private void setupLineChartXP() {
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(1, 20));
        entries.add(new Entry(2, 30));
        entries.add(new Entry(3, 25));
        entries.add(new Entry(4, 40));
        entries.add(new Entry(5, 50));
        entries.add(new Entry(6, 35));
        entries.add(new Entry(7, 45));

        LineDataSet dataSet = new LineDataSet(entries, "XP poslednjih 7 dana");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.BLACK);

        LineData lineData = new LineData(dataSet);
        lineChartXP.setData(lineData);
        lineChartXP.getDescription().setEnabled(false);
        lineChartXP.invalidate();
    }

    private void setupLineChartDifficulty() {
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(1, 2)); // laka
        entries.add(new Entry(2, 3)); // srednja
        entries.add(new Entry(3, 4)); // teža
        entries.add(new Entry(4, 5)); // najteža

        LineDataSet dataSet = new LineDataSet(entries, "Prosečna težina zadataka");
        dataSet.setColor(Color.RED);
        dataSet.setValueTextColor(Color.BLACK);

        LineData lineData = new LineData(dataSet);
        lineChartDifficulty.setData(lineData);
        lineChartDifficulty.getDescription().setEnabled(false);
        lineChartDifficulty.invalidate();
    }
}
