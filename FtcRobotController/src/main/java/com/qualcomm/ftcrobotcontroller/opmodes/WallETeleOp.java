/* Copyright (c) 2014 Qualcomm Technologies Inc

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Qualcomm Technologies Inc nor the names of its contributors
may be used to endorse or promote products derived from this software without
specific prior written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */

package com.qualcomm.ftcrobotcontroller.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.hardware.TouchSensor;
import com.qualcomm.robotcore.hardware.OpticalDistanceSensor;
import com.qualcomm.robotcore.hardware.ColorSensor;

/**
 * TeleOp Mode
 * <p>
 *     Dark Matter
 *     10337
 *     MJH
 * Enables control of the robot via the gamepad
 */
public class WallETeleOp extends OpMode {


	// Constants for Servo min and max postions
	final static double ARM_MIN_RANGE  = 0.0;
	final static double ARM_MAX_RANGE  = 1.0;
	final static double DUMP_MIN = 0.33;
	final static double DUMP_MAX = 0.65;
	final static double RZIP_MIN = 0.0;
	final static double RZIP_MAX = 1.00;
	final static double LZIP_MIN = 0.0;
	final static double LZIP_MAX = 1.00;


	// Constants for servo starting positions
	final static double ARM_INIT = 0.0;
	final static double RZIP_INIT = 1.0;
	final static double LZIP_INIT = 0.0;
	final static double DUMP_INIT = 0.49;

	// Constant for drive train hill holding
	final static float HILL_HOLD_POWER = -0.25f;

	//Constants for accumulator shake button presses
	int waitSHAKE = 0;
	int countSHAKE = -15;

	//timer for hillbreak

	int timer = 0;

	//timer for accumulator stop

	int accumTimer = 0;

	// Constant for accumulator motor power
	double ACCUM_SPEED = 0.0;

	// position of the arm servo.
	double armPosition;
	double rZipPosition;
	double lZipPosition;
	double dumpPosition;

	// Constant amount to change the servo position.
	final static double SOL_DELTA = 0.005;
	final static double ZIP_DELTA = 0.010;
	final static double DUMP_DELTA = 0.02;
	final static double DUMP_DELTA_SHAKE = 0.05;

	// Hill holding brake set
	boolean hillBrake = false;

	// Which direction are we driving?
	boolean driveFwd = true;

	//gamepad buttons are not pressed
	boolean aIsPressed = false;
	boolean bIsPressed = false;
	boolean b2IsPressed = false;
	boolean yIsPressed = false;
	boolean rightBumperIsPressed = false;
	boolean reverseDirection = false;
	boolean accumIsOn = false;
	boolean accumTimerOn = false;


	// Define our hardware -- motors
	DcMotor motorRight;
	DcMotor motorLeft;
	DcMotor motorArm;
	DcMotor motorAccum;

	// Define our hardware -- servos
	Servo oldArm;	// This arm is not there anymore but we left code intact
	Servo dumper;
	Servo rZip;
	Servo lZip;

	// Define our hardware -- limit switch for main arm
	TouchSensor armLimit;

	// Define our hardware -- optical distance sensor
	OpticalDistanceSensor sensorLight;
	ColorSensor sensorColor;

	/**
	 * Constructor
	 */
	public WallETeleOp() {

	}

	/*
	 * Code to run when the op mode is initialized goes here
	 * 
	 * @see com.qualcomm.robotcore.eventloop.opmode.OpMode#init()
	 */
	@Override
	public void init() {


		/*
		 * Use the hardwareMap to get the dc motors and servos by name. Note
		 * that the names of the devices must match the names used when you
		 * configured your robot and created the configuration file.
		 */
		

		// Initialize all the hardware objects to real hardware instances
		motorRight = hardwareMap.dcMotor.get("m1");
		motorLeft = hardwareMap.dcMotor.get("m2");
		motorLeft.setDirection(DcMotor.Direction.REVERSE);
		motorAccum = hardwareMap.dcMotor.get("m3");
		motorArm = hardwareMap.dcMotor.get("m4");

		oldArm = hardwareMap.servo.get("s1");
		dumper = hardwareMap.servo.get("s2");
		rZip = hardwareMap.servo.get("s3");
		lZip = hardwareMap.servo.get("s4");

		armLimit = hardwareMap.touchSensor.get("t1");

		sensorLight = hardwareMap.opticalDistanceSensor.get("ods");
		sensorColor = hardwareMap.colorSensor.get("cs");

		// turn on optical distance sensor LED light
		sensorLight.enableLed(true);
		sensorColor.enableLed(true);

		// assign the starting position of the servos
		armPosition = ARM_INIT;
		dumpPosition = DUMP_INIT;
		rZipPosition = RZIP_INIT;
		lZipPosition = LZIP_INIT;

		// Make sure we start off driving "forward" at each match
		driveFwd = true;
	}

