package sample.dagger.simple.robot.impl;


import dagger.Module;
import dagger.Provides;
import sample.dagger.simple.robot.Legs;
import sample.dagger.simple.robot.Sensors;

import javax.inject.Singleton;

@Module
public class AndroidModule {
  @Provides
  @Singleton
  public Legs provideCyborgLegs() {
    return new Legs() {
      @Override
      public void walk() {
        System.out.println("walk with android legs");
      }

      @Override
      public void run() {
        System.out.println("run with android legs");
      }

      @Override
      public void jump() {
        System.out.println("jump with android legs");
      }
    };
  }

  @Provides
  @Singleton
  public Sensors provideCyborgSensors() {
    return new Sensors() {
      @Override
      public void look() {
        System.out.println("look with android Sensors");
      }

      @Override
      public void hear() {
        System.out.println("hear with android Sensors");
      }
    };
  }
}
