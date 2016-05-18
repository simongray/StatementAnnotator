package statistics;

import java.util.*;

/**
 * "Lazy-loading" immutable statistics (= once calculated, the same values are re-used).
 */
public class Statistics
{
    private List<Double> data;
    private Map<Type, Double> values = new HashMap<>();
    private enum Type {
        MEAN,
        VARIANCE,
        STANDARD_DEVIATION,
        MEDIAN
    }

    public Statistics(List<Double> data) {
        this.data = new ArrayList<>(data);  // create a shallow copy to preserve immutability
    }

    public double getMean()
    {
        if (!values.containsKey(Type.MEAN)) {
            double sum = data.stream().mapToDouble(Double::doubleValue).sum();
            double mean = sum / data.size();
            values.put(Type.MEAN, mean);
        }

        return values.get(Type.MEAN);
    }

    public double getVariance()
    {
        if (!values.containsKey(Type.VARIANCE)) {
            double mean = getMean();
            double squaredDifferences = data
                    .stream()
                    .reduce(0.0, (a, b) -> a + (mean - b) * (mean - b));
            double variance = squaredDifferences / data.size();
            values.put(Type.VARIANCE, variance);
        }

        return values.get(Type.VARIANCE);
    }

    public double getStandardDeviation()
    {
        if (!values.containsKey(Type.STANDARD_DEVIATION)) {
            double standardDeviation = Math.sqrt(getVariance());
            values.put(Type.STANDARD_DEVIATION, standardDeviation);
        }

        return values.get(Type.STANDARD_DEVIATION);
    }

    public double getMedian()
    {
        if (!values.containsKey(Type.MEDIAN)) {
            Collections.sort(data);

            if (data.size() % 2 == 0) {
                double median = (data.get((data.size() / 2) - 1) + data.get(data.size() / 2)) / 2.0;
                values.put(Type.MEDIAN, median);
            } else {
                double median = data.get(data.size() / 2);
                values.put(Type.MEDIAN, median);
            }
        }

        return values.get(Type.MEDIAN);
    }
}
