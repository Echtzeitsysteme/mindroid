package org.mindroid.api.robot.control;

import org.mindroid.impl.exceptions.BrickIsNotReadyException;
import org.mindroid.impl.exceptions.PortIsAlreadyInUseException;

import java.io.IOException;

/**
 * Created by torben on 02.03.2017.
 */

public interface IRobotCommandCenter {
    void startStatemachine(String id);
    void stopStatemachine(String id);
    String[] getStatemachines();

    void connectToBrick() throws IOException;
    boolean isConnected();

    boolean initializeConfiguration() throws BrickIsNotReadyException, PortIsAlreadyInUseException; //TODO initializeConfiguration()
    boolean isConfigurated();

    void disconnect();


    //public IRobotState getRobotState(); TODO
}
