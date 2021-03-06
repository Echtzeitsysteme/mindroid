package org.mindroid.impl.brick;

import java.io.IOException;
import java.util.logging.Logger;

import org.mindroid.api.brick.Brick;
import org.mindroid.api.ev3.EV3StatusLightColor;
import org.mindroid.api.ev3.EV3StatusLightInterval;
import org.mindroid.common.messages.ILoggable;
import org.mindroid.common.messages.brick.BrickMessagesFactory;
import org.mindroid.common.messages.led.StatusLightMessageFactory;
import org.mindroid.common.messages.sound.BeepMessage;
import org.mindroid.common.messages.sound.SoundMessageFactory;
import org.mindroid.impl.errorhandling.ErrorHandlerManager;
import org.mindroid.impl.logging.APILoggerManager;
import org.mindroid.impl.logging.EV3MsgLogger;
import org.mindroid.impl.motor.EV3MotorManager;
import org.mindroid.impl.sensor.EV3SensorManager;


/**
 *
 * Brick controller class.
 * Used to control the brick.
 */
public class EV3Brick extends Brick {
    /** Manager classes for endpoints */
    final EV3MotorManager motorManager;
    final EV3SensorManager sensorManager;
    private EV3Display display;

    private final EV3BrickEndpoint brickEndpoint;

	private final Logger LOGGER = Logger.getLogger(this.getClass().getName());
	private final EV3MsgLogger msgRcvdLogger;
	private final EV3MsgLogger msgSendLogger;

	/**
	 *
	 * @param brickEndpoint - brick Endpoint capsulates the connection to the brick.
	 */
	public EV3Brick (EV3BrickEndpoint brickEndpoint) {
		this.brickEndpoint = brickEndpoint;

        motorManager 	= new EV3MotorManager(brickEndpoint);
        sensorManager 	= new EV3SensorManager(brickEndpoint);

		BrickButtonProvider.getInstance().addButton(new EV3Button(Button.ENTER));
		BrickButtonProvider.getInstance().addButton(new EV3Button(Button.LEFT));
		BrickButtonProvider.getInstance().addButton(new EV3Button(Button.RIGHT));
		BrickButtonProvider.getInstance().addButton(new EV3Button(Button.UP));
		BrickButtonProvider.getInstance().addButton(new EV3Button(Button.DOWN));

		display = new EV3Display();

		/** Add Listeners to Client **/
		brickEndpoint.client.addListener(sensorManager);
		brickEndpoint.client.addListener(motorManager);
		brickEndpoint.client.addListener(display);

		//Set Client to IMotor-/Sensormanager
		sensorManager.setBrickClient(brickEndpoint.client);
		motorManager.setBrickClient(brickEndpoint.client);

		//Init Loggers
		APILoggerManager.getInstance().registerLogger(LOGGER);
		msgRcvdLogger = new EV3MsgLogger(LOGGER,"[ENDPOINT: BRICK] Received ");
		msgSendLogger = new EV3MsgLogger(LOGGER,"[ENDPOINT: BRICK] Send ");
    }


    public boolean connect() {
		try {
			return brickEndpoint.connect();
		} catch (IOException e) {
			ErrorHandlerManager.getInstance().handleError(e,this.getClass(),e.getMessage());
		}
		return false;
	}

	/**
	 * Disconnects all open Connections to the Brick!
	 */
	public void disconnect(){
		brickEndpoint.disconnect();
		getSensorManager().disconnectSensors();
		getMotorManager().disconnectMotors();
	}

	/**
	 * returns true if connection to Brick is established
	 * @return true if the connection is established
	 */
    public boolean isConnected(){
    	return brickEndpoint.isConnected();
    }
    
    /**
     * Returns true if connection to Brick is established and also ready to receive Commands
     * @return true if the brick is ready else false
     */
    public boolean isBrickReady(){
    	return brickEndpoint.isBrickReady();
    }

	@Override
	public void resetBrickState() {
		ILoggable msg = BrickMessagesFactory.createResetBrickMsg();
    	//Resets the Bricks Display,LED,SoundVolume
		brickEndpoint.sendTCPMessage(msg,msgSendLogger);
	}

	public EV3MotorManager getMotorManager() {
		return motorManager;
	}

	public EV3SensorManager getSensorManager() {
		return sensorManager;
	}

	private EV3Display getDisplay() {
		return display;
	}

	public void setDisplay(EV3Display display) {
		this.display = display;
	}

	public void setConnectionInformation(String ip, int tcpPort){
		brickEndpoint.EV3Brick_IP = ip;
		brickEndpoint.EV3Brick_PORT = tcpPort;
	}

	public String getConnectionInformation(){
		return new StringBuffer().append(brickEndpoint.EV3Brick_IP).append(":").append(brickEndpoint.EV3Brick_PORT).toString();
	}




	//-------------- Status Light Operations ----------------
	@Override
	public void setEV3StatusLight(EV3StatusLightColor color, EV3StatusLightInterval interval) {
		ILoggable msg = StatusLightMessageFactory.createSetStatusLightMessage((color.getValue()+3*interval.getValue()));
		brickEndpoint.sendTCPMessage(msg,msgSendLogger);
	}

	@Override
	public void setLEDOff() {
		ILoggable msg = StatusLightMessageFactory.createSetStatusLightMessage(0);
		brickEndpoint.sendTCPMessage(msg,msgSendLogger);
	}


	//-------------- SOUND Operations ----------------
	public void setVolume(int volume){
		ILoggable msg = SoundMessageFactory.createVolumeMessage(volume);
		brickEndpoint.sendTCPMessage(msg,msgSendLogger);
	}

	public void singleBeep(){
		ILoggable msg = SoundMessageFactory.createBeepMessage(BeepMessage.Beeptype.SINGLE_BEEP);
		brickEndpoint.sendTCPMessage(msg,msgSendLogger);
	}

	public void doubleBeep() {
		ILoggable msg = SoundMessageFactory.createBeepMessage(BeepMessage.Beeptype.DOUBLE_BEEP);
		brickEndpoint.sendTCPMessage(msg,msgSendLogger);
	}

	public void buzz() {
		ILoggable msg = SoundMessageFactory.createBeepMessage(BeepMessage.Beeptype.LOW_BUZZ);
		brickEndpoint.sendTCPMessage(msg,msgSendLogger);
	}

	public void beepSequenceDown() {
		ILoggable msg = SoundMessageFactory.createBeepMessage(BeepMessage.Beeptype.BEEP_SEQUENCE_DOWNWARDS);
		brickEndpoint.sendTCPMessage(msg,msgSendLogger);
	}
	public void beepSequenceUp() {
		ILoggable msg = SoundMessageFactory.createBeepMessage(BeepMessage.Beeptype.BEEP_SEQUENCE_UPWARDS);
		brickEndpoint.sendTCPMessage(msg,msgSendLogger);
	}

	@Override
	public void clearDisplay() {
		getDisplay().clearDisplay();
	}

	@Override
	public void drawString(String str,Textsize textsize, int posX, int posY) {
		getDisplay().drawString(str,textsize,posX,posY);
	}

	@Override
	public void drawImage(String str) {
		getDisplay().drawImage(str);
	}

}