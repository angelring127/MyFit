package com.example.angel.myfit.basichistoryapi.fitChart;

/**
 * Created by YOUNSANGHO on 2016-05-22.
 */
import android.graphics.Path;
import android.graphics.RectF;

class OverdrawValueRenderer extends BaseRenderer implements Renderer {
    public OverdrawValueRenderer(RectF drawingArea, FitChartValue value) {
        super(drawingArea, value);
    }

    @Override
    public Path buildPath(float animationProgress, float animationSeek) {
        float startAngle = FitChart.START_ANGLE;
        float valueSweepAngle = (getValue().getStartAngle() +  getValue().getSweepAngle());
        valueSweepAngle -= startAngle;
        float sweepAngle = valueSweepAngle * animationProgress;
        Path path = new Path();
        path.addArc(getDrawingArea(), startAngle, sweepAngle);
        return path;
    }
}
