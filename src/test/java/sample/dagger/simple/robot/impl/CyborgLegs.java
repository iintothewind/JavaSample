package sample.dagger.simple.robot.impl;

import sample.dagger.simple.robot.Legs;

public class CyborgLegs implements Legs {
  @Override
  public void walk() {
    System.out.println("walk with cyborg legs");
  }

  @Override
  public void run() {
    System.out.println("run with cyborg legs");
  }

  @Override
  public void jump() {
    System.out.println("jump with cyborg legs");
  }
}
