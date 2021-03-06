package mindroid.common.ev3.app;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.Font;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.lcd.LCD;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * Provides access to the EV3 display
 *
 * @author Torben Unzicker - Initial implementation
 * @author Roland Kluge - + IP address
 */
public class Display {

    public static void showSystemIsReady(){
        LCD.clearDisplay();
        drawBatteryState(5,5);
        configureFont(Font.getLargeFont());
        drawString("READY", 40, 50, GraphicsLCD.TOP);
        configureFont(Font.getSmallFont());
        drawString("Waiting for connection.", 25, 90, GraphicsLCD.TOP);
        drawString("IP: " + getIPAddress(), 25, 105, GraphicsLCD.TOP);
    }

    public static void showSystemIsReadyAndConnected(){
        LCD.clearDisplay();
        drawBatteryState(5,5);
        configureFont(Font.getLargeFont());
        drawString("READY", 40, 50, GraphicsLCD.TOP);
        configureFont(Font.getSmallFont());
        drawString("Connection established!", 25, 90, GraphicsLCD.TOP);
        drawString("IP: " + getIPAddress(), 25, 105, GraphicsLCD.TOP);
    }

    public static void showPleaseWait(){
        LCD.clearDisplay();
        drawBatteryState(5,5);
        configureFont(Font.getDefaultFont());
        drawString("PLEASE WAIT", 30, 50, GraphicsLCD.TOP);
        configureFont(Font.getSmallFont());
        drawString("IP: " + getIPAddress(), 25, 105, GraphicsLCD.TOP);
    }

    /**
     * Draws the Battery state on the screen.
     *
     * @param x coordinate relative to the top left corner of the display (GraphicsLCD.TOP)
     * @param y coordinate relative to the top left corner of the display (GraphicsLCD.TOP)
     */
    public static void drawBatteryState(int x, int y){
        configureFont(Font.getSmallFont());
        drawString("Battery: "+Brick.getBatteryStatus()+"%",x,y,GraphicsLCD.TOP);
    }

    /**
     * Draws the given text at the position (x,y) relative to the given anchor
     * @param text the text
     * @param x x coordinate (px)
     * @param y y coordinate (px)
     * @param anchor anchor (e.g. {@link GraphicsLCD#TOP}
     */
    public static void drawString(final String text, final int x, final int y, int anchor) {
        LocalEV3.get().getGraphicsLCD().drawString(text, x, y, anchor);
    }

    /**
     * Configures the display to use the given font for the subsequent calls to 'draw...' methods
     * @param font the font to use
     */
    public static void configureFont(final Font font) {
        LocalEV3.get().getGraphicsLCD().setFont(font);
    }

    /**
     * Returns the current IP address of the EV3 brick or "unknown" if the IP address cannot be determined
     * @return the IP address
     */
    private static String getIPAddress() {
        try {
            return getLocalHostLANAddress().getHostAddress();
        } catch (final UnknownHostException e) {
            return "unknown";
        }
    }

    /**
     * Returns an <code>InetAddress</code> object encapsulating what is most likely the machine's LAN IP address.
     * <p/>
     * This method is intended for use as a replacement of JDK method <code>InetAddress.getLocalHost</code>, because
     * that method is ambiguous on Linux systems. Linux systems enumerate the loopback network interface the same
     * way as regular LAN network interfaces, but the JDK <code>InetAddress.getLocalHost</code> method does not
     * specify the algorithm used to select the address returned under such circumstances, and will often return the
     * loopback address, which is not valid for network communication. Details
     * <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4665037">here</a>.
     * <p/>
     * This method will scan all IP addresses on all network interfaces on the host machine to determine the IP address
     * most likely to be the machine's LAN address. If the machine has multiple IP addresses, this method will prefer
     * a site-local IP address (e.g. 192.168.x.x or 10.10.x.x, usually IPv4) if the machine has one (and will return the
     * first site-local address if the machine has more than one), but if the machine does not hold a site-local
     * address, this method will return simply the first non-loopback address found (IPv4 or IPv6).
     * <p/>
     * If this method cannot find a non-loopback address using this selection algorithm, it will fall back to
     * calling and returning the result of JDK method <code>InetAddress.getLocalHost</code>.
     * <p/>
     *
     * @throws UnknownHostException If the LAN address of the machine cannot be found.
     *
     * Javadoc and impl. taken from https://stackoverflow.com/a/20418809
     * Original source: https://issues.apache.org/jira/browse/JCS-40
     */
    private static InetAddress getLocalHostLANAddress() throws UnknownHostException {
        try {
            InetAddress candidateAddress = null;
            // Iterate all NICs (network interface cards)...
            for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
                NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                // Iterate all IP addresses assigned to each card...
                for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
                    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {

                        if (inetAddr.isSiteLocalAddress()) {
                            // Found non-loopback site-local address. Return it immediately...
                            return inetAddr;
                        }
                        else if (candidateAddress == null) {
                            // Found non-loopback address, but not necessarily site-local.
                            // Store it as a candidate to be returned if site-local address is not subsequently found...
                            candidateAddress = inetAddr;
                            // Note that we don't repeatedly assign non-loopback non-site-local addresses as candidates,
                            // only the first. For subsequent iterations, candidate will be non-null.
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                // We did not find a site-local address, but we found some other non-loopback address.
                // Server might have a non-site-local address assigned to its NIC (or it might be running
                // IPv6 which deprecates the "site-local" concept).
                // Return this non-loopback candidate address...
                return candidateAddress;
            }
            // At this point, we did not find a non-loopback address.
            // Fall back to returning whatever InetAddress.getLocalHost() returns...
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            if (jdkSuppliedAddress == null) {
                throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
            }
            return jdkSuppliedAddress;
        }
        catch (Exception e) {
            UnknownHostException unknownHostException = new UnknownHostException("Failed to determine LAN address: " + e);
            unknownHostException.initCause(e);
            throw unknownHostException;
        }
    }

}
