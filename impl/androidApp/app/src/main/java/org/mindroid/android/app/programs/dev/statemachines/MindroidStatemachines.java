package org.mindroid.android.app.programs.dev.statemachines;

import org.mindroid.api.StatemachineAPI;
import org.mindroid.api.ev3.EV3StatusLightColor;
import org.mindroid.api.ev3.EV3StatusLightInterval;
import org.mindroid.api.statemachine.IState;
import org.mindroid.api.statemachine.IStatemachine;
import org.mindroid.api.statemachine.ITransition;
import org.mindroid.api.statemachine.constraints.IConstraint;
import org.mindroid.common.messages.motor.synchronization.SyncedMotorOpFactory;
import org.mindroid.common.messages.motor.synchronization.SynchronizedMotorOperation;
import org.mindroid.impl.brick.Textsize;
import org.mindroid.impl.ev3.EV3PortIDs;
import org.mindroid.impl.statemachine.State;
import org.mindroid.impl.statemachine.Statemachine;
import org.mindroid.impl.statemachine.Transition;
import org.mindroid.impl.statemachine.constraints.*;
import org.mindroid.impl.statemachine.properties.MessageProperty;
import org.mindroid.impl.statemachine.properties.Seconds;
import org.mindroid.impl.statemachine.properties.Milliseconds;
import org.mindroid.impl.statemachine.properties.sensorproperties.Angle;
import org.mindroid.impl.statemachine.properties.sensorproperties.Color;
import org.mindroid.impl.statemachine.properties.sensorproperties.Distance;

import java.util.Random;

import static org.mindroid.api.communication.IMessenger.SERVER_LOG;

/**
 * Created by torben on 02.03.2017.
 *
 * Statemachine programming level
 *
 */

public class MindroidStatemachines extends StatemachineAPI {

    public MindroidStatemachines() {
        initStatemachines();
    }

    public void initStatemachines()  {

        IStatemachine tmpStatemachine;
        //Statemachine rotate 90 degrees
        tmpStatemachine = roationMachine();
        statemachineCollection.addStatemachine(tmpStatemachine.getID(),tmpStatemachine);
        //Statemachine synchronized Wall ping pong
        tmpStatemachine = synchronizedWallPingPong();
        statemachineCollection.addStatemachine(tmpStatemachine.getID(),tmpStatemachine);
        //Statemachine Wall ping pong
        tmpStatemachine = wallPingPong();
        statemachineCollection.addStatemachine(tmpStatemachine.getID(),tmpStatemachine);
        //Statemachine Wall lightshow
        tmpStatemachine = lightshowBig();
        statemachineCollection.addStatemachine(tmpStatemachine.getID(),tmpStatemachine);
        //Statemachine test lightshow
        tmpStatemachine = lightshowSmall();
        statemachineCollection.addStatemachine(tmpStatemachine.getID(),tmpStatemachine);
        //Statemachine test transitions
        tmpStatemachine = testTransitionCopy();
        statemachineCollection.addStatemachine(tmpStatemachine.getID(),tmpStatemachine);
        //Statemachine test Sound
        tmpStatemachine = soundTestStatemachine();
        statemachineCollection.addStatemachine(tmpStatemachine.getID(),tmpStatemachine);
        //Statemachine test Display Drawings
        tmpStatemachine = displayDrawingTestStatemachine();
        statemachineCollection.addStatemachine(tmpStatemachine.getID(),tmpStatemachine);
        tmpStatemachine = messaging4Robots();
        statemachineCollection.addStatemachine(tmpStatemachine.getID(),tmpStatemachine);

    }


    public IStatemachine displayDrawingTestStatemachine()  {
        IStatemachine sm = new Statemachine("TestDisplayStatemachine");

        IState state_clearDisplay = new State("createClearDisplayMsg"){
            public void run(){
                clearDisplay();
            }
        };

        IState state_drawString = new State("createDrawStringMsg"){
            public void run(){
                drawString("SMALL STRING", Textsize.SMALL,1,10);
                drawString("MEDIUM STRING", Textsize.MEDIUM,1,25);
                drawString("LARGE STRING", Textsize.LARGE,1,45);
            }
        };

        sm.addState(state_clearDisplay);
        sm.addState(state_drawString);

        sm.setStartState(state_clearDisplay);

        ITransition t_one_sec = new Transition(new TimeExpired(new Seconds(1)));
        ITransition t_two_sec = new Transition(new TimeExpired(new Seconds(2)));

        sm.addTransition(t_two_sec,state_clearDisplay,state_drawString);
        sm.addTransition(t_two_sec,state_drawString,state_clearDisplay);


        return sm;
    }

