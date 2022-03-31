## Welcome to ISkySel Pages

This repository matains the Java implementation of interval-valued skyline service selection on the incomplete QoS dataset.

You can read more information about the algorithm from our publication:

* Author, "paper name",Journal Name, state, year

### Dependencies

Java 1.8 ([https://www.oracle.com/](https://www.oracle.com/))

### Usage

#### Step 1. Read Data

We provide two methods to read unfilled data and filled respectively: ```

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

### Feedback

If you find any bugs or errors, please post to our [issue page](https://github.com/jhzhang98/ISkySel/issues). Also for any enquire, you can drop an e-mail to our us [our mail here](mailto:jh.zhang98@qq.com).
