package war;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GetMiddle {
  public static String getMiddle(String word) {
    if (word != null) {
      if (word.length() > 2) {
        if (word.length() % 2 == 1) {
          return word.substring(word.length() / 2, word.length() / 2 + 1);
        } else {
          return word.substring(word.length() / 2 - 1, word.length() / 2 + 1);
        }
      }
    }
    return word;
  }

  @Test
  public void evenTests() {
    assertEquals("es", getMiddle("test"));
    assertEquals("dd", getMiddle("middle"));
  }

  @Test
  public void oddTests() {
    assertEquals("t", getMiddle("testing"));
    assertEquals("A", getMiddle("A"));
  }
}