    /**
     * Plays Sounds sequentially singleBeep,doubleBeep,sequenceDown,sequenceUp,buzz
     * @return
     */
    public IStatemachine soundTestStatemachine () {
        IStatemachine sm = new Statemachine("TestSoundStatemachine");

        IState state_beep = new State("SingleBeep"){

            public void run(){

                setSoundVolume(50);
                playSingleBeep();
            }
        };


        IState state_doubleBeep = new State("DoubleBeep"){
            public void run(){
                playDoubleBeep();
            }
        };



        IState state_buzz = new State("Buzz"){
            public void run(){
                playBuzzSound();
            }
        };

        IState state_sequenceDown = new State("SequenceDown"){
            public void run(){
                playBeepSequenceDown();
            }
        };


        IState state_sequenceUp = new State("SequenceUp"){
            public void run(){
                playBeepSequenceUp();
            }
        };

        sm.addState(state_beep);
        sm.addState(state_buzz);
        sm.addState(state_doubleBeep);
        sm.addState(state_sequenceDown);
        sm.addState(state_sequenceUp);

        sm.setStartState(state_beep);

        ITransition t_one_sec = new Transition(new TimeExpired(new Seconds(1)));
        ITransition t_two_sec = new Transition(new TimeExpired(new Seconds(2)));

        sm.addTransition(t_one_sec,state_beep,state_doubleBeep);
        sm.addTransition(t_one_sec,state_doubleBeep,state_buzz);
        sm.addTransition(t_two_sec,state_buzz,state_sequenceDown);
        sm.addTransition(t_two_sec,state_sequenceDown,state_sequenceUp);
        sm.addTransition(t_two_sec,state_sequenceUp,state_beep);

        return sm;
    }

    /**
     * This statemachine should run on 4 robots at the same time to test the messaging feature.
     * NOTE: Robot "A" should receive "sendInitialMsg" as start state, you might need to ensure this manually
     * @return the statemachine
     */
    public IStatemachine messaging4Robots()  {
        IStatemachine sm = new Statemachine("TestMessagingWith4");

        final String player_1 = "A";
        final String player_2 = "B";
        final String player_3 = "C";
        final String player_4 = "D";

        IConstraint cnstr_MsgReceived1 = new MsgReceived(new MessageProperty("MSG", player_1));
        IConstraint cnstr_MsgReceived2 = new MsgReceived(new MessageProperty("MSG", player_2));
        IConstraint cnstr_MsgReceived3 = new MsgReceived(new MessageProperty("MSG", player_3));
        IConstraint cnstr_MsgReceived4 = new MsgReceived(new MessageProperty("MSG", player_4));
        ITransition t_msgReceived = new Transition(new OR(new OR(cnstr_MsgReceived1, cnstr_MsgReceived2), new OR(cnstr_MsgReceived3, cnstr_MsgReceived4)));

        IState state_sendMsg = new State("sendMsg") {
            public void run() {
                Random rand = new Random();
                int randomNum = rand.nextInt(4) + 1;
                switch (randomNum) {
                    case 1: sendMessage(player_1, "MSG");
                            break;
                    case 2: sendMessage(player_2, "MSG");
                            break;
                    case 3: sendMessage(player_3, "MSG");
                            break;
                    case 4: sendMessage(player_4, "MSG");
                            break;
                }

            }
        };
        sm.addState(state_sendMsg);

        if(getRobotID().equals("A")) {
            IState state_initialMsg = new State("sendInitialMsg") {
                public void run() {
                    sendMessage(player_2, "MSG");
                    sendMessage(player_3, "MSG");
                }
            };
            sm.addState(state_initialMsg);
            sm.setStartState(state_initialMsg);
            sm.addTransition(t_msgReceived, state_initialMsg, state_sendMsg);
        } else {
            IState state_waiting = new State("waitingForMessage") {
                public void run() {

                }
            };
            sm.addState(state_waiting);
            sm.setStartState(state_waiting);
            sm.addTransition(t_msgReceived, state_waiting, state_sendMsg);
        }
            sm.addTransition(t_msgReceived, state_sendMsg, state_sendMsg);




        return sm;
    }

