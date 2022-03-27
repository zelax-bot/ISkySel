import skyline.CompleteSkyline;
import skyline.ISkySel;
import sun.awt.windows.WPrinterJob;
import tool.DataHandler;
import tool.Evaluator;
import tool.FileHandler;

import java.util.ArrayList;
import java.util.List;

public class demo {
    public static void main(String[] args) {
        Evaluator ev = new Evaluator();

        double[][] dataOrigin = FileHandler.readData("file/qws_normal.txt", false);
        double[][] dataSparse = FileHandler.readData("file/m_rate_0.1_time_0.txt", false);

        List<double[][]> dataImpute = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            dataImpute.add(FileHandler.readData("file/qws_m_rate_0.1_time_0_prt_" + i + ".txt", false));
        }
        double[][][] dataInterval = DataHandler.buildIntervalData(dataImpute);

        int[] skyline = new CompleteSkyline().sortedFilterSkyline(dataOrigin);

        long start = System.currentTimeMillis();
        int[] skylinePredict = new ISkySel().getSkyline(dataSparse, dataInterval, 20, 50);
        long end = System.currentTimeMillis();

        System.out.println("cost: " + (end - start) / 1000.0 + "s\t pre: " + ev.precision(skyline, skylinePredict));
    }
}
