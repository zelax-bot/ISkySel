import skyline.CompleteSkyline;
import skyline.ISkySel;
import tool.DataHandler;
import tool.Evaluator;
import tool.FileHandler;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Main {
    public static File[] readFileGUI(String title) {
        JFileChooser fDialog = new JFileChooser();
        fDialog.setCurrentDirectory(new File("./"));
        // set title
        fDialog.setDialogTitle(title);
        // multiple selectoin
        fDialog.setMultiSelectionEnabled(true);
        int returnVal = fDialog.showOpenDialog(null);
        if (JFileChooser.APPROVE_OPTION == returnVal) {
            return fDialog.getSelectedFiles();
        } else {
            System.out.println("Choose File Stopped!");
            System.exit(0);
            return null;
        }
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.ENGLISH);

        String message = "Please Choose One Complete Dataset";
        System.out.println(message);
        File[] fileComplete = readFileGUI(message);
        double[][] dataComplete = FileHandler.readData(fileComplete[0]);

        message = "Please Choose Origin Incomplete Dataset";
        System.out.println(message);
        File[] filesNative = readFileGUI(message);
        double[][] dataNative = FileHandler.readData(filesNative[0]);

        message = "Please Choose Multiple Impute Dataset";
        System.out.println(message);
        File[] files = readFileGUI(message);
        List<double[][]> dataAll = new ArrayList<>();
        for (File file : files) {
            dataAll.add(FileHandler.readData(file));
        }
        double[][][] dataFilled = DataHandler.buildIntervalData(dataAll);

        ISkySel skyline = new ISkySel();
        int[] skylinePred = skyline.getSkyline(dataNative, dataFilled, 20, 10);
        int[] rightSkyline = new CompleteSkyline().sortedFilterSkyline(dataComplete);

        double precision = new Evaluator().precision(rightSkyline, skylinePred);
        System.out.println("precision: " + precision);
    }
}
