package com.qualcomm.ftcrobotcontroller.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.util.ElapsedTime;

import java.util.Timer;

/*
 * WallEAutoMoveLeftTurn
 */
public class WallEAutoMoveLeftTurnEnc extends LinearOpMode {

    // Define our hardware -- motors
    DcMotor motorRight;
    DcMotor motorLeft;
    DcMotor motorArm;
    DcMotor motorAccum;


    // Constant for accumulator motor power
    final static double ACCUM_SPEED = 0.0;
    final static int ENC_TOL = 10;

    @Override
    public void runOpMode() throws InterruptedException {
        // Initialize all the hardware objects to real hardware instances
        motorRight = hardwareMap.dcMotor.get("m1");
        motorLeft = hardwareMap.dcMotor.get("m2");
        //motorLeft.setDirection(DcMotor.Direction.REVERSE);
        motorAccum = hardwareMap.dcMotor.get("m3");
        motorArm = hardwareMap.dcMotor.get("m4");

        // Wait for reset encoders
        waitForNextHardwareCycle();
        waitForNextHardwareCycle();
        waitForNextHardwareCycle();

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
        sleep(5000);

        // Set the accumulator to out
        motorAccum.setPower(-ACCUM_SPEED);

        // Drive forward
        driveToPosn(4032, 4032, 0.75, 5.0);
        sleep(500);
        driveToPosn(redblue() * -840, redblue() * 840, 1.0, 5.0);
        sleep(500);
        driveToPosn(4450, 4450, 0.75, 5.0);
        sleep(500);
        driveToPosn(redblue() * 1250, redblue() * -1250, 1.0, 5.0);
        sleep(500);
        driveToPosn(redblue() * 1250, redblue() * -1250, 1.0, 5.0);
        sleep(500);
        driveToPosn(-3400, -3400, 0.75, 5.0);
        sleep(500);
        driveToPosn(-200, -200, 0.3, 5.0);
        sleep(500);



        // and we are done
        motorLeft.setPower(0.0);
        motorRight.setPower(0.0);
        motorAccum.setPower(0.0);
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
    boolean driveToPosn(double left, double right, double power, double timeout)  throws InterruptedException {
        double oldLeft = motorLeft.getCurrentPosition();
        double oldRight = motorRight.getCurrentPosition();

        // Timer to make sure we dont get stuck
        ElapsedTime timer = new ElapsedTime();

        // Calculate new absolute target position
        double newLeft = oldLeft - left; // Left is reversed
        double newRight = oldRight + right;

        // Set the target wheel position
        motorLeft.setTargetPosition((int) newLeft);
        motorRight.setTargetPosition((int) newRight);

        // Wait for hardware
        waitForNextHardwareCycle();
        waitForNextHardwareCycle();

        telemetry.addData("LeftTgt", "LeftTgt: " + newLeft);
        telemetry.addData("RtTgt", "RtTgt: " + newRight);

        // Start timer now
        timer.reset();


        // Set motor powers
        motorLeft.setPower(power);
        motorRight.setPower(power);

        while (!haveEncodersReached(newLeft, newRight) && timer.time() <= timeout) {
            // Do nothing
            telemetry.addData("LeftCur", "LeftCur: " + motorLeft.getCurrentPosition() );
            telemetry.addData("RtCur", "RtCur: " + motorRight.getCurrentPosition() );

        }

        // Stop driving
        motorLeft.setPower(0.0);
        motorRight.setPower(0.0);

        return haveEncodersReached(newLeft, newRight);
    }


}
