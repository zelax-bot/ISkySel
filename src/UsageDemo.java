import skyline.CompleteSkyline;
import skyline.ISkySel;
import tool.DataHandler;
import tool.Evaluator;
import tool.FileHandler;

import java.util.ArrayList;
import java.util.List;

public class UsageDemo {
    public static void main(String[] args) {
        Evaluator ev = new Evaluator();

        double[][] dataOrigin = FileHandler.readData("data/qws_normal.txt", false);
        double[][] dataSparse = FileHandler.readData("data/qws_rate_0.1_time_0.txt", false);
        assert dataSparse == null;

        List<double[][]> dataImpute = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            dataImpute.add(FileHandler.readData("data/qws_m_rate_0.1_time_0_prt_" + i + ".txt", false));
        }
        double[][][] dataInterval = DataHandler.buildIntervalData(dataImpute);

        int[] skyline = new CompleteSkyline().sortedFilterSkyline(dataOrigin);

        int bins = 20, topk = 50;

        long start = System.currentTimeMillis();
        int[] skylinePredictNative = new ISkySel().getSkylineNative(dataSparse, dataInterval, bins, topk);
        long end = System.currentTimeMillis();
        System.out.println("cost: " + (end - start) / 1000.0 + "s\t pre: " + ev.precision(skyline, skylinePredictNative));

        start = System.currentTimeMillis();
        int[] skylinePredictThreshold = new ISkySel().getSkylineNativeWithThreshold(dataSparse, dataInterval, bins, topk);
        end = System.currentTimeMillis();
        System.out.println("cost: " + (end - start) / 1000.0 + "s\t pre: " + ev.precision(skyline, skylinePredictThreshold));

        start = System.currentTimeMillis();
        int[] skylinePredict = new ISkySel().getSkyline(dataSparse, dataInterval, bins, topk);
        end = System.currentTimeMillis();
        System.out.println("cost: " + (end - start) / 1000.0 + "s\t pre: " + ev.precision(skyline, skylinePredict));
    }
}
