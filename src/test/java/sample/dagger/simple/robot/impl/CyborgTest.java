package sample.dagger.simple.robot.impl;

import org.junit.Test;
import sample.dagger.simple.robot.DaggerCyborg;

public class CyborgTest {

  @Test
  public void testMaker() {
    DaggerCyborg.create().getLegs().jump();
  }
}
