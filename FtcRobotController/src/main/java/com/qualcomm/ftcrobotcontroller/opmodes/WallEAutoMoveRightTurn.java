package com.qualcomm.ftcrobotcontroller.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

/*
 * An example linear op mode where the pushbot
 * will drive in a square pattern using sleep()
 * and a for loop.
 */
public class WallEAutoMoveRightTurn extends WallEAutoMoveLeftTurn {


    @Override
    double redblue() {
        return -1.0;
    }

}
