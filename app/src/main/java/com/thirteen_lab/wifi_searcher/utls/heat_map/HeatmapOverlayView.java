package com.thirteen_lab.wifi_searcher.utls.heat_map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.Map;

public class HeatmapOverlayView extends View {

    private Map<CellPosition, SignalGrid.SignalInfo> cells;
    private GridInfo gridInfo;

    private Paint paint = new Paint();
    private float lastTouchX = -1;
    private float lastTouchY = -1;

    public HeatmapOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setStyle(Paint.Style.FILL);
        setClickable(true);
        setFocusable(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            lastTouchX = event.getX();
            lastTouchY = event.getY();

            Log.d("HeatmapOverlayView", "Touched at: " + lastTouchX + ", " + lastTouchY);
            invalidate(); // redraw with marker
            return true;
        }
        return false;
    }

    public CellPosition getTappedCell() {
        if (gridInfo == null) {
            Log.d("HeatmapOverlayView", "gridInfo is null in getTappedCell()");
            return null;
        }
        if (lastTouchX < 0 || lastTouchY < 0) {
            Log.d("HeatmapOverlayView", "Touch coordinates not set");
            return null;
        }

        int col = (int) (lastTouchX / gridInfo.getCellWidthPx());
        int row = (int) (lastTouchY / gridInfo.getCellHeightPx());

        Log.d("HeatmapOverlayView", "Tapped cell: row=" + row + ", col=" + col);
        return new CellPosition(row, col);
    }

    public void clearData() {
        this.cells = null;
        this.lastTouchX = -1;  // reset tap X coordinate
        this.lastTouchY = -1;  // reset tap Y coordinate
        invalidate();          // force redraw with no data and no tap marker
    }



    public void setData(GridInfo gridInfo, Map<CellPosition, SignalGrid.SignalInfo> cells) {
        this.gridInfo = gridInfo;
        this.cells = cells;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (cells != null && gridInfo != null) {
            for (Map.Entry<CellPosition, SignalGrid.SignalInfo> entry : cells.entrySet()) {
                CellPosition pos = entry.getKey();
                SignalGrid.SignalInfo info = entry.getValue();

                if (pos == null || info == null) continue;

                double signal = info.getAverageSignalLevel();
                int color = getColorForSignal(signal);
                paint.setColor(color);

                float left = pos.getColumn() * gridInfo.getCellWidthPx();
                float top = pos.getRow() * gridInfo.getCellHeightPx();
                float right = left + gridInfo.getCellWidthPx();
                float bottom = top + gridInfo.getCellHeightPx();

                canvas.drawRect(left, top, right, bottom, paint);
            }
        }

        // Draw touch marker if touch has occurred
        if (lastTouchX >= 0 && lastTouchY >= 0) {
            paint.setColor(Color.RED);
            canvas.drawCircle(lastTouchX, lastTouchY, 15, paint);
        }
    }

    private int getColorForSignal(double signal) {
        double norm = Math.max(0, Math.min(1, (signal + 100) / 70.0));
        int red = (int) ((1 - norm) * 255);
        int green = (int) (norm * 255);
        return Color.rgb(red, green, 0);
    }
}
