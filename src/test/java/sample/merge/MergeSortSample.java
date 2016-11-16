package sample.merge;

import java.util.*;

public class MergeSortSample {
    public static int[] mergeSort(int[] arr) {
        if (arr.length == 1) {
            return arr;
        }
        int half = arr.length / 2;
        int[] arr1 = new int[half];
        int[] arr2 = new int[arr.length - half];
        System.arraycopy(arr, 0, arr1, 0, arr1.length);
        System.arraycopy(arr, half, arr2, 0, arr2.length);
        arr1 = mergeSort(arr1);
        arr2 = mergeSort(arr2);
        return mergeSortSub(arr1, arr2);
    }

    private static int[] mergeSortSub(int[] arr1, int[] arr2) {
        int[] result = new int[arr1.length + arr2.length];
        int i = 0;
        int j = 0;
        int k = 0;
        while (true) {
            if (arr1[i] < arr2[j]) {
                result[k] = arr1[i];
                if (++i > arr1.length - 1) {
                    break;
                }
            } else {
                result[k] = arr2[j];
                if (++j > arr2.length - 1) {
                    break;
                }
            }
            k++;
        }
        for (; i < arr1.length; i++) {
            result[++k] = arr1[i];
        }
        for (; j < arr2.length; j++) {
            result[++k] = arr2[j];
        }
        return result;
    }


    /**
     * read resource file from given url,
     * return all of its lines as an Iterator when resource is available
     * return empty when resource is unavailable
     *
     * @param url given url
     * @return Some iterator when resource available, Empty when resource is unavailable
     */
    private static Optional<Iterator<String>> from(String url) {

        return Optional.empty();
    }


    /**
     * read a text file, split each line into words, distinct words, and sort them according to word frequency, in a most frequent at top to least frequent at bottom order.
     *
     * @param url given url
     * @return the ranked list
     */
    public static Optional<Iterable<String>> rank(String url) {

        return Optional.empty();
    }


    /**
     * use merge sort to sort a given list
     * Do not use APIs which have already implemented the merge sort algorithm
     *
     * @param list given unsorted list
     * @param <T>  comparable element
     * @return sorted list
     */
    public static <T extends Comparable<? super T>> List<T> mergeSort(List<T> list) {

        return Collections.EMPTY_LIST;
    }
}
