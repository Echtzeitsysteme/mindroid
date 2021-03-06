package org.mindroid.impl.robot;

import org.mindroid.common.messages.motor.synchronization.OperationType;
import org.mindroid.common.messages.motor.synchronization.SynchronizedMotorOperation;
import org.mindroid.impl.errorhandling.ErrorHandlerManager;
import org.mindroid.impl.ev3.EV3PortID;
import org.mindroid.impl.ev3.EV3PortIDs;
import org.mindroid.impl.motor.EV3RegulatedMotorEndpoint;
import org.mindroid.impl.motor.Motor;
import org.mindroid.impl.motor.SynchronizedMotors;
import org.mindroid.impl.robot.Robot;

import java.util.HashMap;

/**
 *
 * This class is used to control all Motors. The wanted operation will be executed by the motor
 * given by the port.
 *
 *
 * Created by torben on 02.03.2017.
 */
public class MotorProvider implements org.mindroid.api.robot.control.MotorProvider {

    private Robot robot;

    private HashMap<EV3PortID,Motor> motors;

    private SynchronizedMotors syncedMotors;

    public MotorProvider(Robot robot){
        this.robot = robot;
        motors = new HashMap<>(4);
    }

    private EV3RegulatedMotorEndpoint getMotorEndpoint(EV3PortID motorPort){
        if(motorPort.equals(EV3PortIDs.PORT_A)){
            return robot.getIMotor_A();
        }else if(motorPort.equals(EV3PortIDs.PORT_B)){
            return  robot.getIMotor_B();
        }else if(motorPort.equals(EV3PortIDs.PORT_C)){
            return  robot.getIMotor_C();
        }else if(motorPort.equals(EV3PortIDs.PORT_D)){
            return  robot.getIMotor_D();
        }
        return null;
    }

    /**
     * Returns a Motor-Object as an simpler interface,
     * @param motorPort port of the Motor you want to control.
     * @return Motor object to control the motor plugged into that port
     */
    public Motor getMotor(EV3PortID motorPort){
        if(motorPort == EV3PortIDs.PORT_A || motorPort == EV3PortIDs.PORT_B || motorPort == EV3PortIDs.PORT_C ||motorPort == EV3PortIDs.PORT_D){
            if(motors.containsKey(motorPort)){ //Check if motor object already created
                return motors.get(motorPort);
            }
            //When not already created, create and put into hashmap
            motors.put(motorPort,new Motor(getMotorEndpoint(motorPort), motorPort));
            return motors.get(motorPort);
        }
        return null;
    }

    public SynchronizedMotors getSynchronizedMotors(){
        if(syncedMotors == null){
            return (syncedMotors = new SynchronizedMotors(robot.getSyncedMotors()));
        }else{
            return syncedMotors;
        }

    }

    /**
     * Stops all motors that were accessed by getMotor(..) method.
     */
    public void stopAllMotors(){
        for (EV3PortID ev3PortID : motors.keySet()) {
            motors.get(ev3PortID).stop();
        }
    }

    /**
     * Clears the  {@link #motors} hashMap, which is necessary, when a new Robot will be created by the Factory.
     */
    protected void clearMotors() {
        motors.clear();
        syncedMotors = null;
    }
}
