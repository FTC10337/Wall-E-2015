package com.qualcomm.ftcrobotcontroller.opmodes;


import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.hardware.ModernRoboticsI2cGyro;
/*
 * autonumus code for driving to blue low zone
 * Dark matter
 * 10337
 * MJH
 */
public class WallEAutoMtnLeftGyro extends LinearOpMode {

    // Define our hardware -- motors
    DcMotor motorRight;
    DcMotor motorLeft;
    DcMotor motorArm;
    DcMotor motorAccum;
    Servo dumper;
    Servo rZip;
    Servo lZip;
    ModernRoboticsI2cGyro sensorGyro;

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
        ModernRoboticsI2cGyro gyroSensor = (ModernRoboticsI2cGyro) hardwareMap.gyroSensor.get("gyro");

        sensorGyro.calibrate();

        dumpPosition = DUMP_INIT;
        rZipPosition = RZIP_INIT;
        lZipPosition = LZIP_INIT;

        waitForStart();

        sensorGyro.calibrate();

        while(sensorGyro.isCalibrating()) { waitForNextHardwareCycle(); }

        dumper.setPosition(dumpPosition);
        rZip.setPosition(rZipPosition);
        lZip.setPosition(lZipPosition);

        // autonomous movements

        driveToPosn(4032, 4032, 0.75, 7.0);
        sleep(500);
        driveUsingGyro(-43);
        sleep(500);
        driveToPosn(5250, 5250, 0.75, 7.0);
        sleep(500);
        driveUsingGyro(-43);
        sleep(500);


        // and we are done
        motorLeft.setPower(0.0);
        motorRight.setPower(0.0);
        motorAccum.setPower(0.0);
        dumper.setPosition(0.49);
        rZip.setPosition(1.0);
        lZip.setPosition(0.0);
    }

        // left turn version
    double redblue() {
        return 1.0;
    }

    public void driveUsingGyro (int targetTurn) throws InterruptedException {

        motorRight.setMode(DcMotorController.RunMode.RUN_WITHOUT_ENCODERS);
        motorLeft.setMode(DcMotorController.RunMode.RUN_WITHOUT_ENCODERS);

        waitForNextHardwareCycle();

        int currentHeading = sensorGyro.getIntegratedZValue();
        int targetHeading = currentHeading + targetTurn;

        int errorDegrees = Math.abs(Math.abs(currentHeading) - Math.abs(targetHeading));

        // turn until within target heading degrees
        int leftStartPos = motorLeft.getCurrentPosition();
        int rightStartPos = motorRight.getCurrentPosition();

        while (errorDegrees >= 2) {

            double leftAdd = 0.0;
            double rightAdd = 0.0;

            int leftCurrentPos = motorLeft.getCurrentPosition();
            int rightCurrentPos = motorRight.getCurrentPosition();

            int errorRightMotor = Math.abs(rightStartPos - rightCurrentPos);
            int errorLeftMotor = Math.abs(leftStartPos - leftCurrentPos);

            // obtain current heading
            currentHeading = sensorGyro.getHeading();

            // calculate degrees from target heading (error degrees)
            errorDegrees = Math.abs(Math.abs(currentHeading) - Math.abs(targetHeading));

            // set motor output based on amount of error degrees
            double motorOutput = ((errorDegrees / 360.0) + 0.5);

            // set motor output if they are greater 1.0 or less than 0.5
            if (motorOutput > 1.0) motorOutput = 1.0;
            if (motorOutput < 0.8) motorOutput = 0.8;

            // turn right if target turn is positive
            if (errorLeftMotor > errorRightMotor) {
                leftAdd = -0.05;
                rightAdd = 0.05;
            } else {
                leftAdd = 0.05;
                rightAdd = -0.05;
            }

            double motorOutputLeft = (motorOutput + leftAdd);
            double motorOutputRight = (motorOutput + rightAdd);

            if (motorOutputRight > 1.0) motorOutputRight = 1.0;
            if (motorOutputLeft > 1.0) motorOutputLeft = 1.0;

            if (targetTurn > 0) {
                motorLeft.setPower(-motorOutputLeft);
                motorRight.setPower(-motorOutputRight);
            }
            // turn left if target turn is negative
            if (targetTurn < 0) {
                motorLeft.setPower(motorOutputLeft);
                motorRight.setPower(motorOutputRight);
            }

            //telemetry.addData("Total motor error", Math.abs(errorLeftMotor - errorRightMotor));
            //telemetry.addData("Motor right pwr: ", (motorOutput + rightAdd));
            //telemetry.addData("Motor left pwr: ", (motorOutput + leftAdd));
            telemetry.addData("Gyro: ", currentHeading);
            telemetry.addData("Motor Pwr Right", motorOutputRight);
            telemetry.addData("Motor Pwr Left: ", motorOutputLeft);
        }
        // turn achieved. Set motor power to zero.
        motorLeft.setPower(0.0);
        motorRight.setPower(0.0);
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

        motorRight.setMode(DcMotorController.RunMode.RUN_TO_POSITION);
        motorLeft.setMode(DcMotorController.RunMode.RUN_TO_POSITION);

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
