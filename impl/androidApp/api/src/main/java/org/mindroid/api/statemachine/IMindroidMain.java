package org.mindroid.api.statemachine;

import org.mindroid.api.statemachine.exception.StateAlreadyExistsException;
import org.mindroid.impl.statemachine.StatemachineCollection;

/**
 * Created by Torben on 01.03.2017.
 */

public interface IMindroidMain {
    StatemachineCollection getStatemachineCollection();

}
