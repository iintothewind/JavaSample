package sample.dagger.simple.robot.impl;

import dagger.Module;
import dagger.Provides;
import sample.dagger.simple.robot.Legs;
import sample.dagger.simple.robot.Sensors;

import javax.inject.Singleton;

@Module
public class CyborgModule {
    @Provides
    @Singleton
    public Legs provideCyborgLegs() {
        return new CyborgLegs();
    }

    @Provides
    @Singleton
    public Sensors provideCyborgSensors() {
        return new CyborgSensors();
    }
}
