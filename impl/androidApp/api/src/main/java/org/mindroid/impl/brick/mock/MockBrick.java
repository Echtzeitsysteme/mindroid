package org.mindroid.impl.brick.mock;

import org.mindroid.api.ev3.EV3StatusLightColor;
import org.mindroid.api.ev3.EV3StatusLightInterval;
import org.mindroid.api.robot.control.IBrickControl;
import org.mindroid.impl.brick.ButtonProvider;
import org.mindroid.impl.brick.Textsize;

public class MockBrick implements IBrickControl{


    /**
     * Creates a Brick Class.
     *
     */
    public MockBrick() {

    }

    @Override
    public void setEV3StatusLight(EV3StatusLightColor color, EV3StatusLightInterval interval) {

    }

    @Override
    public void setLEDOff() {

    }

    @Override
    public void setVolume(int volume) {

    }

    @Override
    public void singleBeep() {

    }

    @Override
    public void doubleBeep() {

    }

    @Override
    public void buzz() {

    }

    @Override
    public void beepSequenceDown() {

    }

    @Override
    public void beepSequenceUp() {

    }

    @Override
    public ButtonProvider getButtonProvider() {
        return null;
    }

    @Override
    public void clearDisplay() {

    }

    @Override
    public void drawString(String str, Textsize textsize, int posX, int posY) {

    }

    @Override
    public void drawImage(String str) {

    }

    public boolean isConnected(){
        //always "connected" as it is used as a mock class
        return true;
    }

}