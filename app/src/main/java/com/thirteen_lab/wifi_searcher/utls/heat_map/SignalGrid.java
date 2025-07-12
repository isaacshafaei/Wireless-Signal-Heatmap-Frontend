package com.thirteen_lab.wifi_searcher.utls.heat_map;

import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignalGrid {
    GridInfo gridInfo;

    private Map<CellPosition, SignalInfo> cells = new HashMap<>();

    public SignalGrid(GridInfo gridInfo) {
        this.gridInfo = gridInfo;
    }

    public void addMeasurement(Location location, ScanResult scanResult) {
        int level = WifiManager.calculateSignalLevel(scanResult.level, 100); // 0–100 scale
        addSignalLevel(gridInfo.computeCellPosition(location), level);
    }

    private void addSignalLevel(CellPosition cellPosition, int signalLevel) {
        if (cellPosition == null)
            return;

        if (!gridInfo.containsCellPosition(cellPosition))
            return;

        if (!cells.containsKey(cellPosition)) {
            cells.put(cellPosition, new SignalInfo());
        }

        cells.get(cellPosition).addSignalLevel(signalLevel);
    }

    public SignalInfo getSignalInfo(CellPosition cellPosition) {
        if (!gridInfo.containsCellPosition(cellPosition) ||
                !cells.containsKey(cellPosition)) {
            return null;
        }

        return cells.get(cellPosition);
    }

    public Map<CellPosition, SignalInfo> getCells() {
        return cells;
    }

    public GridInfo getGridInfo() {
        return gridInfo;
    }

    public class SignalInfo {
        private List<Integer> signalLevels = new ArrayList<>();
        private double averageSignalLevel = -1; // -1 = no valid data

        public void addSignalLevel(int signalLevel) {
            signalLevels.add(signalLevel);
            updateAverageSignalLevel();
        }

        public double getAverageSignalLevel() {
            return averageSignalLevel;
        }

        private void updateAverageSignalLevel() {
            if (signalLevels.isEmpty()) {
                averageSignalLevel = -1;
                return;
            }

            double sum = 0;
            for (int level : signalLevels) {
                sum += level;
            }

            averageSignalLevel = sum / signalLevels.size();
        }
    }

    public void addSignal(CellPosition cell, int signalLevel) {
        if (cell == null || !gridInfo.containsCellPosition(cell))
            return;

        if (!cells.containsKey(cell)) {
            cells.put(cell, new SignalInfo());
        }

        cells.get(cell).addSignalLevel(signalLevel);
    }
}
