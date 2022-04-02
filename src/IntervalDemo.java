import java.util.Arrays;
import java.util.Random;

public class IntervalDemo {
    /**
     * the prob of interval [left, right]
     */
    private static double histProb(double[] cube, double[] freq, double[] prefixSum, double left, double right) {
        if (left == right)
            return 0;
        double gap = (cube[cube.length - 1] - cube[0]) / (cube.length - 1);
        int leftIndex = (Math.abs(left - cube[cube.length - 1]) <= 1e-5) ? cube.length - 2 : (int) ((left - cube[0]) / gap);
        int rightIndex = (Math.abs(right - cube[cube.length - 1]) <= 1e-5) ? cube.length - 2 : (int) ((right - cube[0]) / gap);

        rightIndex = Math.min(freq.length - 1, rightIndex);
        leftIndex = Math.min(freq.length - 1, leftIndex);
        rightIndex = Math.max(0, rightIndex);
        leftIndex = Math.max(0, leftIndex);

        if (leftIndex == rightIndex)
            return freq[leftIndex] * (right - left);
        double prob = freq[leftIndex] * (cube[leftIndex + 1] - left);
        prob += freq[rightIndex] * (right - cube[rightIndex]);
        prob += (gap * (prefixSum[rightIndex - 1] - prefixSum[leftIndex]));
        return prob;
    }

    /**
     * only one condition !!!
     * q[0] < p[0] < q[1] < p[1]
     * q0 ---------- q1
     * p0 ------------ p1
     */
    private static double dominateProb(double[] cube, double[] freq, double[] prefixSum, double[] p, double[] q) {
        return (0.5 * histProb(cube, freq, prefixSum, p[0], q[1]) * histProb(cube, freq, prefixSum, p[0], q[1]))
                + histProb(cube, freq, prefixSum, q[1], p[1]) * histProb(cube, freq, prefixSum, q[0], q[1])
                + histProb(cube, freq, prefixSum, p[0], q[1]) * histProb(cube, freq, prefixSum, q[0], p[0]);
    }

    public static void main(String[] args) {
        // build uniform pdf
        int bins = 20;
        double[] cubeU = new double[bins + 1];
        double[] freqU = new double[bins];
        double[] prefixSumU = new double[bins];
        for (int i = 1; i <= bins; i += 1) {
            cubeU[i] = i;
            freqU[i - 1] = 1.0 / bins;
            prefixSumU[i - 1] = freqU[i - 1] * (i + 1);
        }

        // build not uniform pdf
        double[] cubeNU = new double[bins + 1];
        double[] freqNU = new double[bins];
        double[] prefixSumNU = new double[bins];
        int remain = 1000;
        double total = 1000.0;
        for (int i = 1; i <= bins; i += 1) {
            cubeNU[i] = i;
            int rand = remain / 2;
            remain -= rand;
            if (i == 1) {
                freqNU[i - 1] = rand * 1.0 / total;
                prefixSumNU[i - 1] = freqNU[i - 1];
            } else if (i < bins) {
                freqNU[i - 1] = rand * 1.0 / total;
                prefixSumNU[i - 1] = prefixSumNU[i - 2] + freqNU[i - 1];
            } else {
                freqNU[i - 1] = remain / total;
                prefixSumNU[i - 1] = prefixSumNU[i - 2] + freqNU[i - 1];
            }
        }

        for (int i = 0; i < 5; i++) {
            double start = 4 * new Random().nextDouble();
            double gap = 3;
            double[] x = {start, 2 * gap + start};
            double[] y = {gap + start, 3 * gap + start};
            double[] z = {2 * gap + start, 4 * gap + start};

            System.out.println("===========random iteration: " + (i + 1) + "===========");
            System.out.println("x: " + Arrays.toString(x));
            System.out.println("y: " + Arrays.toString(y));
            System.out.println("z: " + Arrays.toString(z));

            double p1U = dominateProb(cubeU, freqU, prefixSumU, y, x);
            double p2U = dominateProb(cubeU, freqU, prefixSumU, z, y);
            System.out.println("uniform: P(y>=x)=" + p1U + "\t P(z>=y)=" + p2U);

            double p1NU = dominateProb(cubeNU, freqNU, prefixSumNU, y, x);
            double p2NU = dominateProb(cubeNU, freqNU, prefixSumNU, z, y);
            System.out.println("not uniform: P(y>=x)=" + p1NU + "\t P(z>=y)=" + p2NU + "\n");
        }
    }
}
