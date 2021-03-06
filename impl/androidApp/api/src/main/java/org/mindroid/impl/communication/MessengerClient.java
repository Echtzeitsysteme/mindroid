package org.mindroid.impl.communication;

import org.mindroid.api.AbstractImperativeImplExecutor;
import org.mindroid.api.communication.IMessageListener;
import org.mindroid.api.communication.IMessageServer;
import org.mindroid.api.communication.IMessenger;
import org.mindroid.common.messages.server.MessageMarshaller;
import org.mindroid.common.messages.server.MessageType;
import org.mindroid.common.messages.server.MindroidMessage;
import org.mindroid.common.messages.server.RobotId;
import org.mindroid.impl.errorhandling.ErrorHandlerManager;
import org.mindroid.impl.imperative.ImperativeImplExecutor;
import org.mindroid.impl.logging.APILoggerManager;
import org.mindroid.impl.util.Messaging;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Felicia Ruppel on 04.04.17.
 *
 * Refactored by Torben Unzicker 09.11.17
 * - This Class now connects to the message server and provides an output stream for sending messages (as before) but in addition
 *   uses the ServerWorker to handle incoming messages.
 * - The Connection to the Server will now be kept alive until disconnectFromBrick gets called.
 * - This class now offers an isConnectedToBrick() method.
 *
 */
public class MessengerClient implements IMessenger, IMessageListener,IMessageServer {

    private static final int CONNECT_TIMEOUT = 2000;

    public static final String SERVER_LOG = RobotId.SERVER_LOG.getValue();
    public static final String BROADCAST = RobotId.BROADCAST.getValue();

    private static final Logger LOGGER = Logger.getLogger(MessengerClient.class.getName());

    static{
        APILoggerManager.getInstance().registerLogger(LOGGER);
    }

    private String robotID;
    private InetAddress serverip;
    private int serverport;

    private Socket socket;
    private PrintWriter out;
    private ServerWorker in; //handles incoming messages

    private final MessageMarshaller serverMessageMarshaller = new MessageMarshaller();
    private final ArrayList<MindroidMessage> messages = new ArrayList<MindroidMessage>();
    private AbstractImperativeImplExecutor.SessionStateObserver observer;

    public MessengerClient(String robotID){
        this.robotID = robotID;
        this.socket = new Socket();
        setSocketProperties();
    }

    private void setSocketProperties(){
        try {
            socket.setKeepAlive(true);
        } catch (SocketException e) {
            ErrorHandlerManager.getInstance().handleError(e,MessengerClient.class,"Could not set keepAlive: "+e.getMessage());
        }
    }

    /**
     * Sends the message if a connection is established.
     * If not connection is established the ErrorHandlerManager will be called about this problem.
     * @param msg - msg to send
     */
    private synchronized void sendMessage(MindroidMessage msg){
        if(out != null && socket != null && socket.isConnected()) {
            out.println(getSerializedMessage(msg));
        }
    }

    /**
     *
     * Connects the messenger with the MessageServer.
     * The Connection will be kept alive.
     * Only connects if the connection is not established yet.
     *
     * Note: This client can only connect to one server at the time.
     *
     * @param msgServerIP - ip of message server
     * @param msgServerTCPPort - port of message server
     * @return true, if connection was successful
     */
    public synchronized boolean connect(String msgServerIP, int msgServerTCPPort){
        //Check for valid ip and port
        if(!(Messaging.isValidIP(msgServerIP) && Messaging.isValidPort(msgServerTCPPort))){
            //Port or IP are invalid, return.
            IllegalArgumentException e = new IllegalArgumentException("Invalid IP or/and Port");
            ErrorHandlerManager.getInstance().handleError(e,MessengerClient.class,e.getMessage());
            return false;
        }

        if(socket.isClosed()){
            //Create a new socket, when the old one got closed/disconnected
            socket = new Socket();
        }

        if(!isConnected()) {
            try {


                //set vars
                this.serverip = InetAddress.getByName(msgServerIP);
                this.serverport = msgServerTCPPort;

                //Socket
                socket.connect(new InetSocketAddress(serverip, serverport), CONNECT_TIMEOUT);

                //OutputStream
                out = new PrintWriter(socket.getOutputStream(),
                        true);

                //InputStream - also adds this class as a listener
                in = new ServerWorker(socket,this);
                in.addMessageListener(this);
                new Thread(in).start();

                //Register to server
                registerToServer(serverport);

                return true;
            } catch (IOException e) {
                disconnect();
                ErrorHandlerManager.getInstance().handleError(e, MessengerClient.class, "Error while trying to connect to message Server: " + e.getMessage());
                return false;
            }
        }else{
            ErrorHandlerManager.getInstance().handleError(new Exception("Connection not established"), MessengerClient.class, "socket null or already conected");
            return false;
        }

    }

