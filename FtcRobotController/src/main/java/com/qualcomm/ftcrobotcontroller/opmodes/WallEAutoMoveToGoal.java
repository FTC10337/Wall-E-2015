package com.qualcomm.ftcrobotcontroller.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;

/*
 * An example linear op mode where the pushbot
 * will drive in a square pattern using sleep()
 * and a for loop.
 */
public class WallEAutoMoveToGoal extends LinearOpMode {

    // Define our hardware -- motors
    DcMotor motorRight;
    DcMotor motorLeft;
    DcMotor motorArm;
    DcMotor motorAccum;

    // Constant for accumulator motor power
    final static double ACCUM_SPEED = 0.50;

    @Override
    public void runOpMode() throws InterruptedException {
        // Initialize all the hardware objects to real hardware instances
        motorRight = hardwareMap.dcMotor.get("m1");
        motorLeft = hardwareMap.dcMotor.get("m2");
        motorLeft.setDirection(DcMotor.Direction.REVERSE);
        motorAccum = hardwareMap.dcMotor.get("m3");
        motorArm = hardwareMap.dcMotor.get("m4");

        waitForStart();

        // Set the accumulator to out
        motorAccum.setPower(-ACCUM_SPEED);

        // Drive forward
        motorLeft.setPower(0.5);
        motorRight.setPower(0.5);

        wait(2000);     // Drive for 2 seconds

        // Stop and wait for coast
        motorLeft.setPower(0.0);
        motorRight.setPower(0.0);
        wait(500);

        // Turn left
        motorLeft.setPower(-0.5);
        motorRight.setPower(0.5);

        wait(500);     // Turn for 1/2 second

        motorLeft.setPower(0.0);
        motorRight.setPower(0.0);

        wait(500);


        // Drive forward
        motorLeft.setPower(0.5);
        motorRight.setPower(0.5);

        wait(2000);

        // and we are done
        motorLeft.setPower(0.0);
        motorRight.setPower(0.0);
        motorAccum.setPower(0.0);
    }


}
