import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GUI implements ActionListener {

    private boolean started = false;
    private JFrame frame = new JFrame();
    private JPanel panel = new JPanel();
    private JButton startStopButton = new JButton("Start");
    private JButton resetButton = new JButton("Restore Defaults");
    private JLabel statusLabel = new JLabel("Status: Stopped");

    // Labels and TextFields for inputs
    private JLabel sensLabel = new JLabel("Sensitivity:");
    private JTextField sensBox = new JTextField("3", 2);
    private JLabel threshLabel = new JLabel("Threshold:");
    private JTextField threshBox = new JTextField("50", 2);
    private JLabel accelLabel = new JLabel("Acceleration:");
    private JTextField accelBox = new JTextField("10", 2);
    private JLabel ipLabel = new JLabel("IP Address:");
    private JTextField ipBox = new JTextField(10);
    private JLabel portLabel = new JLabel("Port:");
    private JTextField portBox = new JTextField("4242", 6);

    // Default values
    private final String DEFAULT_SENS = "3";
    private final String DEFAULT_THRESH = "50";
    private final String DEFAULT_ACCEL = "10";
    private final String DEFAULT_IP = ""; // Assuming empty as default
    private final String DEFAULT_PORT = "4242";

    public String sens = "3";
    public String thresh = "50";
    public String accel = "10";
    public String ip = ""; // Assuming empty as default
    public String port = "4242";

    public GUI() {
        startStopButton.addActionListener(this);
        resetButton.addActionListener(this);

        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        panel.add(startStopButton, gbc);
        panel.add(statusLabel, gbc);
        addLabeledField(sensLabel, sensBox, panel, gbc);
        addLabeledField(threshLabel, threshBox, panel, gbc);
        addLabeledField(accelLabel, accelBox, panel, gbc);
        addLabeledField(ipLabel, ipBox, panel, gbc);
        addLabeledField(portLabel, portBox, panel, gbc);
        panel.add(resetButton, gbc);

        frame.add(panel, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("GUI");
        frame.pack();
        frame.setVisible(true);

        
    }

    public int getSens() {
        return Integer.parseInt(sensBox.getText());
    }
    public int getThresh() {
        return Integer.parseInt(threshBox.getText());
    }
    public int getAccel() {
        return Integer.parseInt(accelBox.getText());
    }
    public String getIP() {
        return ipBox.getText();
    }
    public int getPort() {
        return Integer.parseInt(portBox.getText());
    }
    public boolean getStarted() {
        return started;
    }

    private void addLabeledField(JLabel label, JTextField field, JPanel panel, GridBagConstraints gbc) {
        panel.add(label, gbc);
        panel.add(field, gbc);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == startStopButton) {
            started = !started;
            startStopButton.setText(started ? "Stop" : "Start");
            statusLabel.setText("Status: " + (started ? "Started" : "Stopped"));
        } else if (e.getSource() == resetButton) {
            // Resetting values to default
            sensBox.setText(DEFAULT_SENS);
            threshBox.setText(DEFAULT_THRESH);
            accelBox.setText(DEFAULT_ACCEL);
            ipBox.setText(DEFAULT_IP);
            portBox.setText(DEFAULT_PORT);
        }
    }

    public static void main(String[] args) {
        new GUI();
    }
}