package tool;

import java.util.Arrays;
import java.util.Comparator;

public class KNNFilled {
    private double distance(double[] p, double[] q) {
        int validNum = 0;
        double dis = 0.0;
        for (int i = 0; i < p.length; i++) {
            if (p[i] >= 0 && q[i] >= 0) {
                validNum++;
                dis += (p[i] - q[i]) * (p[i] - q[i]);
            }
        }
        if (validNum == 0) {
            return Integer.MAX_VALUE;
        }
        return dis / validNum;
    }

    public double[][][] knnFilled(double[][] data, int k) {
        long start = System.currentTimeMillis();
        int n = data.length;
        int d = data[0].length;

        double[][][] res = new double[k][n][d];
        for (int i = 0; i < n; i++) {
            if (i % 500 == 0) {
                long end = System.currentTimeMillis();
                System.out.println("KNN Execute: " + (i * 100.0 / n) + "%\t Spend:" + (end - start) / 1000.0 + "s");
            }
            double[][] dist = new double[n][2];
            for (int x = 0; x < n; x++) {
                dist[x] = new double[]{x, distance(data[x], data[i])};
            }
            dist[i][1] = Integer.MAX_VALUE;
            Arrays.sort(dist, Comparator.comparingDouble(o -> o[1]));
            for (int j = 0; j < d; j++) {
                if (data[i][j] < 0) {
                    int cnt = 0;
                    for (int x = 0; x < n && cnt < k; x++) {
                        int index = (int) Math.ceil(dist[x][0]);
                        if (data[index][j] < 0) {
                            continue;
                        }
                        res[cnt][i][j] = data[index][j];
                        cnt++;
                    }
                } else {
                    for (int x = 0; x < k; x++) {
                        res[x][i][j] = data[i][j];
                    }
                }
            }
        }
        return res;
    }
}
