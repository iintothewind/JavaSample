package sample.dagger.simple.robot.impl;


import sample.dagger.simple.robot.Sensors;

public class CyborgSensors implements Sensors {
    @Override
    public void look() {
        System.out.println("look with cyborg eyes");
    }

    @Override
    public void hear() {
        System.out.println("hear with cyborg ears");
    }
}
