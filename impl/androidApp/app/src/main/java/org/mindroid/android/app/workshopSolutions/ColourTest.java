package org.mindroid.android.app.workshopSolutions;

import org.mindroid.api.ImperativeWorkshopAPI;
import org.mindroid.impl.brick.Textsize;
import org.mindroid.impl.statemachine.properties.Colors;

public class ColourTest extends ImperativeWorkshopAPI {

    public ColourTest() {
        super("Colour Test");
    }

     @Override
    public void run() {       
        while (!isInterrupted()) { 
            Colors leftColorValue = getLeftColor();
            Colors rightColorValue = getRightColor();
            clearDisplay(); 
            drawString("Colors", Textsize.MEDIUM, 1, 1);
            drawString("L: " + describeColor(leftColorValue), Textsize.MEDIUM, 1, 17);
            drawString("R: " + describeColor(rightColorValue), Textsize.MEDIUM, 1, 33);
            delay(500);
        }
    }

    private static String describeColor(final Colors colorValue) {
        if (colorValue == Colors.NONE)   return "None";
        if (colorValue == Colors.BLACK)  return "Black";
        if (colorValue == Colors.BLUE)   return "Blue";
        if (colorValue == Colors.GREEN)  return "Green";
        if (colorValue == Colors.YELLOW) return "Yellow";
        if (colorValue == Colors.RED)    return "Red";
        if (colorValue == Colors.WHITE)  return "White";
        if (colorValue == Colors.BROWN)  return "Brown";
        return "unknown";
    }
}