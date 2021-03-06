package org.mindroid.impl.test;

import org.mindroid.api.*;
import org.mindroid.api.ev3.EV3StatusLightColor;
import org.mindroid.api.ev3.EV3StatusLightInterval;
import org.mindroid.api.robot.control.IRobotCommandCenter;
import org.mindroid.api.statemachine.IState;
import org.mindroid.api.statemachine.IStatemachine;
import org.mindroid.api.statemachine.ITransition;
import org.mindroid.api.statemachine.constraints.IConstraint;
import org.mindroid.api.statemachine.exception.StateAlreadyExistsException;
import org.mindroid.common.messages.NetworkPortConfig;
import org.mindroid.common.messages.motor.synchronization.SynchronizedMotorOperation;
import org.mindroid.common.messages.motor.synchronization.SyncedMotorOpFactory;
import org.mindroid.impl.ev3.EV3PortIDs;
import org.mindroid.impl.exceptions.BrickIsNotReadyException;
import org.mindroid.impl.exceptions.PortIsAlreadyInUseException;
import org.mindroid.impl.robot.RobotFactory;
import org.mindroid.impl.statemachine.State;
import org.mindroid.impl.statemachine.Statemachine;
import org.mindroid.impl.statemachine.StatemachineCollection;
import org.mindroid.impl.statemachine.Transition;
import org.mindroid.impl.statemachine.constraints.*;
import org.mindroid.impl.statemachine.properties.Milliseconds;
import org.mindroid.impl.statemachine.properties.Seconds;
import org.mindroid.impl.statemachine.properties.sensorproperties.Distance;

import java.io.IOException;

import static org.mindroid.common.messages.NetworkPortConfig.SERVER_PORT;

public class TestPCClient {

    public static String brickIP = "10.0.1.1";
    public static String msgServerIP = "127.0.0.1";
    public static String robotID = "Testrobot 1";

    private static final String impID = "TestImperativeImpl";

    public static IStatemachine sm;

    public static void main(String args[]){
        new TestPCClientRobot();
    }


    private static class TestPCClientRobot extends StatemachineAPI implements IImplStateListener {
        IRobotCommandCenter commandCenter;

