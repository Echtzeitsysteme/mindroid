package org.mindroid.impl.brick;

import java.io.IOException;

import org.mindroid.api.ev3.EV3StatusLightColor;
import org.mindroid.api.ev3.EV3StatusLightEnabled;
import org.mindroid.api.ev3.EV3StatusLightInterval;
import org.mindroid.impl.motor.EV3MotorManager;
import org.mindroid.impl.sensor.EV3SensorManager;


import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import org.mindroid.common.messages.BrickMessages;
import org.mindroid.common.messages.MessageRegistrar;
import org.mindroid.common.messages.NetworkPortConfig;
import org.mindroid.common.messages.StatusLightMessages;


public class EV3Brick extends Listener implements EV3StatusLightEnabled{ //TODO Extends ClientEndpointImpl

    public final static int BRICK_TIMEOUT = 50000;
    private final Client client;
    
    
    public static String EV3Brick_IP;
    public static String SERVER_IP;
    public static int EV3Brick_PORT;
    
    /** Manager classes for endpoints */
    final EV3MotorManager motorManager;
    final EV3SensorManager sensorManager;
    private EV3Display display;

    /** Brick is ready for commands - will be set true when hello-msg from brick is received!**/
    private boolean readyForCommands = false;
    
    private Connection conn = null;


    /**
	 * Creates a Brick Class.
	 *
	 * @param ev3Brick_IP
	 * @param ev3Brick_PORT
     */
	public EV3Brick (final String ev3Brick_IP, int ev3Brick_PORT) {
        this.EV3Brick_IP = ev3Brick_IP;
        this.EV3Brick_PORT = ev3Brick_PORT;
        
        motorManager 	= new EV3MotorManager(this); //TODO
        sensorManager 	= new EV3SensorManager(this);

		System.out.println("Local-EV3Brick: Starte client.. ");
		client = new Client();
		client.start();
		new Thread(client).start(); //Neccessary to keep connection alive!

		/** Add Listeners to Client **/
		client.addListener(this);
		client.addListener(sensorManager);
		client.addListener(motorManager);

		//Set Client to Motor-/Sensormanager
		sensorManager.setBrickClient(client);
		motorManager.setBrickClient(client);

		MessageRegistrar.register(client);


    }
    
    public void createDisplay(){
    	if(display == null){
    		display = new EV3Display(EV3Brick_IP,NetworkPortConfig.DISPLAY_PORT,BRICK_TIMEOUT);
    		client.addListener(display);
    		client.sendTCP(BrickMessages.createDisplay());
    		System.out.println("Local-EV3Brick: Display created");
    	}
    }


    public boolean connect() throws IOException {
		System.out.println("Local-EV3Brick: connect to brick "+EV3Brick_IP+":"+EV3Brick_PORT);
		System.out.print("Local-EV3Brick: Connecting...");

		client.setKeepAliveTCP(10000);
		client.connect(BRICK_TIMEOUT, EV3Brick_IP, EV3Brick_PORT,EV3Brick_PORT-NetworkPortConfig.UDP_OFFSET);

		System.out.println("Local-EV3Brick: Connected successful!");

		return client.isConnected();
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
		if(object.getClass() == BrickMessages.HelloMessage.class){
			readyForCommands = true;
			conn = connection;
			System.out.println("Local-EV3Brick: "+((BrickMessages.HelloMessage)object).toString());
		}
		
	}
	
   @Override
    public void disconnected(Connection connection) {
        super.disconnected(connection);
        readyForCommands = false;
    }
 
	@Override
	public void setEV3StatusLight(EV3StatusLightColor color, EV3StatusLightInterval interval) {
		if(isBrickReady()){
			conn.sendTCP(StatusLightMessages.setStatusLight(color.getValue()+3*interval.getValue()));
		}else{
			System.err.println("StatusLight is not ready yet. Check Brick connection!");
		}
	}

	@Override
	public void resetEV3StatusLight() {
		if(isBrickReady()){
			conn.sendTCP(StatusLightMessages.setStatusLight(0));
		}else{
			System.err.println("StatusLight is not ready yet. Check Brick connection!");
		}
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

	public EV3Display getDisplay() {
		
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
}