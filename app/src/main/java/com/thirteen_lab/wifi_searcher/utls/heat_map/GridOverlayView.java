package com.thirteen_lab.wifi_searcher.utls.heat_map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

public class GridOverlayView extends View {
    private int numRows = 15;
    private int numCols = 15;
    private Paint linePaint;

    public GridOverlayView(Context context) {
        super(context);
        init();
    }

    private void init() {
        linePaint = new Paint();
        linePaint.setColor(0x80FFFFFF); // Semi-transparent white
        linePaint.setStrokeWidth(2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cellWidth = getWidth() / (float) numCols;
        float cellHeight = getHeight() / (float) numRows;

        for (int i = 1; i < numCols; i++)
            canvas.drawLine(i * cellWidth, 0, i * cellWidth, getHeight(), linePaint);
        for (int i = 1; i < numRows; i++)
            canvas.drawLine(0, i * cellHeight, getWidth(), i * cellHeight, linePaint);
    }

}
