package org.mindroid.api;

import org.mindroid.api.statemachine.IMindroidMain;
import org.mindroid.impl.ev3.EV3PortIDs;
import org.mindroid.impl.statemachine.StatemachineCollection;

/**
 * Created by Torben on 03.05.2017.
 *
 * Can contain multiple Parallel or single running Statemachines in its StatemachineCollection.
 * Parallel groups or single Statemachines can be identified using their GroupID.
 */
public class StatemachineAPI extends BasicAPI implements IMindroidMain {

/*
 * TODO@revise: Required improvements
 *   Could we create a new subtype of "IMotor"?
 * * Consistency: There is forward(); delay(...); but turnRight() and turnRight(time);
 * * There are numerous comments that suggest that this class is not ready, yet.
 * * A lot of code duplication regarding "missing state machine creation"
 * * Revise all "System.err.println"s -> often, a RuntimeException would be better to signal an error!
 * * Check for unused classes
 * * Update docu to EN
 * * Javadoc should generate without warning (in all projects!)
 * * Avoid varargs and arrays in public API, rather use List<>, Collection<> or Iterable<>
 */

    public final StatemachineCollection statemachineCollection = new StatemachineCollection();

    @Override
    protected void accept(AbstractImplVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public final StatemachineCollection getStatemachineCollection() {
        return  statemachineCollection;
    }

    /**
     * Forwards motors at ports A and D.
     * Speed is set to 50.
     */
    public void forward() {
        getMotorProvider().getMotor(EV3PortIDs.PORT_A).forward();
        getMotorProvider().getMotor(EV3PortIDs.PORT_D).forward();
        getMotorProvider().getMotor(EV3PortIDs.PORT_A).setSpeed(50);
        getMotorProvider().getMotor(EV3PortIDs.PORT_D).setSpeed(50);
    }

    /**
     * Backwards motors at ports A and D
     * Speed is set to 50.
     */
    public void backward() {
        getMotorProvider().getMotor(EV3PortIDs.PORT_A).backward();
        getMotorProvider().getMotor(EV3PortIDs.PORT_D).backward();
        getMotorProvider().getMotor(EV3PortIDs.PORT_A).setSpeed(50);
        getMotorProvider().getMotor(EV3PortIDs.PORT_D).setSpeed(50);
    }

}
