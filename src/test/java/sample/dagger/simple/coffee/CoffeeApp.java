package sample.dagger.simple.coffee;

import dagger.Component;

import javax.inject.Singleton;

public class CoffeeApp {
  public static void main(String[] args) {
    DaggerCoffeeApp_Coffee.create().maker().brew();
  }

  @Singleton
  @Component(modules = {HeaterModule.class, PumpModule.class})
  public interface Coffee {
    CoffeeMaker maker();
  }
}