        public TestPCClientRobot() {
            try {
                System.out.println("[TestRobot:PC-Client] making robot");
                initRobot();
                System.out.println("[TestRobot:PC-Client] Connecting to brick");
                commandCenter.connectToBrick();

                while (!commandCenter.isConnectedToBrick()) {
                    Thread.sleep(100);
                    System.out.println("[TestRobot:PC-Client] connecting..");
                }

                System.out.println("[TestRobot:PC-Client] Initializing configuration");

                commandCenter.initializeConfiguration();
                Thread.sleep(10000);
                System.out.println("[TestRobot:PC-Client] initialized!");

                commandCenter.startImplementation(impID,this);

                Thread.sleep(20000);

                commandCenter.stopImplementation();

            } catch (StateAlreadyExistsException stateAlreadyExists) {
                stateAlreadyExists.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (PortIsAlreadyInUseException e) {
                e.printStackTrace();
            } catch (BrickIsNotReadyException e) {
                e.printStackTrace();
            }
        }

        private void initRobot() throws StateAlreadyExistsException {
            TestPCClient.sm = syncdMotorTest();
            StatemachineCollection statemachineCollection = new StatemachineCollection();
            statemachineCollection.addStatemachine(sm.getID(), sm);

            RobotFactory roFactory = new RobotFactory();
            System.out.println("## App.Robot.makeRobot() got called ");
            //Config
            roFactory.setRobotConfig(new RobotTestConfig());
            roFactory.setBrickIP(brickIP);
            roFactory.setBrickTCPPort(NetworkPortConfig.BRICK_PORT);
            roFactory.setRobotID(robotID);

            //Create Robot
            commandCenter = roFactory.createRobot(false);

            commandCenter.addImplementation(new TestImperativeImpl());

            //commandCenter.addImplementation(statemachineCollection); Needs to be an StatemachineAPI containing the Collection

            //connnect messenger
            commandCenter.connectMessenger(msgServerIP,SERVER_PORT);
        }


        public IStatemachine syncdMotorTest(){
            IStatemachine sm = new Statemachine("SyncedMotorsTest");

            IState state_forward = new State("Forward") {
                @Override
                public void run() {
                    System.out.println(this.getName() + " isActive\n");

                    SynchronizedMotorOperation rotate = SyncedMotorOpFactory.createRotateOperation(720);
                    SynchronizedMotorOperation forward = SyncedMotorOpFactory.createForwardOperation();
                    SynchronizedMotorOperation noOperation = SyncedMotorOpFactory.createNoOperation();

                    //FORWARD
                    getMotorProvider().getSynchronizedMotors().executeSynchronizedOperation(forward,noOperation,noOperation,forward,false);

                    setLED(EV3StatusLightColor.GREEN, EV3StatusLightInterval.ON);
                }
            };
            sm.addState(state_forward);
            sm.setStartState(state_forward);

            return sm;
        }

        public IStatemachine wallPingPong() {
            IStatemachine sm = new Statemachine("SingleWallPingPong");

            IState state_forward = new State("Forward") {
                @Override
                public void run() {
                    System.out.println(this.getName() + " isActive\n");
                    //FORWARD
                    getMotorProvider().getMotor(EV3PortIDs.PORT_A).forward();
                    getMotorProvider().getMotor(EV3PortIDs.PORT_D).forward();

                    setLED(EV3StatusLightColor.GREEN, EV3StatusLightInterval.ON);


                    getMotorProvider().getMotor(EV3PortIDs.PORT_A).setSpeed( 50);
                    getMotorProvider().getMotor(EV3PortIDs.PORT_D).setSpeed(  50);
                }
            };

            IState state_time_test = new State("NothingFound :(") {
                @Override
                public void run() {
                    System.out.println(this.getName() + " isActive\n");
                    //FORWARD
                    getMotorProvider().getMotor(EV3PortIDs.PORT_A).stop();
                    getMotorProvider().getMotor(EV3PortIDs.PORT_D).stop();

                    setLEDOff();
                }
            };


            IState state_backward = new State("backward") {
                @Override
                public void run() {
                    System.out.println(this.getName() + " isActive\n");
                    //BACKWARD
                    getMotorProvider().getMotor(EV3PortIDs.PORT_A).backward();
                    getMotorProvider().getMotor(EV3PortIDs.PORT_D).backward();

                    setLED(EV3StatusLightColor.RED, EV3StatusLightInterval.BLINKING);

                    getMotorProvider().getMotor(EV3PortIDs.PORT_A).setSpeed(50);
                    getMotorProvider().getMotor(EV3PortIDs.PORT_D).setSpeed(50);
                }
            };

            IState state_turn = new State("turn") {
                @Override
                public void run() {
                    System.out.println(this.getName() + " isActive\n");
                    //TURN LEFT
                    getMotorProvider().getMotor(EV3PortIDs.PORT_A).backward();
                    getMotorProvider().getMotor(EV3PortIDs.PORT_D).forward();

                    setLED(EV3StatusLightColor.YELLOW, EV3StatusLightInterval.BLINKING);

                    getMotorProvider().getMotor(EV3PortIDs.PORT_A).setSpeed(50);
                    getMotorProvider().getMotor(EV3PortIDs.PORT_D).setSpeed(50);
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


            sm.addTransition(collision, state_forward, state_backward);
            sm.addTransition(drive_backwards, state_backward, state_turn);
            sm.addTransition(done_turn_180, state_turn, state_forward);
            sm.addTransition(stop, state_forward, state_time_test);

            return sm;
        }


        @Override
        public void handleIsRunning(boolean isRunning) {
            //NOTING
        }
    }

    private static class TestImperativeImpl extends ImperativeWorkshopAPI{

        public TestImperativeImpl() {
            super(impID);
        }

        @Override
        public void run() {
            delay(500);
            turnRight(90);
        }

    }
}