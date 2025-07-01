package com.thirteen_lab.wifi_searcher.utls.heat_map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Location;
import android.util.AttributeSet;
import android.view.View;

public class GridView extends View {

    private Rect imageBounds = null;
    private Paint paint;

    private MainData mainData = null;
    private WifiNetwork currentWifiNetwork = null;
    private Location currentLocation = null;

    public GridView(Context context) {
        super(context);
        init();
    }

    public GridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
    }

    public void setImageBounds(int x, int y, int width, int height) {
        imageBounds = new Rect(x, y, x + width, y + height);
    }

    public void setMainData(MainData mainData) {
        this.mainData = mainData;
    }

    public void update(WifiNetwork wifiNetwork, Location location) {
        if (wifiNetwork != null) {
            this.currentWifiNetwork = wifiNetwork;
        }
        if (location != null) {
            this.currentLocation = location;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mainData == null || currentWifiNetwork == null || mainData.getGridInfo() == null) {
            return;
        }

        GridInfo gridInfo = mainData.getGridInfo();
        SignalGrid signalGrid = mainData.getSignalGrids().get(currentWifiNetwork);
        if (signalGrid == null) return;

        int width = (imageBounds != null) ? imageBounds.width() : getWidth();
        int height = (imageBounds != null) ? imageBounds.height() : getHeight();
        int offsetX = (imageBounds != null) ? imageBounds.left : 0;
        int offsetY = (imageBounds != null) ? imageBounds.top : 0;

        int rectWidth = width / gridInfo.getColumnsCount();
        int rectHeight = height / gridInfo.getRowsCount();

        int rowsCount = gridInfo.getRowsCount();
        int colsCount = gridInfo.getColumnsCount();

        for (int row = 0; row < rowsCount; row++) {
            for (int col = 0; col < colsCount; col++) {
                // Wrap row and col indices
                int wrappedRow = ((row % rowsCount) + rowsCount) % rowsCount;   // handles negative indices safely
                int wrappedCol = ((col % colsCount) + colsCount) % colsCount;

                CellPosition cellPos = new CellPosition(wrappedRow, wrappedCol);
                SignalGrid.SignalInfo signalInfo = signalGrid.getSignalInfo(cellPos);

// Skip drawing this cell if no signal data exists
                if (signalInfo == null) {
                    continue;
                }

                paint.setColor(getMappedColor(signalInfo.getAverageSignalLevel()));


                // Flip Y axis for drawing, same as before
                int flippedRow = rowsCount - 1 - wrappedRow;

                int left = offsetX + rectWidth * wrappedCol;
                int top = offsetY + rectHeight * flippedRow;
                int right = left + rectWidth;
                int bottom = top + rectHeight;

                canvas.drawRect(left, top, right, bottom, paint);
            }
        }

        // Draw current location as a dot
        if (currentLocation != null) {
            CellPosition currentPos = gridInfo.computeCellPosition(currentLocation);
            // Wrap currentPos row and col as well
            int currentRow = ((currentPos.getRow() % rowsCount) + rowsCount) % rowsCount;
            int currentCol = ((currentPos.getColumn() % colsCount) + colsCount) % colsCount;

            int flippedRow = rowsCount - 1 - currentRow;

            paint.setColor(Color.rgb(0, 105, 191));
            int cx = offsetX + rectWidth * currentCol + rectWidth / 2;
            int cy = offsetY + rectHeight * flippedRow + rectHeight / 2;
            canvas.drawCircle(cx, cy, 3f, paint);
        }
    }


    private int getMappedColor(double signalLevel) {
        int redComplement;
        if (signalLevel <= -100.0) {
            redComplement = 200;
        } else if (signalLevel >= -30.0) {
            redComplement = 0;
        } else {
            redComplement = (int) ((-20 / 7.0) * signalLevel - (600 / 7.0));
        }
        int alpha = 97;
        return Color.argb(alpha, 200, redComplement, redComplement);
    }
}
