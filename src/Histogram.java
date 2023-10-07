import java.util.*;

public class Histogram {

    private Double totalPixels;
    Hashtable<Integer, Integer> rawTable;
    Hashtable<Integer, Double> ratioTable;
    Set<Integer> mostCommonColors;
    Set<Integer> mostCommonColorsWithRange;
    List<Integer> mostCommonColorsInOrder;
    List<Integer> mostCommonColorsGrouped;
    List<Double> mostCommonValuesGrouped;

    double mean;

    public Histogram() {
        this.mostCommonColors = new HashSet<>();
        this.mostCommonColorsInOrder = new ArrayList<>();
        this.mostCommonColorsGrouped = new ArrayList<>();
        this.mostCommonColorsWithRange = new HashSet<>();
        this.rawTable = new Hashtable<>();
        this.ratioTable = new Hashtable<>();
        this.totalPixels = 0.0;
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

    public List<Double> getMostCommonValuesGrouped() {
        return mostCommonValuesGrouped;
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

    public List<Integer> getMostCommonColorsGrouped() {
        return mostCommonColorsGrouped;
    }

    public int getRangeOfColor() {
        return 9;
    }

    public void initRatioTable(boolean isObjectHistogram) {
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
/*        mostCommonValues.add(values.get(0));
        Double prevValue = values.get(0);
        for ( int i = 1; i < values.size(); i++ ) {
            Double currValue = values.get(i);
            if ( (currValue / prevValue) < 0.5 || currValue < 0.01 ) {
                break;
            } else {
                mostCommonValues.add(currValue);
            }
        }*/
/*        for (Double value : values) {
            //sum += value;
            mostCommonValues.add(value);
            if (value < 0.005) {
                break;
            }
        }*/
        //for (int i = 0; i < numberOfTopValuesToTrack; i++) {
        //    mostCommonValues.add(values.get(i));
        //}

        //List<Double> mostCommonValues = Arrays.asList(values.get(0), values.get(1), values.get(2), values.get(3));
        //System.out.println("Most common values: " + mostCommonValues);
        //System.out.println(mostCommonValues.stream().mapToDouble(Double::doubleValue).sum());

        System.out.println("------------Init Table-----------");
        for (Double value : values) {
            for (Map.Entry<Integer, Double> e : ratioTable.entrySet()) {
                Double value1 = e.getValue();
                if (Objects.equals(value, value1) && !mostCommonColors.contains(e.getKey())) {
                    //System.out.println(e.getKey());
                    mostCommonColorsInOrder.add(e.getKey());
                    mostCommonColors.add(e.getKey());
                }
            }
        }

        Set<Integer> colorsAdded = new HashSet<>();
        HashMap<Integer, Double> map = new HashMap<>();

        int window = getRangeOfColor();
        for (int i = 0; i < mostCommonColorsInOrder.size(); i++) {
            int color = mostCommonColorsInOrder.get(i);
            if (!colorsAdded.contains(color)) {
                map.put(color, 0.0);
                int color2;

                for (int j = -window; j <= window; j++) {
                    if (color + j > 360) {
                        color2 = j - 1;
                        //mostCommonColorsWithRange.add(j - 1);
                    } else if (color + j < 0) {
                        color2 = 360 + (color + j + 1);
                        //mostCommonColorsWithRange.add(360 + (color + j + 1));
                    } else {
                        color2 = color + j;
                        //mostCommonColorsWithRange.add(color + j);
                    }

                    if (!colorsAdded.contains(color2)) {
                        if (ratioTable.containsKey(color2)) {
                            colorsAdded.add(color2);
                            map.put(color, map.get(color) + ratioTable.get(color2));
                        }
                    }
                }
            }
        }

        System.out.println("My experiment: " + map);
        List<Double> values2 = new ArrayList<>();
        map.forEach((key, value) -> {
            values2.add(value);
        });
        values2.sort(Collections.reverseOrder());

        Double prevValue = values2.get(0);
        mostCommonValuesGrouped = new ArrayList<>();
        mostCommonValuesGrouped.add(prevValue);
        for (int i = 1; i < values2.size(); i++) {
            Double currValue = values2.get(i);
            if ((((currValue / prevValue) < 0.5 && currValue < 0.07 ) || currValue < 0.02) && isObjectHistogram) {
                break;
            } else {
                mostCommonValuesGrouped.add(currValue);
            }
        }

        for (Double val : mostCommonValuesGrouped) {
            for (Map.Entry<Integer, Double> e : map.entrySet()) {
                if (Objects.equals(e.getValue(), val)) {
                    mostCommonColorsGrouped.add(e.getKey());
                    int color = e.getKey();
                    for (int j = -window; j <= window; j++) {
                        if (color + j > 360) {
                            mostCommonColorsWithRange.add(j - 1);
                        } else if (color + j < 0) {
                            mostCommonColorsWithRange.add(360 + (color + j + 1));
                        } else {
                            mostCommonColorsWithRange.add(color + j);
                        }
                    }
                }
            }
        }




/*        int range = 5;
        for (int i = 0; i < mostCommonColorsInOrder.size(); i++) {
            int color = mostCommonColorsInOrder.get(i);
            for (int j = -range; j <= range; j++) {
                if (color + j > 360) {
                    mostCommonColorsWithRange.add(j - 1);
                } else if (color + j < 0) {
                    mostCommonColorsWithRange.add(360 + (color + j + 1));
                } else {
                    mostCommonColorsWithRange.add(color + j);
                }
            }
        }*/

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
