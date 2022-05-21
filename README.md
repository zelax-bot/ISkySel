## Welcome to ISkySel Pages

This repository matains the Java implementation of interval-valued skyline service selection on the incomplete QoS dataset.

You can read more information about the algorithm from our publication:

* Interval-valued Skyline Web Service Selection on Incomplete QoS, submmited for ICWS 2022.

## Dependencies

Java 1.8 ([https://www.oracle.com/](https://www.oracle.com/))

## Usage

### One Method: Use Instance

#### Step 1. Read Data

We provide two methods to read unfilled data and filled respectively:

```
FileHandler.readData(): read data from current file
DataHandler.buildIntervalData(): build interval data from imputed data
```

#### Step 2. Get Skyline

We provide ISkySel, Native and Threshold method, which are used in a similar way：

```
# ISkySel
int[] skyline1 = new ISkySel().getSkyline(dataSparse, dataInterval, bins, topk);

# Threshold
int[] skyline2 = new ISkySel().getSkylineNativeWithThreshold(dataSparse, dataInterval, bins, topk);

# Native
int[] skyline3 = new ISkySel().getSkylineNative(dataSparse, dataInterval, bins, topk);
```

#### Demo

We prove two demos (src/):

```
usageDemo: the usage of three methods
intervalDemo: show the importance of PDF and the condition of PDF is uniform distribution or not
```

### Method 2: Use GUI

We prove one gui demo (Main.java) to use ISkySel:

#### Step 1. Select Complete Dataset
![step1](fig/step1.jpg)
#### Step 2. Select Incomplete Dataset
![step2](fig/step2.jpg)
#### Step 4. Select Multiple Impute Dataset
Attention! In this step, you should choose at least two files.
![step3](fig/step3.jpg)
## Feedback

If you find any bugs or errors, please post to our [issue page](https://github.com/jhzhang98/ISkySel/issues). Also for any enquire, you can drop an e-mail to our us [our mail here](mailto:1910644713@qq.com).
