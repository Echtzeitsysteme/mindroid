package org.mindroid.android.app.programs.workshop.solutions;

import org.mindroid.api.ImperativeWorkshopAPI;

public class ImpSingleWallPingPong extends ImperativeWorkshopAPI {


    public ImpSingleWallPingPong() {
        super("Single Wall-PingPong");
    }

    @Override
    public void run() {
        do {
            forward(500);

            while (getDistance() > 0.15f && !isInterrupted()) {
                delay(25);
            }
            stop();

            driveDistanceBackward(10);

            turnLeft(180);

        }while(!isInterrupted());
    }
}