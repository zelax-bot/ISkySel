package skyline;

import java.util.*;

public class ISkySel extends PSky {

    private final List<Double> time = new ArrayList<>();
    private final String[] timeName = {"Init Hist", "计算所有interval概率", "每个维度挑topk", "计算全局topk"};
    private double[][][] probSmaller;

    public List<Double> getTime() {
        return time;
    }

    public String[] getTimeName() {
        return timeName;
    }

    /**
     * ISkySel method
     *
     * @param dataNative origin data
     * @param dataFilled interval-filled data
     * @param bins       hist bins
     * @param topk       top-k
     * @return index of top-k services (0 start)
     */
    public int[] getSkyline(double[][] dataNative, double[][][] dataFilled, int bins, int topk) {
        assert topk > 0;
        time.clear();
        int n = dataNative.length;
        int d = dataNative[0].length;
        this.probSmaller = new double[2][n][d];

        // init hist prob
        long start = System.currentTimeMillis();
        initAll(dataNative, bins);
        long end = System.currentTimeMillis();
        time.add((end - start) / 1000.0);

        // speed up 1: calculate the prob of all possible interval
        start = System.currentTimeMillis();
        for (int i = 0; i < d; i++) {
            buildProbMatrix(dataFilled, i);
        }
        end = System.currentTimeMillis();
        time.add((end - start) / 1000.0);

        // speed up 2: select topk
        start = System.currentTimeMillis();
        double[][] dataSum = new double[n][2];
        for (int i = 0; i < n; i++) {
            double sum = 0.0;
            for (int j = 0; j < d; j++) {
                sum += (dataFilled[0][i][j] + dataFilled[1][i][j]);
            }
            dataSum[i] = new double[]{i, sum};
        }
        Arrays.sort(dataSum, (o1, o2) -> Double.compare(o2[1], o1[1]));
        int[] indexes = new int[n];
        for (int i = 0; i < n; i++) {
            indexes[i] = (int) Math.ceil(dataSum[i][0]);
        }
        end = System.currentTimeMillis();
        time.add((end - start) / 1000.0);

        start = System.currentTimeMillis();
        // each array : {index, sky prob, dominate prob sum}
        PriorityQueue<double[]> queue = new PriorityQueue<>((o1, o2) -> {
            // 我们的目标：skyline prob越大越好，相同的选dominate prob越大越好
            // 所以小顶堆要求恰好相反：skyline prob越小越好，相同的选dominate prob 越小越好（因为每次要弹出最差的）
            if (o1[1] == o2[1]) {
                return Double.compare(o1[2], o2[2]);
            } else {
                return Double.compare(o1[1], o2[1]);
            }
        });

        // 存储概率等于threshold的服务，用作最后筛选
        // {index, sky prob}
        List<double[]> save = new ArrayList<>();

        double threshold = 0.0;

        for (int i : indexes) {
            double prob = 1.0;
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    continue;
                }
                prob *= (1 - histDominate(dataFilled, j, i, probSmaller));
                if (prob < threshold) {
                    break;
                }
            }
            queue.add(new double[]{i, prob, 0});
            if (queue.size() > topk) {
                double[] cell = queue.poll();
                if (cell[1] >= threshold) {
                    save.add(new double[]{cell[0], cell[1]});
                    threshold = cell[1];
                }
            }
        }

        end = System.currentTimeMillis();
        time.add((end - start) / 1000.0);

        int[] res = new int[topk];

        if (save.size() != 0) {
            while (!queue.isEmpty()) {
                double[] cell = queue.poll();
                save.add(new double[]{cell[0], cell[1]});
            }
            for (double[] service : save) {
                int index = (int) Math.ceil(service[0]);
                if (service[1] < threshold) continue;
                double domProb = 0;
                for (int j = 0; j < n; j++) {
                    if (index == j) continue;
                    domProb += histDominate(dataFilled, index, j, probSmaller);
                }
                queue.add(new double[]{index, service[1], domProb});
            }
            while (queue.size() > topk) {
                queue.poll();
            }
        }

        for (int i = topk - 1; i >= 0; i--) {
            double[] cell = queue.poll();
            res[i] = (int) Math.ceil(cell[0]);
        }
        return res;
    }

    /**
     * native method
     *
     * @param dataNative origin data
     * @param dataFilled interval-filled data
     * @param bins       hist bins
     * @param topk       top-k
     * @return index of top-k services (0 start)
     */
    public int[] getSkylineNative(double[][] dataNative, double[][][] dataFilled, int bins, int topk) {
        time.clear();
        int n = dataNative.length;
        int d = dataNative[0].length;
        this.probSmaller = new double[2][n][d];

        // init hist prob
        long start = System.currentTimeMillis();
        initAll(dataNative, bins);
        long end = System.currentTimeMillis();
        time.add((end - start) / 1000.0);

        // speed up 1: calculate the prob of all possible interval
        start = System.currentTimeMillis();
        for (int i = 0; i < d; i++) {
            buildProbMatrix(dataFilled, i);
        }
        end = System.currentTimeMillis();
        time.add((end - start) / 1000.0);

        // speed up 2: select topk

        start = System.currentTimeMillis();
        // each array : {index, sky prob, dominate prob sum}
        PriorityQueue<double[]> queue = new PriorityQueue<>((o1, o2) -> {
            // 我们的目标：skyline prob越大越好，相同的选dominate prob越大越好
            // 所以小顶堆要求恰好相反：skyline prob越小越好，相同的选dominate prob 越小越好（因为每次要弹出最差的）
            if (o1[1] == o2[1]) {
                return Double.compare(o1[2], o2[2]);
            } else {
                return Double.compare(o1[1], o2[1]);
            }
        });

        for (int i = 0; i < n; i++) {
            double prob = 1.0;
            double probDominate = 0;
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    continue;
                }
                prob *= (1 - histDominate(dataFilled, j, i, probSmaller));
                probDominate += histDominate(dataFilled, i, j, probSmaller);
            }
            queue.add(new double[]{i, prob, probDominate});
            if (queue.size() > topk) {
                queue.poll();
            }
        }

        end = System.currentTimeMillis();
        time.add((end - start) / 1000.0);

        int[] res = new int[topk];
        for (int i = topk - 1; i >= 0; i--) {
            double[] cell = queue.poll();
            res[i] = (int) Math.ceil(cell[0]);
        }
        return res;
    }

    /**
     * native with threshold method
     *
     * @param dataNative origin data
     * @param dataFilled interval-filled data
     * @param bins       hist bins
     * @param topk       top-k
     * @return index of top-k services (0 start)
     */
    public int[] getSkylineNativeWithThreshold(double[][] dataNative, double[][][] dataFilled, int bins, int topk) {
        assert topk > 0;
        time.clear();
        int n = dataNative.length;
        int d = dataNative[0].length;
        this.probSmaller = new double[2][n][d];

        // init hist prob
        long start = System.currentTimeMillis();
        initAll(dataNative, bins);
        long end = System.currentTimeMillis();
        time.add((end - start) / 1000.0);

        // speed up 1: calculate the prob of all possible interval
        start = System.currentTimeMillis();
        for (int i = 0; i < d; i++) {
            buildProbMatrix(dataFilled, i);
        }
        end = System.currentTimeMillis();
        time.add((end - start) / 1000.0);

        start = System.currentTimeMillis();
        // each array : {index, sky prob, dominate prob sum}
        PriorityQueue<double[]> queue = new PriorityQueue<>((o1, o2) -> {
            // 我们的目标：skyline prob越大越好，相同的选dominate prob越大越好
            // 所以小顶堆要求恰好相反：skyline prob越小越好，相同的选dominate prob 越小越好（因为每次要弹出最差的）
            if (o1[1] == o2[1]) {
                return Double.compare(o1[2], o2[2]);
            } else {
                return Double.compare(o1[1], o2[1]);
            }
        });

        // 存储概率等于threshold的服务，用作最后筛选
        // {index, sky prob}
        List<double[]> save = new ArrayList<>();

        double threshold = 0.0;

        for (int i = 0; i < n; i++) {
            double prob = 1.0;
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    continue;
                }
                prob *= (1 - histDominate(dataFilled, j, i, probSmaller));
                if (prob < threshold) {
                    break;
                }
            }
            queue.add(new double[]{i, prob, 0});
            if (queue.size() > topk) {
                double[] cell = queue.poll();
                if (cell[1] >= threshold) {
                    save.add(new double[]{cell[0], cell[1]});
                    threshold = cell[1];
                }
            }
        }

        end = System.currentTimeMillis();
        time.add((end - start) / 1000.0);

        int[] res = new int[topk];

        if (save.size() != 0) {
            while (!queue.isEmpty()) {
                double[] cell = queue.poll();
                save.add(new double[]{cell[0], cell[1]});
            }
            for (double[] service : save) {
                int index = (int) Math.ceil(service[0]);
                if (service[1] < threshold) continue;
                double domProb = 0;
                for (int j = 0; j < n; j++) {
                    if (index == j) continue;
                    domProb += histDominate(dataFilled, index, j, probSmaller);
                }
                queue.add(new double[]{index, service[1], domProb});
            }
            while (queue.size() > topk) {
                queue.poll();
            }
        }

        for (int i = topk - 1; i >= 0; i--) {
            double[] cell = queue.poll();
            res[i] = (int) Math.ceil(cell[0]);
        }
        return res;
    }

    public double histDominate(double[][][] dataFilled, int pIndex, int qIndex, double[][][] probSmaller) {
        double prob = 1.0;
        for (int j = 0; j < dataFilled[0][0].length; j++) {
            double[] p = {dataFilled[0][pIndex][j], dataFilled[1][pIndex][j]};
            double[] q = {dataFilled[0][qIndex][j], dataFilled[1][qIndex][j]};
            double localProb = 1;
            double existProb = 1;

            if (isPoint(p) && isPoint(q)) {
                if (p[0] < q[0]) return 0;
            } else if (isPoint(p)) {
                if (p[1] <= q[0]) return 0;
                else if (p[0] < q[1]) {
                    localProb = histProb(j, qIndex, 0, pIndex, 0, probSmaller);
                    existProb = histProb(j, qIndex, 0, qIndex, 1, probSmaller);
                    prob *= (localProb / existProb);
                }
            } else if (isPoint(q)) {
                if (p[1] <= q[0]) return 0;
                else if (p[0] < q[0]) {
                    localProb = histProb(j, qIndex, 0, pIndex, 1, probSmaller);
                    existProb = histProb(j, pIndex, 0, pIndex, 1, probSmaller);
                    prob *= (localProb / existProb);
                }
            } else {
                if (p[0] >= q[1]) {
                    continue;
                } else if (p[1] <= q[0]) {
                    return 0;
                } else if (q[0] <= p[0] && p[1] <= q[1]) {
                    localProb = 0.5 * histProb(j, pIndex, 0, pIndex, 1, probSmaller)
                            * histProb(j, pIndex, 0, pIndex, 1, probSmaller);
                    localProb += histProb(j, pIndex, 0, pIndex, 1, probSmaller)
                            * histProb(j, qIndex, 0, pIndex, 0, probSmaller);
                } else if (p[0] <= q[0] && q[1] <= p[1]) {
                    localProb = 0.5 * histProb(j, qIndex, 0, qIndex, 1, probSmaller)
                            * histProb(j, qIndex, 0, qIndex, 1, probSmaller);
                    localProb += histProb(j, qIndex, 1, pIndex, 1, probSmaller)
                            * histProb(j, qIndex, 0, qIndex, 1, probSmaller);
                } else if (p[0] <= q[0] && q[0] <= p[1] && p[1] <= q[1]) {
                    localProb = 0.5 * histProb(j, qIndex, 0, pIndex, 1, probSmaller)
                            * histProb(j, qIndex, 0, pIndex, 1, probSmaller);
                } else if (q[0] <= p[0] && p[0] <= q[1] && q[1] <= p[1]) {
                    localProb = (0.5 * histProb(j, pIndex, 0, qIndex, 1, probSmaller)
                            * histProb(j, pIndex, 0, qIndex, 1, probSmaller))
                            + histProb(j, qIndex, 1, pIndex, 1, probSmaller)
                            * histProb(j, qIndex, 0, qIndex, 1, probSmaller)
                            + histProb(j, pIndex, 0, qIndex, 1, probSmaller)
                            * histProb(j, qIndex, 0, pIndex, 0, probSmaller);
                } else {
                    System.out.println("Wrong!\n");
                    System.out.println(Arrays.toString(p));
                    System.out.println(Arrays.toString(q));
                    System.exit(-1);
                }
                existProb = histProb(j, pIndex, 0, pIndex, 1, probSmaller)
                        * histProb(j, qIndex, 0, qIndex, 1, probSmaller);
                if (existProb < 1e-10) continue;
                prob = prob * localProb / existProb;
            }
        }
        return prob;
    }

    /**
     * interval [a, b]
     *
     * @param index       witch column
     * @param startIndex  which service index of a
     * @param start       the start or end of a in startIndex service
     * @param endIndex    which service index of b
     * @param end         the start or end of b in endIndex service
     * @param probSmaller probSmaller
     * @return P([a, b])
     */
    public double histProb(int index, int startIndex, int start, int endIndex, int end, double[][][] probSmaller) {
        return probSmaller[end][endIndex][index] - probSmaller[start][startIndex][index];
    }

    /**
     * to make
     * res.get(right) - res.get(left) = P([left, right])
     */
    public void buildProbMatrix(double[][][] dataFilled, int index) {
        // 排序前去重
        List<Double> attr = new ArrayList<>(
                new HashSet<>(getValidAttr(dataFilled, index)));
        attr.sort(Double::compare);
        double left = attr.get(0);
        // 构建set，统计0-x的概率
        Map<Double, Double> map = new HashMap<>();
        for (double x : attr) {
            double prob = histProb(index, left, x);
            map.put(x, prob);
        }

        // 修改index
        for (int i = 0; i < dataFilled[0].length; i++) {
            probSmaller[0][i][index] = map.get(dataFilled[0][i][index]);
            probSmaller[1][i][index] = map.get(dataFilled[1][i][index]);
        }
    }

    private static List<Double> getValidAttr(double[][][] dataFilled, int index) {
        List<Double> res = new ArrayList<>();
        for (int i = 0; i < dataFilled[0].length; i++) {
            res.add(dataFilled[0][i][index]);
            res.add(dataFilled[1][i][index]);
        }
        return res;
    }
}
