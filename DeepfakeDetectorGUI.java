import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class DeepfakeDetectorGUI extends JFrame {
    private JLabel label;
    private JButton uploadButton;
    private JLabel resultLabel;
    private File selectedFile;

    public DeepfakeDetectorGUI() {
        setTitle("Deepfake Detector");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        uploadButton = new JButton("Upload Image");
        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    selectedFile = fileChooser.getSelectedFile();
                    label.setText("File: " + selectedFile.getName());
                    processImage(selectedFile);
                }
            }
        });

        // Label to display result
        label = new JLabel("No image selected");
        resultLabel = new JLabel("Result: Pending");

        // Add components to GUI
        add(uploadButton);
        add(label);
        add(resultLabel);

        setVisible(true);
    }

    private void processImage(File image) {
        try {
            // Run Python script with the image path as an argument
            ProcessBuilder pb = new ProcessBuilder("python", "run.py", image.getAbsolutePath());
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Read output from Python script
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String result = reader.readLine(); // Read first line of output
            
            // Update the GUI label with the result
            if (result != null) {
                resultLabel.setText("Result: " + result);
            } else {
                resultLabel.setText("Result: Error in processing");
            }

            process.waitFor();
        } catch (Exception ex) {
            resultLabel.setText("Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DeepfakeDetectorGUI());
    }
}
