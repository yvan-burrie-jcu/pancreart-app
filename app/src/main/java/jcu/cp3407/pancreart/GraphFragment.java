package jcu.cp3407.pancreart;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class GraphFragment extends Fragment {
    LineChart chart;
    LineData lineData;
    List<Entry> entryList = new ArrayList<>();
    Context context;
    int day, month, year;

    public GraphFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_graph,container,false);
        chart = view.findViewById(R.id.chart);
        context = view.getContext();
        Legend legend = chart.getLegend();

        Calendar calendar = Calendar.getInstance();
        day = calendar.get(Calendar.DAY_OF_MONTH);
        month = calendar.get(Calendar.MONTH);
        year = calendar.get(Calendar.YEAR);

        drawChart(day, month, year);

        return view;
    }

    private void drawChart(int day, int month, int year) {
        // Query database and store as event.



        // Set X-Axis
        final XAxis xAxis = chart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
        xAxis.setAvoidFirstLastClipping(true);

        // store all glucose in List<Entry> glucoseData = new ArrayList<>()
        // store all insulin in List<Entry> insulinData = new ArrayList<>()
        // calculate median from adding all values and dividing by amount of values in List<Entry>

        // Create LineData objects for each List<Entry> and median value.
        // LineData glucoseLineData = new LineData(new LineDataSet(glucoseData, "Glucose"));
        // LineData insulinLineData = new LineData(new LineDataSet(insulinData, "Insulin"));
        // LineData medianLine = new LineData(new LineDataSet(median, "Median"));

        // Set line colours.
        // glucoseLineData.setValueTextColor(R.color.line_glucose);
        // insulinLineData.setValueTextColor(R.color.line_glucose);
        // medianLine.setValueTextColor(R.color.line_glucose);



        // chart.setData(glucoseLineData);
        // chart.setData(insulinLineData);
        // chart.setData(medianLine);

        chart.getAxisRight().setEnabled(false);
        chart.setBackgroundColor(getResources().getColor(R.color.white));
        chart.setGridBackgroundColor(getResources().getColor(R.color.white));

        List<Entry> values = new ArrayList<>();
        for (int i = 0; i < 10; ++i)
        {
            values.add(new Entry(i, 100 + 1));
        }
        LineData data = new LineData(new LineDataSet(values, ""));
        chart.setData(data);
    }


    public void update(int day, int month, int year) {
        // Handle retrieving data from SQLite database
        // redraw chart

        drawChart(day, month, year);
    }

}