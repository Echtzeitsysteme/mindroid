package org.mindroid.api.statemachine.constraints;

import org.mindroid.api.robot.context.IRobotContextState;
import org.mindroid.api.statemachine.properties.IProperty;

/**
 * Created by torben on 16.03.2017.
 */
public abstract class AbstractTimeComparator extends AbstractComparator {
    public AbstractTimeComparator(IProperty property) {
        super(property);
    }


    public abstract boolean evaluate(IRobotContextState context);

    //TODO
}
