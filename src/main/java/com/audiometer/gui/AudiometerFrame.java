package com.audiometer.gui;

import com.audiometer.algorithm.AudiometrySessionEngine;
import com.audiometer.communication.SerialCommunicationManager;
import com.audiometer.model.Ear;
import com.audiometer.model.Response;
import com.audiometer.model.TestState;
import com.fazecast.jSerialComm.SerialPort;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AudiometerFrame extends JFrame {

    private final com.audiometer.gui.AudiogramPanel audiogramPanel = new com.audiometer.gui.AudiogramPanel();
    private final SerialCommunicationManager serialManager = new SerialCommunicationManager();
    private TestState currentState;


    private JComboBox<String> portCombo;
    private JButton btnConnect;
    private JButton btnDisconnect;
    private JLabel lblConnectionStatus;


    private JComboBox<Ear> earComboBox;
    private JComboBox<Integer> freqComboBox;
    private JSpinner dbSpinner;
    private JButton sendSignalButton;
    private JButton patientHeardButton;
    private JButton patientNotHeardButton;
    private JTextArea logTextArea;

    private boolean serialConnected = false;

    public AudiometerFrame() {
        setTitle("Ankara University - Clinical Audiometer Software");
        setSize(900, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(5, 5));

        add(buildConnectionPanel(), BorderLayout.NORTH);
        add(buildControlPanel(),   BorderLayout.WEST);
        add(audiogramPanel,        BorderLayout.CENTER);
        add(buildLogPanel(),       BorderLayout.SOUTH);

        syncTestState();


        serialManager.registerResponseCallback(response ->
            SwingUtilities.invokeLater(() -> {
                log("← Serial RESPONSE: " + response.frequencyHz() + " Hz, " + response.intensityDb() + " dB, heard=" + response.heard());
                simulatePatientResponse(response.heard());
            })
        );
    }


    private JPanel buildConnectionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        panel.setBorder(BorderFactory.createTitledBorder("Seri Port Bağlantısı (Proteus / COMPIM)"));

        portCombo = new JComboBox<>();
        portCombo.setEditable(true);
        portCombo.setPreferredSize(new Dimension(120, 28));
        loadPorts();


        portCombo.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) { loadPorts(); }
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
            public void popupMenuCanceled(PopupMenuEvent e) {}
        });

        JButton btnRefresh = new JButton("↺ Yenile");
        btnRefresh.addActionListener(e -> loadPorts());

        btnConnect    = new JButton("Bağlan");
        btnDisconnect = new JButton("Bağlantıyı Kes");
        btnDisconnect.setEnabled(false);

        lblConnectionStatus = new JLabel("● Bağlı Değil");
        lblConnectionStatus.setForeground(Color.RED);
        lblConnectionStatus.setFont(lblConnectionStatus.getFont().deriveFont(Font.BOLD));

        btnConnect.addActionListener(e -> connectSerial());
        btnDisconnect.addActionListener(e -> disconnectSerial());

        panel.add(new JLabel("Port:"));
        panel.add(portCombo);
        panel.add(btnRefresh);
        panel.add(btnConnect);
        panel.add(btnDisconnect);
        panel.add(Box.createHorizontalStrut(16));
        panel.add(lblConnectionStatus);

        JLabel hint = new JLabel("  ⚠ Önce Proteus simülasyonunu başlatın, sonra Yenile'ye basın.");
        hint.setForeground(new Color(150, 90, 0));
        hint.setFont(hint.getFont().deriveFont(Font.ITALIC, 11f));
        panel.add(hint);

        return panel;
    }

    private void loadPorts() {
        String selected = (String) portCombo.getSelectedItem();
        portCombo.removeAllItems();
        for (SerialPort p : SerialPort.getCommPorts()) {
            portCombo.addItem(p.getSystemPortName());
        }
        if (selected != null) portCombo.setSelectedItem(selected);
        if (portCombo.getItemCount() == 0) portCombo.addItem("(port bulunamadı)");
    }

    private void addManualPortInput() {
        JTextField manualPort = new JTextField(10);
        manualPort.setToolTipText("/dev/ttys003 gibi yaz");
        JButton addBtn = new JButton("Ekle");
        addBtn.addActionListener(e -> {
            String p = manualPort.getText().trim();
            if (!p.isEmpty()) {
                portCombo.addItem(p);
                portCombo.setSelectedItem(p);
                manualPort.setText("");
            }
        });
    }

    private void connectSerial() {
        String portName = (String) portCombo.getSelectedItem();
        if (portName == null || portName.startsWith("(")) {
            JOptionPane.showMessageDialog(this, "Lütfen geçerli bir port seçin.", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean ok = serialManager.connect(portName, 9600);
        if (ok) {
            serialConnected = true;
            lblConnectionStatus.setText("● Bağlandı: " + portName);
            lblConnectionStatus.setForeground(new Color(0, 140, 0));
            btnConnect.setEnabled(false);
            btnDisconnect.setEnabled(true);
            log("✔ Seri port açıldı: " + portName);
        } else {
            log("HATA: " + portName + " portu açılamadı!");
            JOptionPane.showMessageDialog(this,
                    "Port açılamadı: " + portName,
                    "Bağlantı Hatası", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void disconnectSerial() {
        serialManager.disconnect();
        serialConnected = false;
        lblConnectionStatus.setText("● Bağlı Değil");
        lblConnectionStatus.setForeground(Color.RED);
        btnConnect.setEnabled(true);
        btnDisconnect.setEnabled(false);
        log("✖ Seri port kapatıldı.");
    }


    private JPanel buildControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 4));
        panel.setPreferredSize(new Dimension(220, 0));

        JPanel testPanel = new JPanel(new GridLayout(0, 1, 4, 4));
        testPanel.setBorder(BorderFactory.createTitledBorder("Test Kontrolü"));

        earComboBox = new JComboBox<>(Ear.values());
        earComboBox.addActionListener(e -> syncTestState());

        Integer[] frequencies = {250, 500, 1000, 2000, 4000, 8000};
        freqComboBox = new JComboBox<>(frequencies);
        freqComboBox.setSelectedItem(1000);
        freqComboBox.addActionListener(e -> syncTestState());

        dbSpinner = new JSpinner(new SpinnerNumberModel(50, 0, 120, 5));
        dbSpinner.addChangeListener(e -> syncTestState());

        sendSignalButton      = new JButton("▶ Stimulus Gönder");
        patientHeardButton    = new JButton("✔ Duydu (HEARD)");
        patientNotHeardButton = new JButton("✖ Duymadı (NOT HEARD)");

        patientHeardButton.setEnabled(false);
        patientNotHeardButton.setEnabled(false);

        patientHeardButton.setForeground(new Color(0, 120, 0));
        patientNotHeardButton.setForeground(new Color(180, 0, 0));

        sendSignalButton.addActionListener(e -> sendStimulus());
        patientHeardButton.addActionListener(e -> simulatePatientResponse(true));
        patientNotHeardButton.addActionListener(e -> simulatePatientResponse(false));

        JButton btnClear = new JButton("🗑 Grafiği Temizle");
        btnClear.addActionListener(e -> { audiogramPanel.clearGraph(); log("Graf temizlendi."); });

        testPanel.add(new JLabel("Kulak:"));
        testPanel.add(earComboBox);
        testPanel.add(new JLabel("Frekans (Hz):"));
        testPanel.add(freqComboBox);
        testPanel.add(new JLabel("Yoğunluk (dB):"));
        testPanel.add(dbSpinner);
        testPanel.add(sendSignalButton);
        testPanel.add(patientHeardButton);
        testPanel.add(patientNotHeardButton);
        testPanel.add(btnClear);

        JLabel modeNote = new JLabel("<html><i><small>Seri bağlantıda yanıt<br>otomatik gelir.</small></i></html>");
        modeNote.setForeground(Color.GRAY);

        panel.add(testPanel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(modeNote);

        return panel;
    }

    private JPanel buildLogPanel() {
        logTextArea = new JTextArea(7, 60);
        logTextArea.setEditable(false);
        logTextArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logTextArea.setBackground(new Color(245, 245, 245));
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("İşlem Kaydı"));
        p.add(new JScrollPane(logTextArea));
        return p;
    }



    private void syncTestState() {
        if (earComboBox == null || freqComboBox == null || dbSpinner == null) return;
        Ear ear   = (Ear) earComboBox.getSelectedItem();
        int freq  = (Integer) freqComboBox.getSelectedItem();
        int db    = (Integer) dbSpinner.getValue();

        if (currentState == null
                || currentState.ear() != ear
                || currentState.frequencyHz() != freq) {
            currentState = new TestState(ear, freq, db, new ArrayList<>(), Optional.empty());
            if (logTextArea != null)
                log("Test hazırlandı: " + ear + " kulak | " + freq + " Hz | " + db + " dB");
        } else {
            currentState = new TestState(
                    currentState.ear(), currentState.frequencyHz(), db,
                    currentState.responses(), currentState.thresholdDb());
        }
    }

    private void sendStimulus() {
        int freq = currentState.frequencyHz();
        int db   = currentState.currentDb();

        log("▶ Stimulus: " + freq + " Hz @ " + db + " dB — yanıt bekleniyor...");


        if (serialConnected) {
            serialManager.sendCommand(freq, db);
            log("→ Serial komut gönderildi: TONE:" + freq + ":" + db);
        }

        sendSignalButton.setEnabled(false);
        patientHeardButton.setEnabled(true);
        patientNotHeardButton.setEnabled(true);
    }

    private void simulatePatientResponse(boolean heard) {
        Response response = new Response(currentState.frequencyHz(), currentState.currentDb(), heard);
        log("Yanıt → duydu: " + heard);

        currentState = AudiometrySessionEngine.handleResponse(currentState, response);

        patientHeardButton.setEnabled(false);
        patientNotHeardButton.setEnabled(false);
        sendSignalButton.setEnabled(true);

        if (currentState.thresholdDb().isPresent()) {
            int threshold = currentState.thresholdDb().get();
            log("✔ EŞİK BULUNDU: " + currentState.frequencyHz() + " Hz → " + threshold + " dB");
            audiogramPanel.addThreshold(currentState.ear(), currentState.frequencyHz(), threshold);

            currentState = new TestState(
                    currentState.ear(), currentState.frequencyHz(),
                    threshold, new ArrayList<>(), Optional.empty());
        } else {
            dbSpinner.setValue(currentState.currentDb());
            log("Hughson-Westlake → sonraki seviye: " + currentState.currentDb() + " dB");
        }
    }

    private void log(String message) {
        logTextArea.append(message + "\n");
        logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new AudiometerFrame().setVisible(true));
    }
}
