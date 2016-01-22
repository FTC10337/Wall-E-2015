package com.qualcomm.ftcrobotcontroller.opmodes;


import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

/*
 * autonumus code for driving to blue low zone
 * Dark matter
 * 10337
 * MJH
 */
public class WallEAutoMtnLeft extends LinearOpMode {

    // Define our hardware -- motors
    DcMotor motorRight;
    DcMotor motorLeft;
    DcMotor motorArm;
    DcMotor motorAccum;
    Servo dumper;
    Servo rZip;
    Servo lZip;

    // Constant for accumulator motor power
    final static double ACCUM_SPEED = 1.0;
    final static int ENC_TOL = 10;
    final static double DUMP_INIT = 0.49;
    final static double RZIP_INIT = 1.0;
    final static double LZIP_INIT = 0.0;

    double rZipPosition;
    double lZipPosition;
    double dumpPosition;



    @Override
    public void runOpMode() throws InterruptedException {
        // Initialize all the hardware objects to real hardware instances
        motorRight = hardwareMap.dcMotor.get("m1");
        motorLeft = hardwareMap.dcMotor.get("m2");

        motorAccum = hardwareMap.dcMotor.get("m3");
        motorArm = hardwareMap.dcMotor.get("m4");
        dumper = hardwareMap.servo.get("s2");
        rZip = hardwareMap.servo.get("s3");
        lZip = hardwareMap.servo.get("s4");

        // Wait for reset encoders
        waitForNextHardwareCycle();
        waitForNextHardwareCycle();
        waitForNextHardwareCycle();

        // Reset encoders
        motorRight.setMode(DcMotorController.RunMode.RESET_ENCODERS);
        motorLeft.setMode(DcMotorController.RunMode.RESET_ENCODERS);

        dumpPosition = DUMP_INIT;
        rZipPosition = RZIP_INIT;
        lZipPosition = LZIP_INIT;


        // Wait for reset encoders
        waitForNextHardwareCycle();
        waitForNextHardwareCycle();
        waitForNextHardwareCycle();

        motorRight.setMode(DcMotorController.RunMode.RUN_TO_POSITION);

        motorLeft.setMode(DcMotorController.RunMode.RUN_TO_POSITION);

        waitForStart();


        dumper.setPosition(dumpPosition);
        rZip.setPosition(rZipPosition);
        lZip.setPosition(lZipPosition);

        int count = 0;

        for (count = 0; count < 50; count++){

            // Drive forward 20 inches
            if (count == 25) {
                dumper.setPosition(0.49);
                motorAccum.setPower(-ACCUM_SPEED);
                driveToPosn(2100, 2100, 0.5, 5.0);
                sleep (500);
            }
            // Turn left 45 deg (redblue returns 1.0 for left and -1 for right)
            if (count == 28) {
                driveToPosn(redblue() * -833, redblue() * 833, 1.0, 5.0);
                sleep(500);
            }
            // Drive forward 8 inches
            if (count == 31) {
                driveToPosn(890, 890, 0.5, 5.0);
                sleep(500);
            }
            // turn right 90 deg
            if (count == 34) {
                driveToPosn(redblue() * 1666, redblue() * -1666, 1.0, 5.0);
                sleep (500);
            }
            // back onto mountain
            if (count == 37) {
                motorAccum.setPower(0.0);
                driveToPosn(-1600, -1600, 0.5, 5.0);
                sleep(500);
            }
            // back onto mountain slowly
            if (count == 40) {
                driveToPosn(-400, -400, 0.3, 5.0);
                sleep (500);
            }

            //wag tail
            if (count % 2 == 0) {
               rZip.setPosition(0.80);
                lZip.setPosition(0.20);
                if (count < 25 ) dumper.setPosition(0.4);
                if (count > 40 ) dumper.setPosition(0.4);
            }

            //wag tail
            if (count % 2 != 0) {
                rZip.setPosition(1.0);
                lZip.setPosition(0.0);
                if (count < 25 ) dumper.setPosition(0.60);
                if (count > 40 ) dumper.setPosition(0.60);
            }

            sleep (200);

        }

        // and we are done
        motorLeft.setPower(0.0);
        motorRight.setPower(0.0);
        motorAccum.setPower(0.0);
        dumper.setPosition(0.49);
        rZip.setPosition(1.0);
        lZip.setPosition(0.0);
    }

        // left turn vresion
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
