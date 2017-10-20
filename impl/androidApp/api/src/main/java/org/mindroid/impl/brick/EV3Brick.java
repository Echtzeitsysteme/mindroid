package org.mindroid.impl.brick;

import java.io.IOException;

import org.mindroid.api.ev3.EV3StatusLightColor;
import org.mindroid.api.ev3.EV3StatusLightInterval;
import org.mindroid.api.robot.control.IBrickControl;
import org.mindroid.common.messages.*;
import org.mindroid.common.messages.brick.ButtonMessage;
import org.mindroid.common.messages.brick.HelloMessage;
import org.mindroid.common.messages.led.StatusLightMessageFactory;
import org.mindroid.common.messages.sound.BeepMessage;
import org.mindroid.common.messages.sound.SoundMessageFactory;
import org.mindroid.common.messages.sound.SoundVolumeMessage;
import org.mindroid.impl.motor.EV3MotorManager;
import org.mindroid.impl.sensor.EV3SensorManager;


import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;


/**
 * Brick Endpoint Classes. Used to send proper messages to the Brick.
 */
public class EV3Brick extends Listener implements IBrickControl{ //TODO Extends ClientEndpointImpl

    public final static int BRICK_TIMEOUT = 50000;
    private final Client client;
    
    
    public static String EV3Brick_IP;
    public static String SERVER_IP;
    public static int EV3Brick_PORT;
    
    /** Manager classes for endpoints */
    final EV3MotorManager motorManager;
    final EV3SensorManager sensorManager;
    private final ButtonProvider buttonProvider;
    private EV3Display display;

    /** Brick is ready for commands - will be set true when hello-msg from brick is received!**/
    private boolean readyForCommands = false;
    
    private Connection conn = null;
	private boolean areMessagesRegistered = false;

    /**
	 * Creates a Brick Class.
	 *
	 * @param ev3Brick_IP
	 * @param ev3Brick_PORT
     */
	public EV3Brick (final String ev3Brick_IP, int ev3Brick_PORT) {
        this.EV3Brick_IP = ev3Brick_IP;
        this.EV3Brick_PORT = ev3Brick_PORT;
        
        motorManager 	= new EV3MotorManager(this);
        sensorManager 	= new EV3SensorManager(this);

        buttonProvider = new ButtonProvider();
        buttonProvider.addButton(new EV3Button(Button.ENTER));
        buttonProvider.addButton(new EV3Button(Button.LEFT));
        buttonProvider.addButton(new EV3Button(Button.RIGHT));
        buttonProvider.addButton(new EV3Button(Button.UP));
        buttonProvider.addButton(new EV3Button(Button.DOWN));


        display = new EV3Display();



		System.out.println("Local-EV3Brick: Starte client.. ");
		client = new Client();
		client.start();
		new Thread(client).start(); //Neccessary to keep connection alive!

		/** Add Listeners to Client **/
		client.addListener(this);
		client.addListener(sensorManager);
		client.addListener(motorManager);
		client.addListener(display);

		//Set Client to IMotor-/Sensormanager
		sensorManager.setBrickClient(client);
		motorManager.setBrickClient(client);

		registerMessages(client);
    }


    public boolean connect() throws IOException {
		System.out.println("[Local-EV3Brick] connect to brick "+EV3Brick_IP+":"+EV3Brick_PORT);
		System.out.print("[Local-EV3Brick] Connecting...");

		client.setKeepAliveTCP(10000);
		client.connect(BRICK_TIMEOUT, EV3Brick_IP, EV3Brick_PORT,EV3Brick_PORT-NetworkPortConfig.UDP_OFFSET);

		System.out.println("Local-EV3Brick: Connected successful!");

		return client.isConnected();
    }

	/**
	 * Disconnects all open Connections to the Brick!
	 */
	public void disconnect(){
		System.out.println("[EV3-BRICK:disconnect()] executing disconnect");
		client.close();
		System.out.println("[EV3-BRICK:disconnect()] client state: "+client.isConnected());
		getSensorManager().disconnectSensors();
		getMotorManager().disconnectMotors();
		System.out.println("[EV3-BRICK:disconnect()] Sensors and motors got disconnected");
	}
    
