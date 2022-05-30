package skyline;

import java.util.Arrays;

public abstract class PSky {
    protected double[][] cube;
    protected double[][] freq;
    protected int bins;
    protected double[][] prefixSum;

    public void initAll(double[][] dataNative, int bins) {
        this.bins = bins;
        this.initHist(dataNative);
        this.initPrefixSum();
    }

    /**
     * in each bin, cover left, not cover right -> [left, right)
     * except the last bin -> [left, right]
     */
    private void initHist(double[][] dataNative) {
        this.cube = new double[dataNative[0].length][bins + 1];
        this.freq = new double[dataNative[0].length][bins];
        for (int j = 0; j < dataNative[0].length; j++) {
            double[] attr = getAttr(dataNative, j);
            Arrays.sort(attr);
            int nanNum = 0;
            for (double val : attr) {
                if (val < 0)
                    nanNum++;
            }
            double min = attr[nanNum], max = attr[attr.length - 1];
            int totalNum = attr.length - nanNum;
            double gap = (max - min) / bins;
            cube[j][0] = min;
            int next = 1;
            for (int i = 1; i < cube[j].length; i++)
                cube[j][i] = cube[j][i - 1] + gap;
            cube[j][cube[j].length - 1] = max;
            for (int i = nanNum; i < attr.length; i++) {
                double val = attr[i];
                if (val < 0)
                    continue;
                if (next == cube[j].length - 1 && val == cube[j][next])
                    freq[j][next - 1] += 1;
                else if (val < cube[j][next]) {
                    freq[j][next - 1] += 1;
                } else {
                    i--;
                    next++;
                }
            }
            for (int i = 0; i < freq[j].length; i++)
                freq[j][i] = freq[j][i] / totalNum / gap;
        }
    }

    /**
     * prefix sum of freq to accelerate
     */
    private void initPrefixSum() {
        prefixSum = new double[freq.length][freq[0].length];
        for (int i = 0; i < freq.length; i++) {
            prefixSum[i][0] = freq[i][0];
            for (int j = 1; j < freq[0].length; j++)
                prefixSum[i][j] = prefixSum[i][j - 1] + freq[i][j];
        }
    }

    /**
     * get all value of one attribute
     */
    public static double[] getAttr(double[][] data, int index) {
        double[] res = new double[data.length];
        for (int i = 0; i < data.length; i++)
            res[i] = data[i][index];
        return res;
    }

    protected boolean isPoint(double[] q) {
        return Math.abs(q[0] - q[1]) <= 1e-8;
    }

    /**
     * P([left, right]) of attribute index
     */
    protected double histProb(int index, double left, double right) {
        if (left == right)
            return 0;
        double[] cubeLocal = cube[index];
        double[] freqLocal = freq[index];
        double[] prefixSumLocal = prefixSum[index];
        double gap = (cubeLocal[cubeLocal.length - 1] - cubeLocal[0]) / (cubeLocal.length - 1);
        int leftIndex = (Math.abs(left - cubeLocal[cubeLocal.length - 1]) <= 1e-5) ? cubeLocal.length - 2 : (int) ((left - cubeLocal[0]) / gap);
        int rightIndex = (Math.abs(right - cubeLocal[cubeLocal.length - 1]) <= 1e-5) ? cubeLocal.length - 2 : (int) ((right - cubeLocal[0]) / gap);

        // 临时，当rightIndex大于等于bins时取最后一个
        rightIndex = Math.min(freqLocal.length - 1, rightIndex);
        leftIndex = Math.min(freqLocal.length - 1, leftIndex);
        rightIndex = Math.max(0, rightIndex);
        leftIndex = Math.max(0, leftIndex);

        if (leftIndex == rightIndex)
            return freqLocal[leftIndex] * (right - left);
        double prob = freqLocal[leftIndex] * (cubeLocal[leftIndex + 1] - left);
        prob += freqLocal[rightIndex] * (right - cubeLocal[rightIndex]);
        prob += (gap * (prefixSumLocal[rightIndex - 1] - prefixSumLocal[leftIndex]));
        return prob;
    }

}
