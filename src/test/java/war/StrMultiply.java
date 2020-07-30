package war;

import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

public class StrMultiply {

  public String multiply(String num1, String num2) {
    final Integer[] num1Array = Utls.splitIntChars(num1).toArray(new Integer[0]);
    final Integer[] num2Array = Utls.splitIntChars(num2).toArray(new Integer[0]);
    final int[][] matrix = new int[num2Array.length][num1Array.length + num2Array.length];
    final int[] result = new int[num1Array.length + num2Array.length];
    int numberOfZeros = 0;
    for (int y = 0; y < num2Array.length; y++) {
      for (int i = 0; i < numberOfZeros; i++) {
        matrix[y][(num1Array.length + num2Array.length - 1) - i] = 0;
      }
      for (int x = 0; x < num1Array.length; x++) {
        int a = num2Array[num2Array.length - 1 - y];
        int b = num1Array[num1Array.length - 1 - x];
        matrix[y][(num1Array.length + num2Array.length - 1) - (x + numberOfZeros)] = a * b;
      }
      numberOfZeros++;
    }
    int carry = 0;
    for (int y = matrix[0].length - 1; y >= 0; y--) {
      int tmp = carry;
      for (int x = matrix.length - 1; x >= 0; x--) {
        tmp = tmp + matrix[x][y];
      }
      carry = tmp / 10;
      result[y] = tmp % 10;
    }
    int nonZeroIndex = -1;
    for (int i = 0; i < result.length; i++) {
      if (result[i] != 0) {
        nonZeroIndex = i;
        break;
      }
    }
    if (nonZeroIndex == -1) {
      return "0";
    } else {
      return Arrays.stream(Arrays.copyOfRange(result, nonZeroIndex, result.length)).mapToObj(String::valueOf).collect(Collectors.joining());
    }
  }

  @Test
  public void testMultiply() {
//    System.out.println(multiply("98", "21"));
    System.out.println(multiply("99", "9"));
  }
}