    public IStatemachine roationMachine() {
        IStatemachine sm = new Statemachine("RotationMachine");

        IState state_rotate = new State("Rotating"){
            @Override
            public void run(){
                //TURN RIGHT
                getMotorProvider().getMotor(EV3PortIDs.PORT_A).forward();
                getMotorProvider().getMotor(EV3PortIDs.PORT_D).backward();

                setLED(LED_YELLOW_BLINKING);

                getMotorProvider().getMotor(EV3PortIDs.PORT_A).setSpeed(500);
                getMotorProvider().getMotor(EV3PortIDs.PORT_D).setSpeed(500);

            }
        };

        //90 Degree right turn
        IConstraint constr_ninety_degree = new Rotation(90f,new Angle(EV3PortIDs.PORT_3));
        ITransition trans_rotate = new Transition(constr_ninety_degree);

        IState state_wait = new State("waiting a while"){
            @Override
            public void run(){
                setLED(LED_GREEN_ON);
                getMotorProvider().getMotor(EV3PortIDs.PORT_A).stop();
                getMotorProvider().getMotor(EV3PortIDs.PORT_D).stop();
            }
        };

        IConstraint constr_wait = new TimeExpired(new Seconds(3));
        ITransition trans_wait = new Transition(constr_wait);


        sm.addState(state_rotate);
        sm.addState(state_wait);
        sm.addTransition(trans_rotate,state_rotate,state_wait);
        sm.addTransition(trans_wait,state_wait,state_rotate);

        sm.setStartState(state_wait);

        return sm;
    }


