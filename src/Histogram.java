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
    HashMap<Integer, Double> mostCommonColorsBucketed;

    double mean;

    boolean isObjectHistogram;

    public Histogram() {
        this.mostCommonColorsBucketed = new HashMap<>();
        this.mostCommonColors = new HashSet<>();
        this.mostCommonColorsInOrder = new ArrayList<>();
        this.mostCommonColorsGrouped = new ArrayList<>();
        this.mostCommonColorsWithRange = new HashSet<>();
        this.rawTable = new Hashtable<>();
        this.ratioTable = new Hashtable<>();
        this.totalPixels = 0.0;
        this.isObjectHistogram = true;
    }

    // Used for the matching image objects
    // Gonna make the most common colors grouped buckets identical
    public Histogram(List<Integer> mostCommonColorsInOrder) {
        this.mostCommonColorsBucketed = new HashMap<>();
        this.mostCommonColors = new HashSet<>();
        this.mostCommonColorsInOrder = mostCommonColorsInOrder;
        this.mostCommonColorsGrouped = new ArrayList<>();
        this.mostCommonColorsWithRange = new HashSet<>();
        this.rawTable = new Hashtable<>();
        this.ratioTable = new Hashtable<>();
        this.totalPixels = 0.0;
        this.isObjectHistogram = false;
    }

    public Hashtable<Integer, Integer> getRawTable() {
        return rawTable;
    }

    public Hashtable<Integer, Double> getRatioTable() {
        return ratioTable;
    }

    public void calculateTotalPixels(int topLeftCornerX, int topLeftCornerY, int bottomRightCornerX, int bottomRightCornerY) {
        totalPixels = (double) (bottomRightCornerX - topLeftCornerX) * (bottomRightCornerY - topLeftCornerY);
    }

    public List<Double> getMostCommonValuesGrouped() {
        return mostCommonValuesGrouped;
    }

    public HashMap<Integer, Double> getMostCommonColorsBucketed() {
        return mostCommonColorsBucketed;
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

    public void initRatioTable() {
        List<Double> values = new ArrayList<>();
        //System.out.println("------------Init Table-----------");
        //System.out.println(mostCommonColorsInOrder.toString());
        rawTable.forEach((key, value) -> {
            double ratioValue = value / totalPixels;
            values.add(ratioValue);
            ratioTable.put(key, ratioValue);
        });
        values.sort(Collections.reverseOrder());

        for (Double value : values) {
            for (Map.Entry<Integer, Double> e : ratioTable.entrySet()) {
                Double value1 = e.getValue();
                if (Objects.equals(value, value1) && !mostCommonColors.contains(e.getKey())) {
                    if (isObjectHistogram) {
                        mostCommonColorsInOrder.add(e.getKey());
                    }
                    mostCommonColors.add(e.getKey());
                }
            }
        }

        Set<Integer> colorsAdded = new HashSet<>();

        int window = getRangeOfColor();
        //System.out.println("Hmm: " + this.getMostCommonColorsInOrder());
        for (int i = 0; i < mostCommonColorsInOrder.size(); i++) {
            int color = mostCommonColorsInOrder.get(i);
            if (!colorsAdded.contains(color)) {
                mostCommonColorsBucketed.put(color, 0.0);
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
                            mostCommonColorsBucketed.put(color, mostCommonColorsBucketed.get(color) + ratioTable.get(color2));
                        }
                    }
                }
            }
        }

        //System.out.println("My experiment: " + mostCommonColorsBucketed);
        List<Double> values2 = new ArrayList<>();
        mostCommonColorsBucketed.forEach((key, value) -> {
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
            for (Map.Entry<Integer, Double> e : mostCommonColorsBucketed.entrySet()) {
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
        System.out.println("Size of ratio table: " + ratioTable.size());
        //System.out.println("Most common colors in order: " + mostCommonColorsInOrder);
        //System.out.println("Most common colors in order with range: " + mostCommonColorsWithRange);*/
    }

    public void printOutMostCommonColorsAndTheirPercentages() {
        for (Integer color : mostCommonColorsInOrder) {
            System.out.print(" " + color + ": " + getRatioTable().get(color) + ",");
        }
        System.out.println();
    }
}
