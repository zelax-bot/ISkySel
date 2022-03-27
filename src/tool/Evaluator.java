package tool;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/***
 * 评价函数，包含精准率，召回率
 *      rep和div
 */
public class Evaluator {
    public float precision(int[] right, int[] predicted) {
        Set<Integer> rightSet = Arrays.stream(right).boxed().collect(Collectors.toSet());
        Set<Integer> predictedSet = Arrays.stream(predicted).boxed().collect(Collectors.toSet());
        rightSet.retainAll(predictedSet);
        return (float) (1.0 * rightSet.size() / predicted.length);
    }

    public float recall(int[] right, int[] predicted) {
        Set<Integer> rightSet = Arrays.stream(right).boxed().collect(Collectors.toSet());
        Set<Integer> predictedSet = Arrays.stream(predicted).boxed().collect(Collectors.toSet());
        rightSet.retainAll(predictedSet);
        return (float) (1.0 * rightSet.size() / right.length);
    }
}