    public IStatemachine synchronizedWallPingPong() {
        IStatemachine sm = new Statemachine("syncWallPingPong");

        final String cmd_red = "RED";
        final String cmd_yellow = "YELLOW";
        final String cmd_green = "GREEN";

        String player_1 = "Bobby";
        String player_2 = "Lea";

        final String other_player;

        //WHO AM I?
        if(getRobotID().equals(player_1)){
            other_player = player_2;
            final String myID = player_1;
        }else{
            other_player = player_1;
            final String myID = player_2;

        }
        final String player_dest = other_player;

        IState state_idle = new State("Idle") {
            @Override
            public void run() {
                //FORWARD
                getMotorProvider().getMotor(EV3PortIDs.PORT_A).stop();
                getMotorProvider().getMotor(EV3PortIDs.PORT_D).stop();

                setLED(LED_YELLOW_BLINKING);
            }
        };


        IState state_forward = new State("Forward") {
            @Override
            public void run() {
                //FORWARD
                getMotorProvider().getMotor(EV3PortIDs.PORT_A).forward();
                getMotorProvider().getMotor(EV3PortIDs.PORT_D).forward();

                setLED(LED_GREEN_ON);

                getMotorProvider().getMotor(EV3PortIDs.PORT_A).setSpeed(500);
                getMotorProvider().getMotor(EV3PortIDs.PORT_D).setSpeed(500);
            }
        };

        IState state_wait = new State("WaitingForMessage") {
            @Override
            public void run() {
                //Wait
                getMotorProvider().getMotor(EV3PortIDs.PORT_A).stop();
                getMotorProvider().getMotor(EV3PortIDs.PORT_D).stop();

                setLED(LED_GREEN_BLINKING);
            }
        };


        IState state_backward = new State("backward") {
            @Override
            public void run() {
                System.out.println(this.getName() + " isActive\n");
                //BACKWARD
                getMotorProvider().getMotor(EV3PortIDs.PORT_A).backward();
                getMotorProvider().getMotor(EV3PortIDs.PORT_D).backward();

                setLED(LED_RED_BLINKING);

                getMotorProvider().getMotor(EV3PortIDs.PORT_A).setSpeed(500);
                getMotorProvider().getMotor(EV3PortIDs.PORT_D).setSpeed(500);
            }
        };

        IState state_turn = new State("turn") {
            @Override
            public void run() {
                System.out.println(this.getName() + " isActive\n");
                //TURN LEFT
                getMotorProvider().getMotor(EV3PortIDs.PORT_A).backward();
                getMotorProvider().getMotor(EV3PortIDs.PORT_D).forward();

                setLED(LED_YELLOW_BLINKING);

                getMotorProvider().getMotor(EV3PortIDs.PORT_A).setSpeed(500);
                getMotorProvider().getMotor(EV3PortIDs.PORT_D).setSpeed(500);
            }
        };

        IState state_sendStartMsg = new State("BackToStart") {
            @Override
            public void run() {
                System.out.println(this.getName() + " isActive\n");
                //TURN LEFT
                getMotorProvider().getMotor(EV3PortIDs.PORT_A).forward();
                getMotorProvider().getMotor(EV3PortIDs.PORT_D).forward();

                setLED(LED_GREEN_ON);

                getMotorProvider().getMotor(EV3PortIDs.PORT_A).setSpeed(500);
                getMotorProvider().getMotor(EV3PortIDs.PORT_D).setSpeed(500);

                sendMessage(player_dest,"START");

            }
        };


        IConstraint cnstr_leader = new OR(new EQ(Color.BLACK,new Color(EV3PortIDs.PORT_1)),new EQ(Color.BLACK,new Color(EV3PortIDs.PORT_4)));
        Transition trans_iamLeader = new Transition(cnstr_leader){
            @Override
            public void run(){
                sendMessage(player_dest,"I AM THE LEADER");
            }
        };

        //--- Transitionen
        IConstraint cnstr_iamFollower = new MsgReceived(new MessageProperty("I AM THE LEADER",other_player));
        Transition trans_iamFollower = new Transition(cnstr_iamFollower);

        IConstraint cnstr_rcvdStartMsg = new MsgReceived(new MessageProperty("START",other_player));
        Transition trans_rcvdStartMsg = new Transition(cnstr_rcvdStartMsg);

        IConstraint distance_collision = new LT(0.15f, new Distance(EV3PortIDs.PORT_2));
        ITransition trans_collision = new Transition(distance_collision);

        IConstraint time_driving_backward = new TimeExpired(new Milliseconds(1200));
        ITransition trans_drive_backwards = new Transition(time_driving_backward);

        IConstraint time_180turn = new TimeExpired(new Milliseconds(1400));
        ITransition trans_done_turn_180 = new Transition(time_180turn);

        IConstraint time_stop = new TimeExpired(new Seconds(4));
        ITransition trans_stop = new Transition(time_stop);


        // SETUP STATEMACHINE
        //Set start states ------
        sm.addState(state_idle);
        sm.setStartState(state_idle);

        //Add States ------
        sm.addState(state_forward);
        sm.addState(state_backward);
        sm.addState(state_turn);
        sm.addState(state_sendStartMsg);
        sm.addState(state_wait);

        sm.addTransition(trans_iamLeader,state_idle,state_forward);
        sm.addTransition(trans_iamFollower,state_idle,state_wait);
        sm.addTransition(trans_rcvdStartMsg,state_wait,state_forward);

        sm.addTransition(trans_collision,state_forward,state_backward);
        sm.addTransition(trans_drive_backwards, state_backward, state_turn);
        sm.addTransition(trans_done_turn_180, state_turn, state_sendStartMsg);
        sm.addTransition(trans_stop,state_sendStartMsg,state_idle);

        return sm;
    }