    /**
     * Disconnects the Client.
     * Closes all open Streams.
     */
    public synchronized void disconnect(){
        if(out != null){
            out.println("<close>");
            out.close();
        }

        if(socket != null){
            try {
                socket.close();
            } catch (IOException e) {
                ErrorHandlerManager.getInstance().handleError(e, MessengerClient.class, "Error while closing MessengerClient: " + e.getMessage());
            }
        }
    }

    /**
     * Returns the serialized message
     * @param msg - msg to serialize
     * @return serialized message as String
     */
    private String getSerializedMessage(MindroidMessage msg){
        return serverMessageMarshaller.serialize(msg);
    }

    @Override
    public void sendMessage(String destination, String content) {
        MessageType type;
        if (destination.equals(IMessenger.SERVER_LOG)) {
            type = MessageType.LOG;
        } else {
            type = MessageType.MESSAGE;
        }
        MindroidMessage msgObj = new MindroidMessage(new RobotId(robotID), new RobotId(destination), type, content);
        sendMessage(msgObj);
    }

    @Override
    public void registerToServer(int port) {
        MindroidMessage msgObj = new MindroidMessage(new RobotId(robotID), RobotId.SERVER_LOG, MessageType.REGISTRATION, ""+port);
        sendMessage(msgObj);
    }

    @Override
    public synchronized void sendLogMessage(String content) {
        MindroidMessage msgObj = new MindroidMessage(new RobotId(robotID), RobotId.SERVER_LOG, MessageType.LOG, content);
        sendMessage(msgObj);
    }

    @Override
    public synchronized boolean isConnected() {
        if(socket != null && in != null){
            return in.isConnected();
        }
        return false;
    }


    /**
     * This messengerClient is registered to the server worker. The messages the server worker receives will be added to the client using this method
     * @param msg - message to handle
     */
    @Override
    public void handleMessage(MindroidMessage msg) {
        if(msg.getMessageType().equals(MessageType.SESSION) && msg.getSessionRobotCount() == MindroidMessage.STOP_SESSION){
            observer.stopExecution();
        }
        LOGGER.log(Level.INFO, "rcvd msg: " + msg.toString());
        getMessages().add(msg);

    }

    @Override
    public int getReceivedMessagesCount(){
        return getMessages().size();
    }

    @Override
    public boolean hasMessage() {
        return getMessages().iterator().hasNext();
    }

    @Override
    public MindroidMessage getNextMessage() {
        if(getMessages().size() >= 1){
            //Get message
            MindroidMessage msg = getMessages().get(0);
            //remove message from line
            getMessages().remove(0);
            return msg;
        }else{
            return null;
        }
    }

    private ArrayList<MindroidMessage> getMessages(){
        return messages;
    }

    @Override
    public void registerMsgListener(IMessageListener listener) {
        if(in != null){
            in.addMessageListener(listener);
        }
    }

    public void setRobotID(String robotID) {
        this.robotID = robotID;
    }

    /**
     * Clears the Message cache.
     * All stored messages will be deleted.
     */
    public void clearMessageCache() {
        messages.clear();
    }

    public void sendSessionMessage(int sessionRobotCount) {
        MindroidMessage sessionMessage = new MindroidMessage(new RobotId(robotID), RobotId.SERVER_LOG, MessageType.SESSION, "", sessionRobotCount);
        sendMessage(sessionMessage);
    }

    public void addObserver(AbstractImperativeImplExecutor.SessionStateObserver obs) {
        this.observer = obs;
    }
}
