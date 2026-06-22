package com.audiometer.communication;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.audiometer.processing.ResponseMessageParser;
import com.audiometer.model.Response;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class SerialCommunicationManager {

    private SerialPort serialPort;
    private Consumer<Response> responseCallback;

    public boolean connect(String portName, int baudRate) {
        serialPort = SerialPort.getCommPort(portName);
        serialPort.setBaudRate(baudRate);
        serialPort.setNumDataBits(8);
        serialPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
        serialPort.setParity(SerialPort.NO_PARITY);

        if (serialPort.openPort()) {
            setupListener();
            return true;
        }
        return false;
    }

    public void disconnect() {
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.removeDataListener();
            serialPort.closePort();
        }
    }

    public boolean isConnected() {
        return serialPort != null && serialPort.isOpen();
    }

    public void registerResponseCallback(Consumer<Response> callback) {
        this.responseCallback = callback;
    }

    private void setupListener() {
        serialPort.addDataListener(new SerialPortDataListener() {
            private final StringBuilder buffer = new StringBuilder();

            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) return;

                byte[] newData = new byte[serialPort.bytesAvailable()];
                serialPort.readBytes(newData, newData.length);
                buffer.append(new String(newData, StandardCharsets.UTF_8));

                int newlineIndex;
                while ((newlineIndex = buffer.indexOf("\n")) != -1) {
                    String message = buffer.substring(0, newlineIndex).trim();
                    buffer.delete(0, newlineIndex + 1);
                    if (!message.isEmpty()) {
                        processIncomingMessage(message);
                    }
                }
            }
        });
    }

    private void processIncomingMessage(String rawMessage) {
        // Proteus'tan gelen format: "RESPONSE,<frekans>,<dB>"
        // Örnek: "RESPONSE,1000,40"
        if (rawMessage.equalsIgnoreCase("RESPONSE")) {
            // Basit RESPONSE mesajı (frekans/dB bilgisi olmadan)
            if (responseCallback != null) {
                responseCallback.accept(new Response(0, 0, true));
            }
            return;
        }

        String[] tokens = rawMessage.split(",");
        if (tokens.length == 3) {
            try {
                String type = tokens[0].trim();
                int freq    = Integer.parseInt(tokens[1].trim());
                int db      = Integer.parseInt(tokens[2].trim());

                ResponseMessageParser.parse(type, freq, db).ifPresent(response -> {
                    if (responseCallback != null) {
                        responseCallback.accept(response);
                    }
                });
            } catch (NumberFormatException ignored) {}
        }
    }

    /**
     * Arduino'ya ton komutu gönderir.
     * Format: "TONE:<frekans>:<dB>\n"
     * Örnek:  "TONE:1000:40\n"
     */
    public void sendCommand(int frequency, int intensityDb) {
        if (serialPort != null && serialPort.isOpen()) {
            String cmd = "FREQ:" + frequency + ";DB:" + intensityDb + "\n";
            byte[] bytes = cmd.getBytes(StandardCharsets.UTF_8);
            serialPort.writeBytes(bytes, bytes.length);
        }
    }

    /**
     * Arduino'ya durdurma komutu gönderir.
     */
    public void sendStop() {
        if (serialPort != null && serialPort.isOpen()) {
            byte[] bytes = "STOP\n".getBytes(StandardCharsets.UTF_8);
            serialPort.writeBytes(bytes, bytes.length);
        }
    }
}