    public IStatemachine lightshowBig() {
        IStatemachine sm = new Statemachine("lightShow");

        final String cmd_red = "RED";
        final String cmd_yellow = "YELLOW";
        final String cmd_green = "GREEN";


        String player_1 = "Bobby";
        String player_2 = "Lea";

        final String other_player;

        //WHO AM I?
        if(getRobotID().equals(player_1)){
            other_player = player_2;
            final String myID = player_1;
        }else{
            other_player = player_1;
            final String myID = player_2;

        }
        final String player_dest = other_player;



        /** Waits for command **/
        IState state_idle = new State("Idle"){
            @Override
            public void run(){
                //brickController.setLEDOff();
            }
        };


        IConstraint send_cmd = new OR(new EQ(Color.BLACK,new Color(EV3PortIDs.PORT_1)),new EQ(Color.BLACK,new Color(EV3PortIDs.PORT_4)));
        ITransition trans_send_cmd = new Transition(send_cmd);


        IState state_sending_command = new State("SendingCommand"){
            @Override
            public void run(){
                int color;
                sendMessage(SERVER_LOG,"Calculating light.. ");
                do {
                    color = (int)Math.round(Math.random() *1000 * 3);
                    switch(color){
                        case 1: sendMessage(player_dest,cmd_red); sendMessage(SERVER_LOG,"sent Command: "+cmd_red); break;
                        case 2: sendMessage(player_dest,cmd_yellow); sendMessage(SERVER_LOG,"sent Command: "+cmd_yellow); break;
                        case 3: sendMessage(player_dest,cmd_green); sendMessage(SERVER_LOG,"sent Command: "+cmd_green); break;
                    }
                }while(!(color >= 1 && color <= 3));
                sendMessage(SERVER_LOG,"Command sent to "+other_player);
            }
        };

        IConstraint light_red = new MsgReceived(new MessageProperty(cmd_red,other_player));
        IConstraint light_yellow = new MsgReceived(new MessageProperty(cmd_yellow,other_player));
        IConstraint light_green = new MsgReceived(new MessageProperty(cmd_green,other_player));

        ITransition trans_light_red = new Transition(light_red);
        ITransition trans_light_yellow = new Transition(light_yellow);
        ITransition trans_light_green = new Transition(light_green);


        State state_red = new State("Show Red Light"){
            @Override
            public void run(){
                setLED(LED_RED_ON);
                sendLogMessage("Watch "+getRobotID()+"s Lightshow!");
            }
        };

        State state_yellow = new State("Show Yellow Light"){
            @Override
            public void run(){
                setLED(LED_YELLOW_ON);
                sendLogMessage("Watch "+getRobotID()+"s Lightshow!");
            }
        };

        State state_green = new State("Show Green Light"){
            @Override
            public void run(){
                setLED(LED_GREEN_ON);
                sendLogMessage("Watch "+getRobotID()+"s Lightshow!");
            }
        };

        Transition trans_end_Redlightshow = new Transition(new TimeExpired(new Seconds(4)));
        Transition trans_end_Yellowlightshow = new Transition(new TimeExpired(new Seconds(4)));
        Transition trans_end_Greenlightshow = new Transition(new TimeExpired(new Seconds(4)));
        Transition trans_cmd_sent = new Transition(new TimeExpired(new Seconds(2)));

        sm.addState(state_idle);
        sm.setStartState(state_idle);
        sm.addState(state_sending_command);
        sm.addState(state_red);
        sm.addState(state_yellow);
        sm.addState(state_green);

        sm.addTransition(trans_send_cmd,state_idle,state_sending_command);
        sm.addTransition(trans_cmd_sent,state_sending_command,state_idle);

        sm.addTransition(trans_light_red,state_idle,state_red);
        sm.addTransition(trans_light_yellow,state_idle,state_yellow);
        sm.addTransition(trans_light_green,state_idle,state_green);

        sm.addTransition(trans_end_Redlightshow,state_red,state_sending_command);
        sm.addTransition(trans_end_Yellowlightshow,state_yellow,state_sending_command);
        sm.addTransition(trans_end_Greenlightshow,state_green,state_sending_command);

        return sm;
    }

