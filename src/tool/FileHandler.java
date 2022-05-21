package tool;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {
    /**
     * read file from disk, data is separated by ',' or ' '
     *
     * @param filePath file path
     * @param withHead if true -> first line must be: rowNum,colNum or rowNum colNum
     * @return default is float[][]
     */
    public static double[][] readData(String filePath, boolean withHead) {
        if (withHead)
            return readFileWithHead(filePath);
        File file = new File(filePath);
        return readData(file);
    }

    public static double[][] readFileWithHead(String filePath) {
        File file = new File(filePath);
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String firstLine = br.readLine();
            String[] tmp = firstLine.split(",|[ ]");
            int row = Integer.parseInt(tmp[0]);
            int col = Integer.parseInt(tmp[1]);
            double[][] data = new double[row][col];
            String line = br.readLine();
            int i = 0;
            while (null != line) {
                String[] dataLine = line.split(",|[ ]");
                for (int j = 0; j < col; j++)
                    data[i][j] = Double.parseDouble(dataLine[j]);
                i++;
                line = br.readLine();
            }
            return data;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static double[][] readData(File file){
        List<double[]> data = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            while (null != line) {
                List<Float> lineNum = new ArrayList<>();
                String[] dataLine = line.split(",|[ ]|[\t]");
                for (int i = 0; i < dataLine.length; i++) {
                    String ele = dataLine[i];
                    if (i == dataLine.length - 1 && ele.length() == 0)
                        continue;
                    if (ele.equals("nan"))
                        lineNum.add(-1f);
                    else
                        lineNum.add(Float.parseFloat(ele));
                }
                double[] lineData = new double[lineNum.size()];
                for (int j = 0; j < lineNum.size(); j++)
                    lineData[j] = lineNum.get(j);
                data.add(lineData);
                line = br.readLine();
            }
            double[][] res = new double[data.size()][data.get(0).length];
            for (int i = 0; i < data.size(); i++)
                res[i] = data.get(i);
            return res;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveFile(double[][] data, String path, boolean writeHead, String delimiter) {
        try {
            File file = new File(path);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            if (writeHead)
                writer.write("" + data.length + delimiter + data[0].length + '\n');
            for (double[] datum : data) {
                StringBuilder line = new StringBuilder();
                for (int j = 0; j < data[0].length; j++) {
                    line.append(datum[j]).append(delimiter);
                }
                line.replace(line.length() - 1, line.length(), "\n");
                writer.write(String.valueOf(line));
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
