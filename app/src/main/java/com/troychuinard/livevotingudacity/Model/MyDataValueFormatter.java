package com.troychuinard.livevotingudacity.Model;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;

/**
 * Created by troychuinard on 2/19/16.
 */
public class MyDataValueFormatter implements ValueFormatter{

    private DecimalFormat mFormat;

    public MyDataValueFormatter(){
        mFormat = new DecimalFormat("###,###,##0");
    }


    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        return mFormat.format(value);
    }
}