     public IStatemachine lightshowSmall() {
        IStatemachine sm = new Statemachine("lightshowSmall");

        final String cmd_red = "RED";
        final String cmd_yellow = "YELLOW";
        final String cmd_green = "GREEN";


        String player_1 = "Bobby";
        String player_2 = "Lea";

        final String other_player;

        //WHO AM I?
        if(getRobotID().equals(player_1)){
            other_player = player_2;
            final String myID = player_1;
        }else{
            other_player = player_1;
            final String myID = player_2;

        }
        final String player_src = other_player;
        final String player_dest = other_player;

        /** Waits for command to start with sending an command or wait**/
        IState state_idle = new State("Idle"){
            @Override
            public void run(){
                sendLogMessage("Who should sent the Command?");
            }
        };

        IConstraint send_cmd = new OR(new EQ(Color.BLACK,new Color(EV3PortIDs.PORT_1)),new EQ(Color.BLACK,new Color(EV3PortIDs.PORT_4)));

        Transition trans_send_first_cmd = new Transition(send_cmd){
            @Override
            public void run(){
                int color;
                System.out.println("Running transition --> 'trans_send_first_cmd' --> Calculating light..");
                sendLogMessage("Calculating light.. ");
                do {
                    color = (int)Math.round(Math.random() *1000 * 3);
                    switch(color){
                        case 1: sendMessage(player_dest,cmd_red); break;
                        case 2: sendMessage(player_dest,cmd_yellow); break;
                        case 3: sendMessage(player_dest,cmd_green); break;
                    }
                }while(!(color >= 1 && color <= 3));
                sendLogMessage("Command sent to "+other_player);
            }
        };

        IState state_wait_for_cmd = new State("waiting_for_cmd");

        IState state_lightshow = new State("lightshow");

        IConstraint light_red = new MsgReceived(new MessageProperty(cmd_red,other_player));
        IConstraint light_yellow = new MsgReceived(new MessageProperty(cmd_yellow,other_player));
        IConstraint light_green = new MsgReceived(new MessageProperty(cmd_green,other_player));

        Transition trans_red = new Transition(light_red){
            @Override
            public void run(){
                setLED(LED_RED_ON);
                sendLogMessage("Watch "+getRobotID()+"s Lightshow!");
            }
        };

        Transition trans_yellow = new Transition(light_yellow){
            @Override
            public void run(){
                setLED(LED_YELLOW_ON);
                sendLogMessage("Watch "+getRobotID()+"s Lightshow!");
            }
        };

        Transition trans_green = new Transition(light_green){
            @Override
            public void run(){
                setLED(LED_GREEN_ON);
                sendLogMessage("Watch "+getRobotID()+"s Lightshow!");
            }
        };

        IConstraint time_expired = new TimeExpired(new Seconds(4));
        Transition trans_end_lightshow = new Transition(time_expired){
            @Override
            public void run(){
                int color;
                do {
                    color = (int)Math.round(Math.random() *1000 * 3);
                    switch(color){
                        case 1: sendMessage(player_dest,cmd_red); break;
                        case 2: sendMessage(player_dest,cmd_yellow); break;
                        case 3: sendMessage(player_dest,cmd_green); break;
                    }
                }while(!(color >= 1 && color <= 3));
                sendLogMessage("Command sent to "+other_player);
            }
        };


        sm.addState(state_idle);
        sm.setStartState(state_idle);

        sm.addState(state_wait_for_cmd);
        sm.addState(state_lightshow);

        sm.addTransition(trans_send_first_cmd,state_idle,state_wait_for_cmd);
        sm.addTransition(trans_red,state_idle,state_lightshow);
        sm.addTransition(trans_yellow,state_idle,state_lightshow);
        sm.addTransition(trans_green,state_idle,state_lightshow);

        sm.addTransition(trans_end_lightshow,state_lightshow,state_wait_for_cmd);

        sm.addTransition(trans_red,state_wait_for_cmd,state_lightshow);
        sm.addTransition(trans_yellow,state_wait_for_cmd,state_lightshow);
        sm.addTransition(trans_green,state_wait_for_cmd,state_lightshow);

        return sm;
    }