	public EV3MotorManager getMotorManager() {
		return motorManager;
	}

	public EV3SensorManager getSensorManager() {
		return sensorManager;
	}
    
	@Override
	public void received(Connection connection,Object object){
		/** First answer from the Brick if the Connection is established **/
		if(object.getClass() == HelloMessage.class){
			readyForCommands = true;
			conn = connection;
			System.out.println("Local-EV3Brick: "+ object.toString());
		}

		if(object.getClass() == ButtonMessage.class){
			ButtonMessage msg = (ButtonMessage) object;
			EV3Button button;
			button = buttonProvider.getButton(ButtonProvider.getMappedID(msg.getButtonID()));
			if(button != null){
				if(msg.getButtonAction() == ButtonAction.RELEASED.getValue()){
					button.setIsPressed(false);
					return;
				}

				if(msg.getButtonAction() == ButtonAction.PRESSED.getValue()){
					button.setIsPressed(true);
					return;
				}
			}
		}
		
	}
	
   @Override
    public void disconnected(Connection connection) {
        super.disconnected(connection);
        client.close();
        readyForCommands = false;
    }


   
	/**
	 * returns true if connection to Brick is established
	 * @return
	 */
    public boolean isConnected(){
    	return client.isConnected();
    }
    
    /**
     * Returns true if connection to Brick is established and also ready to receive Commands
     * @return
     */
    public boolean isBrickReady(){
    	return (readyForCommands && isConnected());
    }

	private EV3Display getDisplay() {
		return display;
	}

	public void setDisplay(EV3Display display) {
		this.display = display;
	}

	public void setConnectionInformation(String ip, int tcpPort){
		EV3Brick_IP = ip;
		EV3Brick_PORT = tcpPort;
	}

	public String getConnectionInformation(){
		return new StringBuffer().append(EV3Brick_IP).append(":").append(EV3Brick_PORT).toString();
	}

	private void registerMessages(Client client) {
		if (!areMessagesRegistered){
			MessageRegistrar.register(client);
			areMessagesRegistered = true;
		}
	}


	//-------------- Status Light Operations ----------------
	@Override
	public void setEV3StatusLight(EV3StatusLightColor color, EV3StatusLightInterval interval) {
		if(isBrickReady()){
			conn.sendTCP(StatusLightMessageFactory.createSetStatusLightMessage((color.getValue()+3*interval.getValue())));
		}else{
			//TODO@revise: Show in some kind of error log...
			System.err.println("StatusLight is not ready yet. Check Brick connection!");
		}
	}

	@Override
	public void setLEDOff() {
		if(isBrickReady()){
			conn.sendTCP(StatusLightMessageFactory.createSetStatusLightMessage(0));
		}else{
			System.err.println("StatusLight is not ready yet. Check Brick connection!");
		}
	}

	//-------------- SOUND Operations ----------------
	public void setVolume(int volume){
		if(isBrickReady()){
			conn.sendTCP(SoundMessageFactory.createVolumeMessage(volume));
		}
	}

	public void singleBeep(){
		if(isBrickReady()){
			conn.sendTCP(SoundMessageFactory.createBeepMessage(BeepMessage.Beeptype.SINGLE_BEEP));
		}
	}

	public void doubleBeep() {
		if(isBrickReady()){
			conn.sendTCP(SoundMessageFactory.createBeepMessage(BeepMessage.Beeptype.DOUBLE_BEEP));
		}
	}

	public void buzz() {
		if(isBrickReady()){
			conn.sendTCP(SoundMessageFactory.createBeepMessage(BeepMessage.Beeptype.LOW_BUZZ));
		}
	}

	public void beepSequenceDown() {
		if(isBrickReady()){
			conn.sendTCP(SoundMessageFactory.createBeepMessage(BeepMessage.Beeptype.BEEP_SEQUENCE_DOWNWARDS));
		}
	}
	public void beepSequenceUp() {
		if(isBrickReady()){
			conn.sendTCP(SoundMessageFactory.createBeepMessage(BeepMessage.Beeptype.BEEP_SEQUENCE_UPWARDS));
		}
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

	@Override
	public ButtonProvider getButtonProvider() {
		return buttonProvider;
	}
}