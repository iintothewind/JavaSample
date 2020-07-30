package war;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.junit.Test;
import sample.basic.Lst;

import java.util.Arrays;


@Getter
@Setter
@ToString
class Node {
  private int value;
  private Node left;
  private Node right;
}


public class Solution {
  public static void printNode(final Node node) {
    System.out.println(node.getValue());
    if (node.getLeft() != null) {
      printNode(node.getLeft());
    }
    if (node.getRight() != null) {
      printNode(node.getRight());
    }
  }

  public static void traverse(final Lst<Integer> lst, final Node node) {
    lst.cons(node.getValue());
    if (node.getLeft() != null) {
      traverse(lst, node.getLeft());
    }
    if (node.getRight() != null) {
      traverse(lst, node.getRight());
    }
  }

  public static int maxDistance(final int[] array) {
//    IntStream .range(0, array.length)
//      .boxed()
//      .collect(Collectors.toMap(i -> array[i], i -> array[i]))
//      .entrySet()
//      .stream()
//      .sorted(Comparator.<Map.Entry<Integer, Integer>>comparingInt(Map.Entry::getValue).thenComparing(Map.Entry::getKey))
//      .collect(Collectors.toMap(e->e.getValue(),IntStream.))
    return 0;

  }

  public int solution(int[] array) {
    array = Arrays.stream(array).distinct().sorted().toArray();
    int last = 0;
    for (int i = 1; i < array.length; i++) {
      if (array[i] > 0 && array[i] - array[i - 1] == 1) {
        last = array[i];
      }
    }
    return last + 1;
  }

  @Test
  public void testSolution() {
    System.out.println(solution(new int[]{1, 3, 6, 4, 1, 2}));
    System.out.println(solution(new int[]{1, 2, 3}));
    System.out.println(solution(new int[]{-1, -3}));
  }

  /**
   * 0
   * / \
   * 1    2
   * 3  4  5  6
   */
  @Test
  public void testInit() {
    final Node root = new Node();

    final Node leftChild = new Node();
    final Node leftChild1 = new Node();
    final Node leftChild2 = new Node();

    final Node rightChild = new Node();
    final Node rightChild1 = new Node();
    final Node rightChild2 = new Node();

    root.setValue(0);

    leftChild.setValue(1);
    rightChild.setValue(2);
    root.setLeft(leftChild);
    root.setRight(rightChild);

    leftChild1.setValue(3);
    leftChild2.setValue(4);
    leftChild.setLeft(leftChild1);
    leftChild.setRight(leftChild2);

    rightChild1.setValue(5);
    rightChild2.setValue(6);
    rightChild.setLeft(rightChild1);
    rightChild.setRight(rightChild2);

    traverse(Lst.empty(), root);

  }

}