	/*
	 * This method will be called repeatedly in a loop
	 * 
	 * @see com.qualcomm.robotcore.eventloop.opmode.OpMode#run()
	 */
	@Override
	public void loop() {



		// Drivetrain code beginning

		// Left stick on pad1 is the throttle
		// Right stick on pad1 is the left/right turning
		float throttle = -gamepad1.left_stick_y;
		float direction = gamepad1.right_stick_x;
		float armmovement = gamepad2.right_stick_x;

		// Read the switches to see if we are reversing the drive direction
		if ((gamepad1.right_bumper) && (!rightBumperIsPressed) && (!reverseDirection)){
			driveFwd = true;
			rightBumperIsPressed = true;
			reverseDirection = true;

		}else if ((gamepad1.right_bumper) && (!rightBumperIsPressed) && (reverseDirection)){
			driveFwd = false;
			rightBumperIsPressed = true;
			reverseDirection = false;
		}

		if (!gamepad1.right_bumper) rightBumperIsPressed = false;

		// If we are driving in reverse we simply flip the throttle value
		if (!driveFwd){
			throttle = -1.0f * throttle;
		}

		// Convert throttle and direction into left/right motor values
		float right = throttle - direction;
		float left = throttle + direction;
		float arm = armmovement;

		// clip the right/left values so that the values never exceed +/- 1
		right = Range.clip(right, -1, 1);
		left = Range.clip(left, -1, 1);

		// scale the joystick value to make it easier to control
		// the robot more precisely at slower speeds.
		//right = (float)scaleInput(right);
		//left =  (float)scaleInput(left);
		right = (float)smoothPowerCurve(deadzone(right,0.10));
		left = (float)smoothPowerCurve(deadzone(left,0.10));

		// Check if hill hold brake set
		if (gamepad1.x) {
			// Hill holder is pushed -- so we set flag to hillBrake mode
			hillBrake = true;
			timer = 0;
		}

		if ((hillBrake && Math.abs(right) < 0.05 && Math.abs(left) < 0.05)){
			// Hill brake is set and no joystick input so we hold hill
			right = HILL_HOLD_POWER;
			left = HILL_HOLD_POWER;
			timer++;
		}
		else {
			// Turn off the brake -- either its already off or joystick has throttle
			hillBrake = false;
			timer = 0;
		}


       // stop hillbrake after 2 minutes of no input from driver controllers
        if (gamepad1.atRest() && gamepad2.atRest() && timer >= 12000)
        {
            motorRight.setPower(0.0);
            motorLeft.setPower(0.0);
            hillBrake = false;
            timer = 0;
        }

		// Finally we set the drive motors to appropriate values
		motorRight.setPower(right);
		motorLeft.setPower(left);

		// End of drivetrain <code></code>




		// Servo code for all of the servos on robot

		// "Old" arm position servo -- dumper for climbers into shelter
		//if (gamepad2.right_bumper) {
			// if the A button is pushed on gamepad1, increment the position of
			// the arm servo.
		//	armPosition += SOL_DELTA;
		//}
		//if (gamepad2.left_bumper) {
			// if the Y button is pushed on gamepad1, decrease the position of
			// the arm servo.
		//	armPosition -= SOL_DELTA;
		//}

		// Stick inputs for zipline tools
		if (gamepad1.right_trigger > 0.2) {
			// if the A button is pushed on gamepad1, increment the position of
			// the arm servo.
			rZipPosition -= ZIP_DELTA;
		}
		if (gamepad1.dpad_down ) {
			// if the Y button is pushed on gamepad1, decrease the position of
			// the arm servo.
			rZipPosition += ZIP_DELTA;
		}
		if (gamepad1.left_trigger > 0.2) {
			// if the A button is pushed on gamepad1, increment the position of
			// the arm servo.
			lZipPosition += ZIP_DELTA;
		}
		if (gamepad1.dpad_down ) {
			// if the Y button is pushed on gamepad1, decrease the position of
			// the arm servo.
			lZipPosition -= ZIP_DELTA;
		}

		// Main arm dumper servo position
		if (gamepad2.dpad_left) {
			// if the A button is pushed on gamepad1, increment the position of
			// the arm servo.
			dumpPosition += DUMP_DELTA;
		}
		if (gamepad2.dpad_right) {
			// if the Y button is pushed on gamepad1, decrease the position of
			// the arm servo.
			dumpPosition -= DUMP_DELTA;
		}
		if (gamepad2.dpad_down) {
			// if the Y button is pushed on gamepad1, decrease the position of
			// the arm servo.
			dumpPosition = 0.49;
		}

		if (gamepad2.left_bumper) {
			// if the left bumper is pushed on gamepad2, begin shaking the slide back and forth.
			if (countSHAKE == -15) dumpPosition = 0.65;
			if (countSHAKE == 0) dumpPosition = 0.60;
			countSHAKE += 1;
			if (countSHAKE == 15) countSHAKE = -15;
		}
		if (gamepad2.right_bumper) {
			// if the left bumper is pushed on gamepad2, begin shaking the slide back and forth.
			if (countSHAKE == -15) dumpPosition = 0.33;
			if (countSHAKE == 0) dumpPosition = 0.38;
			countSHAKE += 1;
			if (countSHAKE == 15) countSHAKE = -15;
		}


		// clip the position values so that they never exceed their allowed range.
		armPosition = Range.clip(armPosition, ARM_MIN_RANGE, ARM_MAX_RANGE);
		dumpPosition = Range.clip(dumpPosition, DUMP_MIN, DUMP_MAX);
		rZipPosition = Range.clip(rZipPosition, RZIP_MIN, RZIP_MAX);
		lZipPosition = Range.clip(lZipPosition, LZIP_MIN, LZIP_MAX);


		// write position values to the servos
		oldArm.setPosition(armPosition);
		dumper.setPosition(dumpPosition);
		rZip.setPosition(rZipPosition);
		lZip.setPosition(lZipPosition);



		// Code for moving the main arm

		// Read joystick value and range limit it
		float armStick = gamepad2.left_stick_y;
		armStick = Range.clip(armStick, -1, 1);

		// scale the joystick value to make it easier to control
		// the robot more precisely at slower speeds.
		armStick = (float)smoothPowerCurve(deadzone(armStick,0.10));

		// Cheak if limit switch is pressed
		if (armLimit.isPressed()) {
			// if touch sensor is pushed the motor will stop going in that direction
			armStick =  Range.clip(armStick, -1, 0);
		}

		// And power the motor
		motorArm.setPower(armStick);

		// End of main arm


		// Code for the accumulator motor

		if ((gamepad1.a) && (!aIsPressed) && (!accumIsOn))
		{
			ACCUM_SPEED = 1.0;
			aIsPressed = true;
			accumIsOn = true;
		}
		else if ((gamepad1.a) && (!aIsPressed) && (accumIsOn))
		{
			ACCUM_SPEED = 0.30;
			accumTimerOn = true;
			aIsPressed = true;
			accumIsOn = false;
		}

		if ((gamepad1.b) && (!bIsPressed) && (accumIsOn))
		{
			ACCUM_SPEED = 0.30;
			accumTimerOn = true;
			bIsPressed = true;
			accumIsOn = false;
		}

		if ((gamepad2.b) && (!b2IsPressed) && (!accumIsOn))
		{
			ACCUM_SPEED = 0.5;
			b2IsPressed = true;
			accumIsOn = true;
		}
		else if ((gamepad2.b) && (!b2IsPressed) && (accumIsOn) )
		{
			ACCUM_SPEED = 0.30;
			accumTimerOn = true;
			b2IsPressed = true;
			accumIsOn = false;
		}

		if ((gamepad1.y) && (!yIsPressed) && (!accumIsOn))
		{
			ACCUM_SPEED = -1.0;
			yIsPressed = true;
			accumIsOn = true;
		}
		else if ((gamepad1.y) && (!yIsPressed) && (accumIsOn) )
		{
			ACCUM_SPEED = -0.30;
			accumTimerOn = true;
			yIsPressed = true;
			accumIsOn = false;
		}

		if (!gamepad1.a) aIsPressed = false;
		if (!gamepad1.b) bIsPressed = false;
		if (!gamepad2.b) b2IsPressed = false;
		if (!gamepad1.y) yIsPressed = false;

		int lightAlpha = sensorColor.alpha();


		if (accumTimerOn) accumTimer++;


		if (lightAlpha >= 10 && !accumIsOn && accumTimer >= 65) {
			ACCUM_SPEED = 0.0;
			accumTimerOn = false;
			accumTimer = 0;
		}

		if (gamepad1.left_bumper) {
			// Emergency stop on accumulator
			ACCUM_SPEED = 0.0;
			accumTimerOn = false;
			accumIsOn = false;
		}

		motorAccum.setPower(ACCUM_SPEED);

		/*
		 * Send telemetry data back to driver station. Note that if we are using
		 * a legacy NXT-compatible motor controller, then the getPower() method
		 * will return a null value. The legacy NXT-compatible motor controllers
		 * are currently write only.
		 */
		// telemetry.addData("Text", "*** Robot Data***");
		// telemetry.addData("LZip", "LZip:  " + String.format("%.2f", lZipPosition));
		// telemetry.addData("RZip", "Rzip:  " + String.format("%.2f", 1.0f - rZipPosition));
		// telemetry.addData("Arm",  "Arm: " + String.format("%.2f", arm));
		// telemetry.addData("dumper", "dumper: "  + String.format("%.2f", dumpPosition));
		// telemetry.addData("LeftCur", "LeftCur: " + motorLeft.getCurrentPosition() );
		// telemetry.addData("RtCur", "RtCur: " + motorRight.getCurrentPosition() );
		// telemetry.addData("Timer", "Timer: " + timer);
		// telemetry.addData("B is", "B is: " + bIsPressed);
		telemetry.addData("Light Alpha is: ", lightAlpha);
		telemetry.addData("Arm motor output: ", armStick);
	}

