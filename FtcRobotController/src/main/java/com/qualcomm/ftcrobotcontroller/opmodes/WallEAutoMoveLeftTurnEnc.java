package com.qualcomm.ftcrobotcontroller.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;

import java.util.Timer;

/*
 * An example linear op mode where the pushbot
 * will drive in a square pattern using sleep()
 * and a for loop.
 */
public class WallEAutoMoveLeftTurnEnc extends LinearOpMode {

    // Define our hardware -- motors
    DcMotor motorRight;
    DcMotor motorLeft;
    DcMotor motorArm;
    DcMotor motorAccum;

    // Constant for accumulator motor power
    final static double ACCUM_SPEED = 0.50;
    final static int ENC_TOL = 10;

    @Override
    public void runOpMode() throws InterruptedException {
        // Initialize all the hardware objects to real hardware instances
        motorRight = hardwareMap.dcMotor.get("m1");
        motorLeft = hardwareMap.dcMotor.get("m2");
        //motorLeft.setDirection(DcMotor.Direction.REVERSE);
        motorAccum = hardwareMap.dcMotor.get("m3");
        motorArm = hardwareMap.dcMotor.get("m4");

        // Reset encoders
        motorRight.setMode(DcMotorController.RunMode.RESET_ENCODERS);
        motorLeft.setMode(DcMotorController.RunMode.RESET_ENCODERS);

        // Wait for reset encoders
        waitForNextHardwareCycle();
        waitForNextHardwareCycle();
        waitForNextHardwareCycle();

        motorRight.setMode(DcMotorController.RunMode.RUN_TO_POSITION);
        motorLeft.setMode(DcMotorController.RunMode.RUN_TO_POSITION);

        waitForStart();

        // Wait for 5 second interval before moving
        // sleep(5000);

        // Set the accumulator to out
        // motorAccum.setPower(-ACCUM_SPEED);

        // Drive forward
        driveToPosn(-10000, 10000);

        // Drive forward
        //motorLeft.setPower(0.5);
        //motorRight.setPower(0.5);

       //sleep(2200);     // Drive for 2 seconds

        // Stop and wait for coast
        motorLeft.setPower(0.0);
        motorRight.setPower(0.0);
        sleep(500);

        // Turn left (redblue returns 1.0)
        //motorLeft.setPower(redblue() * -0.8);
        //motorRight.setPower(redblue() * 0.8);

        //sleep(1000);     // Turn for 1/2 second

        //motorLeft.setPower(0.0);
        //motorRight.setPower(0.0);

        //sleep(500);


        // Drive forward
        //motorLeft.setPower(0.5);
        //motorRight.setPower(0.5);

        //sleep(1600);

        // and we are done
        //motorLeft.setPower(0.0);
        //motorRight.setPower(0.0);
        //motorAccum.setPower(0.0);
    }


    double redblue() {
        return 1.0;
    }

    /**
     * Indicate whether the drive motors' encoders have reached a value.
     */
    public boolean haveEncodersReached(double leftCount, double rightCount) {

        if (hasLeftEncoderReached(leftCount) &&
                hasRightEncoderReached(rightCount)) {
            return true;
        }
        return false;
    }

    /**
     * Indicate whether the left drive motor's encoder has reached a value.
     */
    boolean hasLeftEncoderReached(double count) {

        if (Math.abs(motorLeft.getCurrentPosition() - count) < ENC_TOL) {
            return true;
        }
        return false;
    }


    /**
     * Indicate whether the right drive motor's encoder has reached a value.
     */
    boolean hasRightEncoderReached(double count) {

        if (Math.abs(motorRight.getCurrentPosition() - count ) < ENC_TOL) {
            return true;
        }
        return false;
    }

    /**
     *
     * Drive the specified RELATIVE distance left and right
     */
    boolean driveToPosn(double left, double right)  throws InterruptedException {
        double oldLeft = motorLeft.getCurrentPosition();
        double oldRight = motorRight.getCurrentPosition();

        // Calculate new absolute target position
        double newLeft = oldLeft - left; // Left is reversed
        double newRight = oldRight + right;

        // Set the target wheel position
        motorLeft.setTargetPosition((int) newLeft);
        motorRight.setTargetPosition((int) newRight);

        // Wait for hardware
        waitForNextHardwareCycle();
        waitForNextHardwareCycle();

        telemetry.addData("LeftTgt", "LeftTgt: " +newLeft);
        telemetry.addData("RtTgt", "RtTgt: " + newRight);


        // Set motor powers
        motorLeft.setPower(0.5);
        motorRight.setPower(0.5);

        while (!haveEncodersReached(newLeft, newRight)) {
            // Do nothing
            telemetry.addData("LeftCur", "LeftCur: " + motorLeft.getCurrentPosition() );
            telemetry.addData("RtCur", "RtCur: " + motorRight.getCurrentPosition() );

        }

        return haveEncodersReached(newLeft, newRight);
    }


}