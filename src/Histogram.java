import java.util.*;

public class Histogram {

    private Double totalPixels;
    Hashtable<Integer, Integer> rawTable;
    Hashtable<Integer, Double> ratioTable;
    Set<Integer> mostCommonColors;
    Set<Integer> mostCommonColorsWithRange;
    List<Integer> mostCommonColorsInOrder;

    double mean;

    public Histogram() {
        this.mostCommonColors = new HashSet<>();
        this.mostCommonColorsInOrder = new ArrayList<>();
        mostCommonColorsWithRange = new HashSet<>();
        this.rawTable = new Hashtable<>();
        this.ratioTable = new Hashtable<>();
        totalPixels = 0.0;
    }

    public Hashtable<Integer, Integer> getRawTable() {
        return rawTable;
    }

    public Hashtable<Integer, Double> getRatioTable() {
        return ratioTable;
    }

    public void calculateTotalPixels(int topLeftCornerX, int topLeftCornerY, int bottomRightCornerX, int bottomRightCornerY) {
        /*System.out.println("Calculating");
        System.out.println(topLeftCornerX);
        System.out.println(topLeftCornerY);
        System.out.println(bottomRightCornerX);
        System.out.println(bottomRightCornerY);*/

        totalPixels = (double) (bottomRightCornerX - topLeftCornerX) * (bottomRightCornerY - topLeftCornerY);
        //System.out.println(totalPixels);

        /*for (Integer value : rawTable.values()) {
            totalPixels+=value;
        }

        System.out.println(totalPixels);*/
    }

    public List<Integer> getColorsInOrder() {
        List<Integer> colorsInOrder = new ArrayList<>();
        for (Map.Entry<Integer, Double> e : ratioTable.entrySet()) {
            colorsInOrder.add(e.getKey());
        }

        Collections.sort(colorsInOrder);
        return colorsInOrder;
    }

    public void calculateMean() {
        double sum = 0;
        int divisor = 0;
/*        for (int color : rawTable) {
            sum += color * rawTable.get(color);
            divisor += rawTable.get(color);
        }*/
        for (Map.Entry<Integer, Integer> e : rawTable.entrySet()) {
            Integer color = e.getKey();
            Integer occurrences = e.getValue();
            sum += color * occurrences;
            divisor += occurrences;
        }

        mean = sum / divisor;
    }

    public double getMean() {
        return mean;
    }

    public Set<Integer> getMostCommonColors() {
        return mostCommonColors;
    }

    public Set<Integer> getMostCommonColorsWithRange() {
        return mostCommonColorsWithRange;
    }


    public List<Integer> getMostCommonColorsInOrder() {
        return mostCommonColorsInOrder;
    }

    public void initRatioTable() {
        List<Double> values = new ArrayList<>();
        rawTable.forEach((key, value) -> {
            double ratioValue = value / totalPixels;
            values.add(ratioValue);
            ratioTable.put(key, ratioValue);
        });
        values.sort(Collections.reverseOrder());
        //System.out.println(values);

        //int numberOfTopValuesToTrack = 20;
        List<Double> mostCommonValues = new ArrayList<>();
        int mostCommonValuesSize = (int) (0.15 * ratioTable.size());
        double sum = 0.0;
        for (Double value : values) {
            //sum += value;
            mostCommonValues.add(value);
            if ( value < 0.01 ) {
                break;
            }
        }
        //for (int i = 0; i < numberOfTopValuesToTrack; i++) {
        //    mostCommonValues.add(values.get(i));
        //}

        //List<Double> mostCommonValues = Arrays.asList(values.get(0), values.get(1), values.get(2), values.get(3));
        //System.out.println("Most common values: " + mostCommonValues);
        //System.out.println(mostCommonValues.stream().mapToDouble(Double::doubleValue).sum());

        System.out.println("------------Init Table-----------");
        for (Double value : mostCommonValues) {
            for (Map.Entry<Integer, Double> e : ratioTable.entrySet()) {
                Double value1 = e.getValue();
                if (Objects.equals(value, value1) && !mostCommonColors.contains(e.getKey())) {
                    //System.out.println(e.getKey());
                    mostCommonColorsInOrder.add(e.getKey());
                    mostCommonColors.add(e.getKey());
                }
            }
        }

        int range = 5;
        for ( int i = 0; i < mostCommonColorsInOrder.size(); i++) {
            int color = mostCommonColorsInOrder.get(i);
            for (int j = -range; j <= range; j++) {
                if ( color + j > 360 ) {
                    mostCommonColorsWithRange.add(j - 1);
                } else if ( j < 0 ) {
                    mostCommonColorsWithRange.add(360 + j + 1);
                } else {
                    mostCommonColorsWithRange.add(color + j);
                }
            }
        }

        /*for (int i = 5; i < mostCommonColorsInOrder.size(); i++) {
            mostCommonColorsWithRange.add(mostCommonColorsInOrder.get(i));
        }*/

        //System.out.println("Most common colors: " + mostCommonColors.toString());
/*        System.out.println("Size of most common colors: " + mostCommonColors.size());
        //System.out.println(mostCommonColors.toString());
        System.out.println("Size of ratio table: " + ratioTable.size());*/
        System.out.println("Most common colors in order: " + mostCommonColorsInOrder);
        System.out.println("Most common colors in order with range: " + mostCommonColorsWithRange);
    }

    public void printOutMostCommonColorsAndTheirPercentages() {
        for (Integer color : mostCommonColorsInOrder) {
            System.out.print(" " + color + ": " + getRatioTable().get(color) + ",");
        }
        System.out.println();
    }
}
