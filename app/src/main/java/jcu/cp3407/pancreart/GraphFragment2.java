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
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;


public class GraphFragment2 extends Fragment {
    private LineChart chart;
    Context context;
    public GraphFragment2() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_graph1,container,false);
        chart = view.findViewById(R.id.chart);
        context = view.getContext();

        Legend legend = chart.getLegend();

        final XAxis xAxis = chart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
        xAxis.setAvoidFirstLastClipping(true);

        chart.getAxisRight().setEnabled(false);
        chart.setBackgroundColor(Color.parseColor("#FFFFFF"));
        chart.setGridBackgroundColor(Color.parseColor("#FFFFFF"));

        List<Entry> vals = new ArrayList<>();
        for (int i = 0; i < 10; ++i)
        {
            vals.add(new Entry(i, 100 + 1));
        }
        LineData data = new LineData(new LineDataSet(vals, ""));
        chart.setData(data);
        return view;
    }
}