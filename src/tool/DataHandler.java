package tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataHandler {
    public static double[][] dataSlice(double[][] data, int scaleStart, int scaleEnd, boolean[] dimension) {
        int dSelect = 0;
        for (boolean check : dimension) {
            if (check) dSelect++;
        }
        double[][] res = new double[scaleEnd - scaleStart + 1][dSelect];

        int resI = 0;
        for (int i = scaleStart - 1; i < scaleEnd; i++) {
            int resJ = 0;
            for (int j = 0; j < data[0].length; j++) {
                if (dimension[j]) {
                    res[resI][resJ++] = data[i][j];
                }
            }
            resI++;
        }
        return res;
    }

    public static double[][][] buildIntervalData(List<double[][]> dataAll) {
        int m = dataAll.get(0).length;
        int n = dataAll.get(0)[0].length;
        double[][][] dataFilled = new double[2][m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                double MIN_VALUE = Integer.MAX_VALUE;
                double MAX_VALUE = Integer.MIN_VALUE;
                for (int k = 0; k < dataAll.size(); k++) {
                    MIN_VALUE = Math.min(MIN_VALUE, dataAll.get(k)[i][j]);
                    MAX_VALUE = Math.max(MAX_VALUE, dataAll.get(k)[i][j]);
                }
                dataFilled[0][i][j] = MIN_VALUE;
                dataFilled[1][i][j] = MAX_VALUE;
            }
        }
        return dataFilled;
    }


    public static double[][] dataNormalized(double[][] data, boolean[] smallerFlag) {
        int n = data.length;
        int d = data[0].length;
        double[][] res = new double[n][d];
        for (int j = 0; j < d; j++) {
            double min = Integer.MAX_VALUE;
            double max = Integer.MIN_VALUE;
            for (int i = 0; i < n; i++) {
                if (data[i][j] < 0) {
                    continue;
                }
                min = Math.min(data[i][j], min);
                max = Math.max(data[i][j], max);
            }
            for (int i = 0; i < n; i++) {
                if (data[i][j] < 0) {
                    res[i][j] = -1;
                } else if (smallerFlag[j]) {
                    res[i][j] = (max - data[i][j]) / (max - min);
                } else {
                    res[i][j] = (data[i][j] - min) / (max - min);
                }
            }
        }
        return res;
    }

}
