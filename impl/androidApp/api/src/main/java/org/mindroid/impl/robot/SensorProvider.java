package org.mindroid.impl.robot;

import org.mindroid.common.messages.hardware.Sensormode;
import org.mindroid.impl.ev3.EV3PortID;
import org.mindroid.impl.ev3.EV3PortIDs;
import org.mindroid.impl.logging.APILoggerManager;
import org.mindroid.impl.motor.Motor;
import org.mindroid.impl.sensor.EV3SensorEndpoint;
import org.mindroid.impl.sensor.IEV3SensorEndpoint;
import org.mindroid.impl.sensor.Sensor;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Created by torben on 02.03.2017.
 */

public class SensorProvider{

    private Robot robot;
    HashMap<EV3PortID,Sensor> sensors;

    private final static Logger LOGGER = Logger.getLogger(SensorProvider.class.getName());

    static{
        APILoggerManager.getInstance().registerLogger(LOGGER);
    }

    public SensorProvider(Robot robot){
        this.robot = robot;
        sensors = new HashMap<EV3PortID, Sensor>(4);
    }

    private IEV3SensorEndpoint getSensorEndpoint(EV3PortID sensport){
        if(sensport.equals(EV3PortIDs.PORT_1)){
            return robot.getSensorS1();
        }else if(sensport.equals(EV3PortIDs.PORT_2)){
            return  robot.getSensorS2();
        }else if(sensport.equals(EV3PortIDs.PORT_3)){
            return  robot.getSensorS3();
        }else if(sensport.equals(EV3PortIDs.PORT_4)){
            return  robot.getSensorS4();
        }
        return null;
    }

    /**
     * Returns a Sensor-Object as an simpler interface,
     * @param sensorPort port of the Sensor you want to control.
     * @return Sensor object to control the Sensor plugged into that port
     */
    public Sensor getSensor(EV3PortID sensorPort){
        if(sensorPort == EV3PortIDs.PORT_1 || sensorPort == EV3PortIDs.PORT_2 || sensorPort == EV3PortIDs.PORT_3 ||sensorPort == EV3PortIDs.PORT_4){
            if(sensors.containsKey(sensorPort)){ //Check if motor object already created
                return sensors.get(sensorPort);
            }
            //When not already created, create and put into hashmap
            sensors.put(sensorPort,new Sensor(getSensorEndpoint(sensorPort), sensorPort));//TODO check what happens, when the sensorEndpoint is null? -> Send an info message?

            Sensor tmpSensor = sensors.get(sensorPort);

            LOGGER.log(Level.INFO,"SensorProvider: requested sensor: ["+sensorPort.getLabel()+"] "+tmpSensor.toString());

            return tmpSensor;
        }
        return null;
    }

    /**
     * Clears the {@link #sensors} hashMap, which is necessary, when a new Robot will be created by the Factory.
     */
    protected void clearSensors() {
        sensors.clear();
    }
}
