package sample.dagger.simple.robot;

import dagger.Component;
import sample.dagger.simple.robot.impl.CyborgModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {CyborgModule.class})
public interface Cyborg {
  public abstract Legs getLegs();

  public abstract Sensors getSensors();

}
