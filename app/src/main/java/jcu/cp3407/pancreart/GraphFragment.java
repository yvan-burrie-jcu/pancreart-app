package jcu.cp3407.pancreart;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class GraphFragment extends Fragment implements OnChartValueSelectedListener {

    LineChart chart;

    final float textSize = 12;

    int day, month, year;

    public GraphFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_graph, container, false);

        setupChart(view);
        setupChartLegends();
        setupChartLimitLines();

        setChartDescription("Click on glucose and insulin dots for more info.");

        Calendar calendar = Calendar.getInstance();
        day = calendar.get(Calendar.DAY_OF_MONTH);
        month = calendar.get(Calendar.MONTH);
        year = calendar.get(Calendar.YEAR);

        drawChart(day, month, year);

        return view;
    }

    private void setupChart(View view) {
        chart = view.findViewById(R.id.chart);

        chart.setOnChartValueSelectedListener(this);

        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(false);

        chart.getAxisLeft().setEnabled(true);
        chart.getAxisRight().setEnabled(false);

        chart.setDrawBorders(false);
        chart.setBackgroundColor(getResources().getColor(R.color.white));
        chart.setGridBackgroundColor(getResources().getColor(R.color.white));
    }

    private void setupChartLegends() {
        Legend legend = chart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
    }

    private void setupChartLimitLines() {
        LimitLine ll1, ll2;

        // High glucose limit
        ll1 = new LimitLine(180, "High Glucose Level");
        ll1.setLineColor(Color.YELLOW);
        ll1.setLineWidth(2);
        ll1.enableDashedLine(50, 10, 0);
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
        ll1.setTextSize(textSize);

        // Low glucose limit
        ll2 = new LimitLine(60, "Low Glucose Level");
        ll2.setLineColor(Color.RED);
        ll2.setLineWidth(2);
        ll2.enableDashedLine(50, 10, 0);
        ll2.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_BOTTOM);
        ll2.setTextSize(textSize);

        // Append lines to chart
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.removeAllLimitLines();
        leftAxis.addLimitLine(ll1);
        leftAxis.addLimitLine(ll2);
    }

    public void setChartDescription(@Nullable String text) {
        if (text == null) {
            chart.getDescription().setEnabled(false);
            return;
        }
        Description description = new Description();
        description.setText(text);
        description.setTextSize(textSize);
        chart.setDescription(description);
    }

    @Override
    public void onValueSelected(Entry entry, Highlight highlight) {
        setChartDescription("Glucose amount of " + entry.getX() + " ml.");
    }

    @Override
    public void onNothingSelected() {
    }

    private void drawChart(int day, int month, int year) {
        Calendar date1 = new GregorianCalendar(year, month, day);
        Calendar date2 = (Calendar) date1.clone();
        date2.add(Calendar.DAY_OF_MONTH, 1);

        final XAxis xAxis = chart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAvoidFirstLastClipping(true);

        List<Entry> values1 = new ArrayList<>();
        List<Entry> values2 = new ArrayList<>();

        for (Event event : DashboardActivity.events) {
            if (event.time < date1.getTimeInMillis() && event.time > date2.getTimeInMillis()) {
                continue;
            }
            Entry entry = new Entry(event.time - date1.getTimeInMillis(), (float) event.amount);
            if (event.type == Event.Type.GLUCOSE_READING) {
                values1.add(entry);
                continue;
            }
            if (event.type == Event.Type.INSULIN_INJECTION) {
                values2.add(entry);
            }
        }

        List<ILineDataSet> lineDataSets = new ArrayList<>();

        LineDataSet lineDataSet1 = new LineDataSet(values1, "Glucose");
        lineDataSet1.setCircleRadius(3);
        lineDataSet1.setCircleHoleRadius(1);
        lineDataSet1.setCircleColor(Color.BLUE);
        lineDataSet1.setCircleHoleColor(Color.CYAN);
        lineDataSet1.setColor(Color.BLUE);
        lineDataSet1.setDrawValues(true);
        lineDataSets.add(lineDataSet1);

        LineDataSet lineDataSet2 = new LineDataSet(values2, "Insulin");
        lineDataSet2.setCircleRadius(5);
        lineDataSet2.setCircleHoleRadius(3);
        lineDataSet2.setCircleColor(Color.GREEN);
        lineDataSet2.setCircleHoleColor(Color.GRAY);
        lineDataSet2.setColor(Color.GREEN);
        lineDataSet2.setLineWidth(0);
        lineDataSets.add(lineDataSet2);

        LineData lineData = new LineData(lineDataSets);
        chart.setData(lineData);
        chart.invalidate();
    }

    public void update(int day, int month, int year) {
        // Handle retrieving data from SQLite database
        // redraw chart

        drawChart(day, month, year);
    }
}
