import java.util.ArrayList;

class QuickSort {

    private ArrayList<Pair<String, Double>> array;

    /**
     * Sort an array highest to lowest using quicksort.
     *
     * @param inputArr imput array to sort.
     */
    void sort(ArrayList<Pair<String, Double>> inputArr) {

        if (inputArr == null || inputArr.size() == 0) {
            return;
        }

        this.array = inputArr;
        quickSort(0, inputArr.size() - 1);
    }

    /**
     * Sort the array using a pivot as a reference.
     * The pivot will be in the position in which would be if the array were sorted
     * with all the bigger numbers than the pivot in the smaller positions and all
     * the smaller numbers than the pivot in the bigger positions.
     *
     * @param  lowerIndex lower index to sort the array.
     * @param  higherIndex higher index to sort the array.
     */
    private void quickSort(int lowerIndex, int higherIndex) {
        int i = lowerIndex;
        int j = higherIndex;

        Double pivot = array.get(lowerIndex + (higherIndex - lowerIndex) / 2).getSecond();

        while (i <= j) {

            while (array.get(i).getSecond() > pivot) {
                ++i;
            }

            while (array.get(j).getSecond() < pivot) {
                --j;
            }

            if (i <= j) {
                exchangeNumbers(i, j);
                ++i;
                --j;
            }
        }

        if (lowerIndex < j) {
            quickSort(lowerIndex, j);
        }

        if (i < higherIndex) {
            quickSort(i, higherIndex);
        }
    }

    /**
     * Swap the two positions of the array given as a parameter.
     *
     * @param  i first position.
     * @param  j second position.
     */
    private void exchangeNumbers(int i, int j) {

        Pair<String, Double> temp = array.get(i);
        array.set(i, array.get(j));
        array.set(j, temp);
    }

}