    public IStatemachine wallPingPong() {
        IStatemachine sm = new Statemachine("SingleWallPingPongSM");

        IState state_forward = new State("Forward") {
            @Override
            public void run() {
                System.out.println(this.getName() + " isActive\n");
                getMotorProvider().getMotor(EV3PortIDs.PORT_A).setSpeed(500);
                getMotorProvider().getMotor(EV3PortIDs.PORT_D).setSpeed(500);
                //FORWARD
                SynchronizedMotorOperation forwardOp = SyncedMotorOpFactory.createForwardOperation();
                SynchronizedMotorOperation noOp = SyncedMotorOpFactory.createNoOperation();

                getMotorProvider().getSynchronizedMotors().executeSynchronizedOperation(forwardOp, noOp,noOp,forwardOp,false);

                setLED(LED_GREEN_ON);
            }
        };

        IState state_time_test = new State("NothingFound :(") {
            @Override
            public void run() {
                System.out.println(this.getName() + " isActive\n");
                //STOP
                SynchronizedMotorOperation stopOp = SyncedMotorOpFactory.createStopOperation();
                SynchronizedMotorOperation noOp = SyncedMotorOpFactory.createNoOperation();

                getMotorProvider().getSynchronizedMotors().executeSynchronizedOperation(stopOp, noOp,noOp,stopOp,false);

                setLEDOff();
            }
        };


        IState state_backward = new State("backward") {
            @Override
            public void run() {
                System.out.println(this.getName() + " isActive\n");
                //BACKWARD
                SynchronizedMotorOperation backwardOp = SyncedMotorOpFactory.createBackwardOperation();
                SynchronizedMotorOperation noOp = SyncedMotorOpFactory.createNoOperation();

                getMotorProvider().getSynchronizedMotors().executeSynchronizedOperation(backwardOp, noOp,noOp,backwardOp,false);

                setLED(LED_RED_BLINKING);

                //getMotorProvider().getMotor(EV3PortIDs.PORT_A).setSpeed(500);
                //getMotorProvider().getMotor(EV3PortIDs.PORT_D).setSpeed(500);
            }
        };

        IState state_turn = new State("turn") {
            @Override
            public void run() {
                System.out.println(this.getName() + " isActive\n");

                //TURN LEFT
                SynchronizedMotorOperation backwardOp = SyncedMotorOpFactory.createBackwardOperation();
                SynchronizedMotorOperation forwardOp = SyncedMotorOpFactory.createForwardOperation();
                SynchronizedMotorOperation noOp = SyncedMotorOpFactory.createNoOperation();

                getMotorProvider().getSynchronizedMotors().executeSynchronizedOperation(backwardOp, noOp,noOp,forwardOp,false);

                setLED(LED_YELLOW_BLINKING);

                //getMotorProvider().getMotor(EV3PortIDs.PORT_A).setSpeed(500);
                //getMotorProvider().getMotor(EV3PortIDs.PORT_D).setSpeed(500);
            }
        };

        //Set start states ------
        sm.addState(state_forward);
        sm.setStartState(state_forward);

        //Add States ------
        sm.addState(state_time_test);
        sm.addState(state_backward);
        sm.addState(state_turn);

        IConstraint distance_collision = new LT(0.10f, new Distance(EV3PortIDs.PORT_2));
        IConstraint time_driving_backward = new TimeExpired(new Milliseconds(1200));

        IConstraint time_180turn = new TimeExpired(new Milliseconds(1300));

        IConstraint time_stop = new TimeExpired(new Seconds(25));


        //--- Transitionen
        ITransition collision = new Transition(distance_collision);
        ITransition drive_backwards = new Transition(time_driving_backward);
        ITransition done_turn_180 = new Transition(time_180turn);
        ITransition stop = new Transition(time_stop);


        sm.addTransition(collision,state_forward,state_backward);
        sm.addTransition(drive_backwards, state_backward, state_turn);
        sm.addTransition(done_turn_180, state_turn, state_forward);
        sm.addTransition(stop,state_forward,state_time_test);

        return sm;
    }

    public IStatemachine testTransitionCopy() {
        IStatemachine sm = new Statemachine("testTransitionCopy");
        IState state_mathRandom = new State("Random Number State"){
            @Override
            public void run(){
                setLED(LED_YELLOW_BLINKING);
                int number;
                do {
                    number = (int)Math.round(Math.random() * 3);
                    sendLogMessage("Calculated Number"+number);
                }while(!(number >= 1 && number <= 3));
                sendMessage(getRobotID(),""+number);
                sendLogMessage(""+number+" - "+getRobotID());
            }
        };

        IState state_nr1 = new State("State NR1"){
            @Override
            public void run(){
                setLED(LED_RED_ON);
            }
        };

        IState state_nr2 = new State("State NR2"){
            @Override
            public void run(){
                setLED(LED_YELLOW_ON);
            }
        };

        IState state_nr3 = new State("State NR3"){
            @Override
            public void run(){
                setLED(LED_GREEN_ON);
            }
        };

        IConstraint constr_nr1 = new MsgReceived(new MessageProperty("1",getRobotID()));
        IConstraint constr_nr2 = new MsgReceived(new MessageProperty("2",getRobotID()));
        IConstraint constr_nr3 = new MsgReceived(new MessageProperty("3",getRobotID()));
        IConstraint constr_timeout = new TimeExpired(new Seconds(3));

        Transition trans_nr1 = new Transition(constr_nr1);
        Transition trans_nr2 = new Transition(constr_nr2);
        Transition trans_nr3 = new Transition(constr_nr3);
        Transition trans_timeout = new Transition(constr_timeout);
        Transition trans_timeout2 = new Transition(constr_timeout);


        sm.addState(state_mathRandom);
        sm.setStartState(state_mathRandom);
        sm.addState(state_nr1);
        sm.addState(state_nr2);
        sm.addState(state_nr3);

        sm.addTransition(trans_nr1,state_mathRandom,state_nr1);
        sm.addTransition(trans_nr2,state_mathRandom,state_nr2);
        sm.addTransition(trans_nr3,state_mathRandom,state_nr3);

        sm.addTransition(trans_timeout,state_nr1,state_mathRandom);
        sm.addTransition(trans_timeout,state_nr2,state_mathRandom);
        sm.addTransition(trans_timeout2,state_nr3,state_mathRandom);

        return sm;
    }

}
