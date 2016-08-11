package sample.merge;

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
}
