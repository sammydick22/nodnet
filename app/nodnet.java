// FreelookTest.java

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.*;
import java.awt.Robot;
import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.lang.*;



public class nodnet {

        // return data;
    private static int screenWidth;
    private static int screenHeight;

    private static double pitchScaler;

    private static double sensitivity;
    private static GUI gui = new GUI();
    private static int threshold; 
    private static int pitchThreshold;
    private static double acceleration;
    private static String ipv4;
    private static int port;





    public static void main(String[] args) {
        initializeScreenDimensions();

        sensitivity = gui.getSens()/10.0;
        threshold = gui.getThresh();
        acceleration = 1 + gui.getAccel()/10.0;
        ipv4 = gui.getIP();
        port = gui.getPort();
        pitchThreshold = (int) (threshold/pitchScaler);
        
        while(true){
            System.out.println("off");
            if(gui.getStarted())
                receiveFreelookData(ipv4, port); // IP and port can be adjusted

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

        // Apply thresholds and scalers
        yaw = applyThreshold(yaw, threshold);
        pitch = applyThreshold(pitch, pitchThreshold);
        
        
        
        int negOrNotY = 1;
        int negOrNotP = 1;
        if (yaw < 0) {
            negOrNotY = -1;
        }
        if (pitch < 0) {
            negOrNotP = -1;
        }
        
        moveMouse(negOrNotY*Math.pow(yaw*sensitivity, acceleration), negOrNotP*Math.pow(pitch*sensitivity, acceleration)*pitchScaler);
        long timeBeforeMove = System.currentTimeMillis();
        long delay = timeBeforeMove - timeReceived;
        System.out.println(yaw + " " + pitch + " " + "delay: " + delay + "ms");
    }

    private static double applyThreshold(double value, double threshold) {
        if (value < -threshold) {
            return -threshold;
        }
        if (value > threshold) {
            return threshold;
        }
        return value;
    }

    private static void moveMouse(double yaw, double pitch) {
        try {
            Robot robot = new Robot();
            Point mousePoint = MouseInfo.getPointerInfo().getLocation();
            int newX = (int) (mousePoint.x + yaw);
            int newY = (int) (mousePoint.y + pitch);

            robot.mouseMove(newX, newY);
        } catch (AWTException e) {
            System.err.println("AWTException: " + e.getMessage());
        }
    }
}