	/*
	 * Code to run when the op mode is first disabled goes here
	 * 
	 * @see com.qualcomm.robotcore.eventloop.opmode.OpMode#stop()
	 */
	@Override
	public void stop() {

	}

	/*
	 * This method scales the joystick input so for low joystick values, the 
	 * scaled value is less than linear.  This is to make it easier to drive
	 * the robot more precisely at slower speeds.
	 */
	double scaleInput(double dVal)  {
		double[] scaleArray = { 0.0, 0.05, 0.09, 0.10, 0.12, 0.15, 0.18, 0.24,
				0.30, 0.36, 0.43, 0.50, 0.60, 0.72, 0.85, 1.00, 1.00 };

		// get the corresponding index for the scaleInput array.
		int index = (int) (dVal * 16.0);
		if (index < 0) {
			index = -index;
		} else if (index > 16) {
			index = 16;
		}

		double dScale = 0.0;
		if (dVal < 0) {
			dScale = -scaleArray[index];
		} else {
			dScale = scaleArray[index];
		}

		return dScale;
	}

	/**
	 * This does the cubic smoothing equation on joystick value.
	 * Assumes you have already done any deadzone processing.
	 *
	 * @param x  joystick input
	 * @return  smoothed value
	 */
	protected double smoothPowerCurve (double x) {
		//double a = this.getThrottle();
		double a = 1.0;         // Hard code to max smoothing
		double b = 0.05;		// Min power to overcome motor stall

		if (x > 0.0)
			return (b + (1.0-b)*(a*x*x*x+(1.0-a)*x));

		else if (x<0.0)
			return (-b + (1.0-b)*(a*x*x*x+(1.0-a)*x));
		else return 0.0;
	}

	/**
	 * Add deadzone to a stick value
	 *
	 * @param rawStick  Raw value from joystick read -1.0 to 1.0
	 * @param dz	Deadzone value to use 0 to 0.999
	 * @return		Value after deadzone processing
	 */
	protected double deadzone(double rawStick, double dz) {
		double stick;

		// Force limit to -1.0 to 1.0
		if (rawStick > 1.0) {
			stick = 1.0;
		} else if (rawStick < -1.0) {
			stick = -1.0;
		} else {
			stick = rawStick;
		}

		// Check if value is inside the dead zone
		if (stick >= 0.0){
			if (Math.abs(stick) >= dz)
				return (stick - dz)/(1 -  dz);
			else
				return 0.0;

		}
		else {
			if (Math.abs(stick) >= dz)
				return (stick + dz)/(1 - dz);
			else
				return 0.0;

		}
	}

}
