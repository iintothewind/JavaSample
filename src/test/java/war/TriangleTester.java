package war;

import static java.lang.Math.abs;

public class TriangleTester {
  public static boolean isTriangle(int a, int b, int c) {
    return (a + b > c || a + c > b || b + c > a) && (abs(a - b) < c && abs(b - c) < a && abs(a - c) < b);
  }
}
