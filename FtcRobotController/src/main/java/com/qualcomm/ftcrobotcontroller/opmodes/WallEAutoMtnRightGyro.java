package com.qualcomm.ftcrobotcontroller.opmodes;

/*
 *autonamus code for right turn for red side of the mountain
 */
public class WallEAutoMtnRightGyro extends WallEAutoMtnLeft {

    // this code is a mirrior of left turn on the mountain
    // It simply overrides the one value that is used to add proper sign to values for turns
    // -1.0 means we are turning right
    @Override
    double redblue() {
        return -1.0;
    }

}
