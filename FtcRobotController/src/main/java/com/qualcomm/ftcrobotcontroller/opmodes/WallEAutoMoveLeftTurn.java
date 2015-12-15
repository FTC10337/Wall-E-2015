package com.qualcomm.ftcrobotcontroller.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;

/*
 * An example linear op mode where the pushbot
 * will drive in a square pattern using sleep()
 * and a for loop.
 */
public class WallEAutoMoveLeftTurn extends LinearOpMode {

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

        // Wait for 10 second interval before moving
        sleep(10000);

        // Set the accumulator to out
        motorAccum.setPower(-ACCUM_SPEED);

        // Drive forward
        motorLeft.setPower(0.5);
        motorRight.setPower(0.5);

        sleep(2200);     // Drive for 2 seconds

        // Stop and wait for coast
        motorLeft.setPower(0.0);
        motorRight.setPower(0.0);
        sleep(500);

        // Turn left (redblue returns 1.0)
        motorLeft.setPower(redblue() * -0.8);
        motorRight.setPower(redblue() * 0.8);

        sleep(1000);     // Turn for 1/2 second

        motorLeft.setPower(0.0);
        motorRight.setPower(0.0);

        sleep(500);


        // Drive forward
        motorLeft.setPower(0.5);
        motorRight.setPower(0.5);

        sleep(1600);

        // and we are done
        motorLeft.setPower(0.0);
        motorRight.setPower(0.0);
        motorAccum.setPower(0.0);
    }


    double redblue() {
        return 1.0;
    }

}
