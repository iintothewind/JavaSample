package war;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LeetCode {

  /*
   * [3] Longest Substring Without Repeating Characters
   *
   * https://leetcode.com/problems/longest-substring-without-repeating-characters/description/
   *
   * algorithms
   * Medium (24.67%)
   * Total Accepted:    462.9K
   * Total Submissions: 1.9M
   * Testcase Example:  '"abcabcbb"'
   *
   * Given a string, find the length of the longest substring without repeating
   * characters.
   *
   * Examples:
   *
   * Given "abcabcbb", the answer is "abc", which the length is 3.
   *
   * Given "bbbbb", the answer is "b", with the length of 1.
   *
   * Given "pwwkew", the answer is "wke", with the length of 3. Note that the
   * answer must be a substring, "pwke" is a subsequence and not a substring.
   */
  public int lengthOfLongestSubstring(String s) {
    List<Character> lss = new ArrayList<>();
    List<Character> nonerepeatChars = new ArrayList<>();
    for (Character c : s.chars().mapToObj(c -> (char) c).collect(Collectors.toList())) {
      if (nonerepeatChars.contains(c)) {
        nonerepeatChars = new ArrayList<>(nonerepeatChars.subList(nonerepeatChars.indexOf(c) + 1, nonerepeatChars.size()));
      }
      nonerepeatChars.add(c);
      if (nonerepeatChars.size() > lss.size()) {
        lss = nonerepeatChars;
      }
    }
    return lss.size();
  }


  @Test
  public void testLengthOfLongestSubstring() {
    System.out.println(lengthOfLongestSubstring("abcadefg"));
    System.out.println(lengthOfLongestSubstring("bbbbbbbb"));
    System.out.println(lengthOfLongestSubstring("pwwkew"));
    System.out.println(lengthOfLongestSubstring("dvdf"));
  }

  public double findMedianSortedArrays(int[] nums1, int[] nums2) {
    if (nums1 == null || nums1.length == 0) {
      if (nums2 == null || nums2.length == 0) {
        return 0;
      } else if (nums2.length == 1) {
        return nums2[0];
      } else {
        if (nums2.length % 2 == 1) {
          return nums2[(nums2.length - 1) / 2];
        } else {
          return (nums2[(nums2.length - 1) / 2] + nums2[(nums2.length - 1) / 2 + 1]) / 2.0D;
        }
      }
    } else if (nums2 == null || nums2.length == 0) {
      if (nums1.length == 1) {
        return nums1[0];
      } else {
        if (nums1.length % 2 == 1) {
          return nums1[(nums1.length - 1) / 2];
        } else {
          return (nums1[(nums1.length - 1) / 2] + nums1[(nums1.length - 1) / 2 + 1]) / 2.0D;
        }
      }
    } else {
      return (Math.min(nums1[0], nums2[0]) + Math.max(nums1[nums1.length - 1], nums2[nums2.length - 1])) / 2.0D;
    }
  }

  @Test
  public void testFindMedian() {
    System.out.println(findMedianSortedArrays(new int[]{}, new int[]{1}));
  }

  public int bs(int[] array, int target) {
    Arrays.sort(array);
    int pos = -1;
    int low = 0;
    int high = array.length;

    while (low != high) {
      pos = (low + high) / 2;
      if (target < array[pos]) {
        high = (low + high) / 2;
      } else if (target > array[pos]) {
        low = (low + high) / 2 + 1;
      } else {
        return pos;
      }
    }
    return -1;
  }

  @Test
  public void testBs() {
    System.out.println(bs(new int[0], 1));
    System.out.println(bs(new int[]{1}, 1));
    System.out.println(bs(new int[]{1, 2}, 2));
    System.out.println(bs(new int[]{1, 2, 3}, 3));
    System.out.println(bs(new int[]{1, 2, 3}, 4));
    System.out.println(bs(new int[]{1, 2, 3}, -1));
  }


  public int bsr(int[] array, int target, int low, int high) {
    if (array == null || array.length == 0) return -1;
    if (low != high) {
      int pos = (low + high) / 2;
      if (target < array[pos]) {
        bsr(array, target, low, (low + high) / 2);
      } else if (target > array[pos]) {
        bsr(array, target, (low + high) / 2 + 1, high);
      } else {
        return pos;
      }
    }
    return -1;
  }

  @Test
  public void testBsr() {
//    System.out.println(bsr(new int[0], 1, 0, 0));
//    System.out.println(bsr(new int[]{1}, 1, 0, 1));
//    System.out.println(bsr(new int[]{1, 2}, 2, 0, 2));
    System.out.println(bsr(new int[]{1, 2, 3}, 3, 0, 3));
//    System.out.println(bsr(new int[]{1, 2, 3}, 4, 0, 3));
//    System.out.println(bsr(new int[]{1, 2, 3}, -1, 0, 3));
  }

}
