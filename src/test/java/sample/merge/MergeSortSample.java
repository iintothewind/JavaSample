package sample.merge;

import io.vavr.control.Try;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


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
   * read lines from a text file, return all lines
   *
   * @param path, the path of input file
   * @return an iterable that contains all lines, return empty list if there is any IOException
   */
  public static Iterable<String> readLines(String path) {
    return Try.of(() ->
      Files
        .lines(Paths.get(path))
        .collect(Collectors.toList()))
      .getOrElse(new ArrayList<>());
  }

  /**
   * split each line into words, distinct words, and sort them according to word frequency,
   * return all words and their count numbers as an Iterable of string separated by a space character,
   * in a most frequent at top to least frequent at bottom order.
   *
   * @param lines given string lines
   * @return an Iterable that contains all distinct words and their count numbers
   * as an Iterable of string separated by a space character, ordered by count number, then word
   */
  public static Iterable<String> process(Iterable<String> lines) {
    return Optional
      .ofNullable(lines)
      .map(lns -> StreamSupport.stream(lns.spliterator(), false)
        .flatMap(str ->
          Arrays.stream(str.split("\\s+")))
        .collect(Collectors.toMap(
          Function.identity(),
          str -> 1,
          Integer::sum))
        .entrySet()
        .stream()
        .filter(e -> !"".equals(e.getKey()))
        .sorted(Comparator
          .<Map.Entry<String, Integer>, Integer>comparing(Map.Entry::getValue)
          .thenComparing(Map.Entry::getKey)
          .reversed())
        .map(entry -> String.format("%s %s", entry.getKey(), entry.getValue()))
        .collect(Collectors.toList())
      ).orElse(new ArrayList<>());
  }


  /**
   * use merge sort to sort a given list
   * Do not use APIs which have already implemented the merge sort algorithm
   *
   * @param list given unsorted list
   * @param <T>  comparable element
   * @return sorted list
   */
  public static <T extends Comparable<? super T>> Iterable<T> mergeSort(Iterable<T> list) {

    return null;
  }

  @Test
  public void testRank() {
    process(readLines("lines.txt")).forEach(System.out::println);
  }

  public static String normalizePath(String url) {
    return Try
      .of(() -> new File(url))
      .filter(File::exists)
      .mapTry(File::getCanonicalPath)
      .getOrElseThrow(() -> new IllegalArgumentException(String.format("file path: %s is not existing",
        Try.of(() -> new File(url)).mapTry(File::getCanonicalPath).getOrElse(url))));
  }

  @Test
  public void testNormalizePath() {
    final String path = normalizePath("./");
    System.out.println(path);

    final File f = new File("file:///Volumes/work/code/java/JavaSample");
    System.out.println(f.exists());
  }
}
