package com.example.angel.myfit.basichistoryapi.fitChart;

/**
 * Created by YOUNSANGHO on 2016-05-22.
 */
import android.graphics.RectF;

class RendererFactory {
    public static Renderer getRenderer(AnimationMode mode, FitChartValue value, RectF drawingArea) {
        if (mode == AnimationMode.LINEAR) {
            return new LinearValueRenderer(drawingArea, value);
        } else {
            return new OverdrawValueRenderer(drawingArea, value);
        }
    }
}
