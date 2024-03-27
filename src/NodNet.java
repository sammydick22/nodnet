package src;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Robot;
import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.awt.event.KeyEvent;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class NodNet implements NativeKeyListener{
    private static int screenWidth;
    private static int screenHeight;
    private static double pitchScaler;
    private static double sensitivity;
    private static GUI gui;
    private static double acceleration;
    private static String ipv4;
    private static int port;
    private static boolean altDown = false;
    private static boolean xDown = false;
    private static boolean cDown = false;
    private static boolean zDown = false;
    private static boolean isLeftPressing = false;
    private static boolean isRightPressing = false;
    private static boolean locked = false;

    public static void main(String[] args) {
        try {
            initializeScreenDimensions();
            gui = new GUI();
            System.out.println(sensitivity);
            System.err.println(acceleration);
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(new NodNet());
            java.util.logging.Logger logger = java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName());
            logger.setLevel(java.util.logging.Level.OFF);
            new Thread(() -> {
                try {
                    Robot robot = new Robot();
                    while (true) {
                        if(!locked){
                            if (altDown && cDown) {
                                if(!isRightPressing){
                                    isRightPressing = true;
                                    robot.mousePress(KeyEvent.BUTTON3_DOWN_MASK);
                                }
                            }
                            else{
                                if(isRightPressing){
                                    isRightPressing = false;
                                    robot.mouseRelease(KeyEvent.BUTTON3_DOWN_MASK);
                                }
                                if (altDown && xDown) {
                                    if(!isLeftPressing){
                                        isLeftPressing = true;
                                        robot.mousePress(KeyEvent.BUTTON1_DOWN_MASK);
                                    }
                                }
                                else{
                                    if(isLeftPressing){
                                        isLeftPressing = false;
                                        robot.mouseRelease(KeyEvent.BUTTON1_DOWN_MASK);
                                    }
                                }
                            }
                        }
                        robot.delay(23); // Delay to avoid high CPU usage - standard mouse input
                    }
                } catch (AWTException e) {
                    e.printStackTrace();
                }
            }).start();

            while(true){
                System.out.println(locked);
                if(gui.getStarted() && !locked){
                    sensitivity = gui.getSens()/10.0;
                    acceleration = 1 + gui.getAccel()/10.0;
                    try(final DatagramSocket socket = new DatagramSocket()){
                        socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                        ipv4 = socket.getLocalAddress().getHostAddress();
            
                    } catch (UnknownHostException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    port = gui.getPort();
                    receiveFreelookData(ipv4, port); // IP and port can be adjusted
                }
            } 
        } catch (NativeHookException | SocketException e) {
            e.printStackTrace();
        }
    }

    private static void initializeScreenDimensions() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenWidth = screenSize.width;
        screenHeight = screenSize.height;
        pitchScaler = screenWidth/screenHeight;
    }

    private static void receiveFreelookData(String ip, int port) {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("Listening for freelook data on IP " + ip + " : " + port);
            byte[] buffer = new byte[48]; // Buffer for incoming data

            while (gui.getStarted()) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                long timeReceived = System.currentTimeMillis();
                socket.receive(packet);
                processFreelookData(packet.getData(), timeReceived);
            }
        } catch (SocketException e) {
            System.err.println("Socket exception: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void processFreelookData(byte[] data, long timeReceived) {
        if (data.length != 48) {
            System.out.println("Received data of unexpected length.");
            return;
        }

        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        double yaw = buffer.getDouble(3 * Double.BYTES);
        double pitch = -buffer.getDouble(4 * Double.BYTES); // Inverting pitch
        
        
        int negOrNotY = 1;
        int negOrNotP = 1;
        if (yaw < 0) {
            negOrNotY = -1;
        }
        if (pitch < 0) {
            negOrNotP = -1;
        }
        
        moveMouse(negOrNotY*Math.abs(Math.pow(yaw*sensitivity, acceleration))/acceleration, negOrNotP*Math.abs(Math.pow(pitch*sensitivity, acceleration))*pitchScaler/acceleration);
        long timeBeforeMove = System.currentTimeMillis();
        long delay = timeBeforeMove - timeReceived;
        System.out.println(yaw + " " + pitch + " " + "delay: " + delay + "ms" + "locked: "+locked);
    }

    private static void moveMouse(double yaw, double pitch) {
        try {
            if(locked) return;
            Robot robot = new Robot();
            Point mousePoint = MouseInfo.getPointerInfo().getLocation();
            int newX = (int) (mousePoint.x + yaw);
            int newY = (int) (mousePoint.y + pitch);

            robot.mouseMove(newX, newY);
        } catch (AWTException e) {
            System.err.println("AWTException: " + e.getMessage());
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_ALT){
            if (zDown) locked = !locked;
            altDown = true;
        }
        else if (e.getKeyCode() == NativeKeyEvent.VC_X) xDown = true;
        else if (e.getKeyCode() == NativeKeyEvent.VC_C) cDown = true;
        else if (e.getKeyCode() == NativeKeyEvent.VC_Z){
            if (altDown) locked = !locked;
            zDown = true;
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_ALT) altDown = false;
        else if (e.getKeyCode() == NativeKeyEvent.VC_X) xDown = false;
        else if (e.getKeyCode() == NativeKeyEvent.VC_C) cDown = false;
        else if (e.getKeyCode() == NativeKeyEvent.VC_Z) zDown = false;
    }
}
