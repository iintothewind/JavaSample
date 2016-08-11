package sample.dagger.simple.coffee;

import dagger.Component;


import javax.inject.Singleton;

public class CoffeeApp {
    @Singleton
    @Component(modules = {HeaterModule.class, PumpModule.class})
    public interface Coffee {
        CoffeeMaker maker();
    }

    public static void main(String[] args) {
        DaggerCoffeeApp_Coffee.create().maker().brew();
    }
}