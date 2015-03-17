package com.sunilsahoo.drivesafe.utility;

public interface TestResultCategory {
	byte DISABLED = 101;
	byte EMERGENCY = 103;
	byte FAILED = 104;
	byte PASSED = 107;
	byte START_TEST = 120;
	byte INITIATE_EMERGENCY = 105;
	byte UNKNOWN = 106;
	
	byte PAUSED = 108;
	byte SCREEN = 109;
	byte TIMEOUT = 110;
}
