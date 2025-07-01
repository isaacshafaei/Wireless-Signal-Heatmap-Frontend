package com.thirteen_lab.wifi_searcher.utls.heat_map;

import android.location.Location;

public class GridInfo {

    private double cellWidth;  // meters
    private double cellHeight; // meters

    private int columnsCount;
    private int rowsCount;

    private Location centerLocation;

    private int totalPixelWidth;
    private int totalPixelHeight;

    public GridInfo(Location centerLocation) {
        this.cellWidth = 10.0;
        this.cellHeight = 10.0;
        this.columnsCount = 10;
        this.rowsCount = 10;
        this.centerLocation = centerLocation;

        // Default values; should be set later based on screen/map size
        this.totalPixelWidth = 1000;
        this.totalPixelHeight = 1000;
    }

    public Location getCenterLocation() {
        return centerLocation;
    }

    public double getCellWidth() {
        return cellWidth;
    }

    public double getCellHeight() {
        return cellHeight;
    }

    public int getColumnsCount() {
        return columnsCount;
    }

    public int getRowsCount() {
        return rowsCount;
    }

    public double getWidth() {
        return cellWidth * columnsCount;
    }

    public double getHeight() {
        return cellHeight * rowsCount;
    }

    public void setPixelSize(int widthPx, int heightPx) {
        this.totalPixelWidth = widthPx;
        this.totalPixelHeight = heightPx;
    }

    public float getCellWidthPx() {
        return (float) totalPixelWidth / columnsCount;
    }

    public float getCellHeightPx() {
        return (float) totalPixelHeight / rowsCount;
    }

    public boolean containsCellPosition(CellPosition cellPosition) {
        return cellPosition.getRow() >= 0 && cellPosition.getRow() < rowsCount &&
                cellPosition.getColumn() >= 0 && cellPosition.getColumn() < columnsCount;
    }

    public CellPosition computeCellPosition(Location location) {
        double yOffset = GeographicalCalculator.InMeters.getNorthwardsDisplacement(centerLocation, location);
        double xOffset = GeographicalCalculator.InMeters.getEastwardsDisplacement(centerLocation, location);

        yOffset += getHeight() / 2;
        xOffset += getWidth() / 2;

        int row = (int) (yOffset / cellHeight);
        int column = (int) (xOffset / cellWidth);

        return new CellPosition(row, column);
    }
}
