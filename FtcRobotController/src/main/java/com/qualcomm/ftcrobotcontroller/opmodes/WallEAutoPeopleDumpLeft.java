package com.qualcomm.ftcrobotcontroller.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

/*
 * WallEAutoMoveLeftTurn
 */
public class WallEAutoPeopleDumpLeft extends LinearOpMode {

    // Define our hardware -- motors
    DcMotor motorRight;
    DcMotor motorLeft;
    DcMotor motorArm;
    DcMotor motorAccum;


    // Servos
    Servo rZip;
    Servo lZip;
    Servo dumper;

    // Constant for accumulator motor power
    final static double ACCUM_SPEED = 1.0;
    final static int ENC_TOL = 10;

    // Constants for Servo min and max postions
    final static double DUMP_MIN = 0.33;
    final static double DUMP_MAX = 0.65;
    final static double RZIP_MIN = 0.0;
    final static double RZIP_MAX = 1.00;
    final static double LZIP_MIN = 0.0;
    final static double LZIP_MAX = 1.00;


    // Constants for servo starting positions
    final static double RZIP_INIT = 1.0;
    final static double LZIP_INIT = 0.0;
    final static double DUMP_INIT = 0.49;

    @Override
    public void runOpMode() throws InterruptedException {
        // Initialize all the hardware objects to real hardware instances
        motorRight = hardwareMap.dcMotor.get("m1");
        motorLeft = hardwareMap.dcMotor.get("m2");
        //motorLeft.setDirection(DcMotor.Direction.REVERSE);
        motorAccum = hardwareMap.dcMotor.get("m3");
        motorArm = hardwareMap.dcMotor.get("m4");


        rZip = hardwareMap.servo.get("s3");
        lZip = hardwareMap.servo.get("s4");
        dumper = hardwareMap.servo.get("s2");


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
        //sleep(5000);

        rZip.setPosition(RZIP_INIT);
        lZip.setPosition(LZIP_INIT);
        dumper.setPosition(0.49);
        sleep(100);

        // Set the accumulator to out
        motorAccum.setPower(-ACCUM_SPEED);

        // Drive forward
        driveToPosn(3500, 3500, 0.75, 5.0);
        sleep(250);
        driveToPosn(redblue() * -840, redblue() * 840, 1.0, 5.0);
        sleep(250);
        driveToPosn(7350, 7350, 0.75, 5.0);
        sleep(250);
        driveToPosn(-500, -500, 0.75, 5.0);
        sleep(250);

        // Turn back to original angle
        driveToPosn(redblue() * 840, redblue() * -840, 1.0, 5.0);

        // Set the accumulator to out
        motorAccum.setPower(0.0);

        driveToPosn(-1025, -1025, 0.75, 5.0);

        driveToPosn(redblue() * 410, redblue() * -410, 1.0, 5.0);
        sleep(250);
        driveToPosn(redblue() * 1250, redblue() * -1250, 1.0, 5.0);
        sleep(250);
        rZip.setPosition(0.5);
        lZip.setPosition(0.5);
        driveToPosn(-1500, -1500, 0.5, 5.0);
        sleep(250);
        driveToPosn(-700, -700, 0.3, 5.0);
        sleep(250);
        // Bump it off the wall
        driveToPosn(100, 100, 0.3, 1.0);
        sleep(250);

        if (dump()) {
            // This version of code dumps
            motorArm.setPower(-0.6);
            sleep(3000);
        }

        // and we are done
        motorArm.setPower(0.0);
        motorLeft.setPower(0.0);
        motorRight.setPower(0.0);
        motorAccum.setPower(0.0);
    }


    double redblue() {
        return 1.0;
    }

    boolean dump() { return true;}


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
