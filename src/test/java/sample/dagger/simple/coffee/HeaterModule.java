package sample.dagger.simple.coffee;

import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
class HeaterModule {
  @Provides
  @Singleton
  Heater provideHeater() {
    return new ElectricHeater();
  }
}