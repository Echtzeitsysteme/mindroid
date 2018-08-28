package org.mindroid.android.app.programs.workshop.solutions;

import org.mindroid.api.ImperativeWorkshopAPI;
import org.mindroid.impl.brick.Textsize;

public class HelloWorldPingB extends ImperativeWorkshopAPI {

    public HelloWorldPingB() {
        super("Hello World Ping Bob [sol]");
    }

    @Override
    public void run() {
        clearDisplay();
        while(!isInterrupted()){
            if (hasMessage()){
                String msg = getNextMessage().getContent();
                if (msg.equals("Hallo Bob!")){
                    drawString("Nachricht von Alice erhalten", Textsize.MEDIUM, 1, 60);
                }
            }
            delay(100);
        };
    }
  }
  
