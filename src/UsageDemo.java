import skyline.CompleteSkyline;
import skyline.ISkySel;
import tool.DataHandler;
import tool.Evaluator;
import tool.FileHandler;
import tool.KNNFilled;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UsageDemo {
    public static void main(String[] args) {
        // we use histogram to build pdf, bins is the number of columns
        int bins = 20;
        // the number of skyline that iskysel returns
        int topk = 20;
        // the number of neighbours in KNN
        int k = 5;

        // Step 1. Read File
        double[][] dataOrigin = FileHandler.readData("data/qws_rate_0.1.txt", false);
        int n = dataOrigin.length, d = dataOrigin[0].length;

        // Step 2. Normalization
        boolean[] smallerBetter = new boolean[d];
        smallerBetter[0] = true;
        smallerBetter[7] = true;
        double[][] dataOriginNorm = DataHandler.dataNormalized(dataOrigin, smallerBetter);

        // Step 3. Impute
        // You can use KNN to get them
        double[][][] dataOriginFilled = new KNNFilled().knnFilled(dataOriginNorm, k);
        double[][][] dataOriginInterval = DataHandler.buildIntervalData(Arrays.asList(dataOriginFilled));

        // Or you can use your own impute method
        List<double[][]> save = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            save.add(FileHandler.readData("data/qws_m_rate_0.1_time_0_prt_" + i + ".txt", false));
        }
        double[][][] dataOriginInterval2 = DataHandler.buildIntervalData(save);

        // Step 3. Get Skyline
        ISkySel iSkySel = new ISkySel();
        int[] skylineOriginPredict = iSkySel.getSkyline(dataOriginNorm, dataOriginInterval, bins, topk);
        System.out.println(Arrays.toString(skylineOriginPredict));

        // Or you maybe want to slice this data
        // We prove a slice method: DataHandler.dataSlice(data, start, end, dimension)

        // Step 1. Tell Us the QoS Selected
        boolean[] dimensionSelected = new boolean[9];
        dimensionSelected[0] = true;
        dimensionSelected[1] = true;
        dimensionSelected[3] = true;
        dimensionSelected[6] = true;
        dimensionSelected[7] = true;
        int start = 1001, end = 2000;
        double[][] dataSlice = DataHandler.dataSlice(dataOrigin, start, end, dimensionSelected);

        // Step 2. Normalization
        double[][] dataSliceNorm = DataHandler.dataNormalized(dataSlice, new boolean[]{true, false, false, false, true});

        // Step 3. Impute
        double[][][ ] dataSliceFilled = new KNNFilled().knnFilled(dataSliceNorm, k);
        double[][][] dataSliceInterval = DataHandler.buildIntervalData(Arrays.asList(dataSliceFilled));

        // Step 4. Skyline
        ISkySel iSkySel2 = new ISkySel();
        int[] skylineSlicePredicted = iSkySel2.getSkyline(dataSliceNorm, dataSliceInterval, bins, topk);
        System.out.println(Arrays.toString(skylineSlicePredicted));

        // You can also get the accuracy by Evaluator.precision
        double[][] dataComplete = FileHandler.readData("data/qws_normal.txt", false);
        int[] skylineOriginRight = new CompleteSkyline().sortedFilterSkyline(dataComplete);
        System.out.println("Origin Accuracy: " + new Evaluator().precision(skylineOriginRight, skylineOriginPredict));

        double[][] dataCompleteSlice = DataHandler.dataSlice(dataComplete, start, end, dimensionSelected);
        int[] skylineSelectRight = new CompleteSkyline().sortedFilterSkyline(dataCompleteSlice);
        System.out.println("Slice Accuracy: " + new Evaluator().precision(skylineSelectRight, skylineSlicePredicted));
    }
}
