package skyline;

import java.util.ArrayList;
import java.util.List;

public class CompleteSkyline {
    /***
     * sort the inputData by the sum of each row in DESCENDING
     * @return the index after sorting
     */
    protected static int[] sortedBySum(double[][] inputData) {
        List<Point> sortedIndex = new ArrayList<>();
        for (int i = 0; i < inputData.length; i++) {
            double sum = 0;
            for (int j = 0; j < inputData[0].length; j++)
                sum += inputData[i][j];
            sortedIndex.add(new Point(i, sum));
        }

        sortedIndex.sort((o1, o2) -> Double.compare(o2.weight, o1.weight));

        int[] index = new int[sortedIndex.size()];
        for (int i = 0; i < index.length; i++)
            index[i] = sortedIndex.get(i).index;

        return index;
    }

    /***
     * if p dominate q, return true, else false
     */
    protected boolean dominate(double[] p, double[] q) {
        for (int i = 0; i < p.length; i++)
            if (p[i] < q[i])
                return false;
        return true;
    }

    /***
     * simple implements of sfs algorithm
     */
    public int[] sortedFilterSkyline(double[][] data) {
        int[] sortedIndex = sortedBySum(data);
        List<Integer> skyline = new ArrayList<>();
        skyline.add(sortedIndex[0]);

        for (int i = 1; i < data.length; i++) {
            int index = sortedIndex[i];
            boolean continueFlag = false;
            for (int sp : skyline) {
                if (dominate(data[sp], data[index])) {
                    continueFlag = true;
                    break;
                }
            }
            if (continueFlag) continue;

            skyline.add(index);
        }

        return skyline.stream().mapToInt(Integer::valueOf).toArray();
    }

    public int[] nativeSkyline(double[][] data) {
        boolean[] dominated = new boolean[data.length];
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data.length; j++) {
                if (j == i) continue;
                if (dominate(data[j], data[i])) {
                    dominated[i] = true;
                    break;
                }
            }
        }
        List<Integer> res = new ArrayList<>();
        for (int i = 0; i < dominated.length; i++) {
            if (!dominated[i]) {
                res.add(i);
            }
        }
        return res.stream().mapToInt(Integer::valueOf).toArray();
    }
}

class Point {
    public int index;
    public double weight;

    public Point(int index, double weight) {
        this.index = index;
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "" + index + ": " + weight;
    }
}
