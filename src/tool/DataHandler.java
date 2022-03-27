package tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataHandler {
    public static double[][] dataSlice(double[][] data, int scale, int dimension) {
        double[][] dataRow = dataRowSlice(data, scale);
        return dataColSlice(dataRow, dimension);
    }

    public static double[][] dataRowSlice(double[][] data, int scale) {
        if (scale >= data.length) return data;
        double[][] res = new double[scale][data[0].length];
        for (int i = 0; i < scale; i++) {
            res[i] = data[i];
        }
        return res;
    }

    public static double[][] dataColSlice(double[][] data, int cols) {
        if (cols >= data[0].length) return data;
        double[][] res = new double[data.length][cols];
        for (int i = 0; i < res.length; i++) {
            for (int j = 0; j < cols; j++) {
                res[i][j] = data[i][j];
            }
        }
        return res;
    }

    public double[][] getHist(double[] data, int bins) {
        double min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (double d : data) {
            min = Math.min(d, min);
            max = Math.max(d, max);
        }
        double[][] res = new double[bins][3];
        double stepLen = (max - min) / bins;
        double[] cp = Arrays.copyOf(data, data.length);
        Arrays.sort(cp);
        int j = 0;
        for (int i = 0; i < bins; i++) {
            double[] box = new double[]{stepLen * i, stepLen * (i + 1), 0};
            for (; j < data.length; j++) {
                if (cp[j] >= box[0] && cp[j] <= box[1])
                    box[2]++;
                else
                    break;
            }
            res[i] = box;
        }
        for (int i = 0; i < bins; i++)
            res[i][2] = res[i][2] / data.length;
        return res;
    }

    public static double[] getAttr(double[][] data, int index) {
        double[] attr = new double[data.length];
        for (int i = 0; i < data.length; i++)
            attr[i] = data[i][index];
        return attr;
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

}
