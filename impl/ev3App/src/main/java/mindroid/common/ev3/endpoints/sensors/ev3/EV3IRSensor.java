package mindroid.common.ev3.endpoints.sensors.ev3;

import org.mindroid.common.messages.SensorMessages;
import org.mindroid.common.messages.Sensors;
import lejos.hardware.port.Port;

/**
 * Created by torben on 27.01.2017.
 */

public class EV3IRSensor extends AbstractSensor {

    public EV3IRSensor(Port sensorPort, SensorMessages.SensorMode_ mode) {
        super(SensorSampleRates.SENS_IR_SAMPLERATE);
        isSensorCreated = create(sensorPort, Sensors.EV3IRSensor,mode); //Creates Lejos.EV3ColorSensor. acceptable
        if(isSensorCreated){
            sendSensorData();
        }
        System.out.println(toString());;
    }

    @Override
    public boolean setSensorMode(SensorMessages.SensorMode_ newMode) {
        switch(newMode){
            // Measures the distance to an object in front of the sensor
            case DISTANCE:  sensor.setCurrentMode(newMode.getValue()); return true;
                // Locates up to four beacons
            case SEEK:		sensor.setCurrentMode(newMode.getValue()); return true;
            default: return false;
        }
    }
    
	@Override
	public String toString() {
		return "EV3ColorSensor [sensor=" + sensor + ", sensortype=" + sensortype + ", sensormode=" + sensormode
				+ ", sensorPort=" + sensorPort + ", sampleRate=" + sampleRate + ", isSensorCreated=" + isSensorCreated
				+ "]";
	}
}
