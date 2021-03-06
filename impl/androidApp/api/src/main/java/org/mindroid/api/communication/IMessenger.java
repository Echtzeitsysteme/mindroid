package org.mindroid.api.communication;


import org.mindroid.common.messages.server.RobotId;

/**
 * Created by torben on 04.04.2017.
 */
public interface IMessenger {

    public static final String SERVER_LOG = RobotId.SERVER_LOG.getValue();
    public static final String BROADCAST = RobotId.BROADCAST.getValue();


    /**
     * Sends a message to the given destination. If the destination is the server, MessageType INFO is used.
     * For other Log-MessageTypes use sendLogMessage(String content, MessageType type)
     *
     * @param destination - destination of the message
     * @param content - text of the message
     */
    void sendMessage(String destination, String content);


    /**
     *
     * Is called by the Robot Server. Does not need to be called manually.
     *
     * @param port - port of the server
     */
    void registerToServer(int port);

    /**
     * Sends a log message to the server.
     *
     * @param content - content of the message
     */
    void sendLogMessage(String content);

    /**
     * Returns the state of the connection.
     * @return true if connected
     */
    boolean isConnected();


}
