# Clinical Audiometer System 🎧

A professional clinical audiometer software developed in Java, designed to perform pure-tone audiometry tests and simulate hardware-software integration.

## 🚀 Overview
This project provides a comprehensive graphical user interface (GUI) for audiologists to conduct hearing tests. It features an automated implementation of the **Hughson-Westlake algorithm** for threshold detection and supports real-time hardware communication via serial ports (designed to work with Proteus / COMPIM simulations).

## ✨ Key Features
* **Hughson-Westlake Algorithm:** Automated, clinically accurate hearing threshold detection (Down 10, Up 5 dB steps).
* **Real-Time Serial Communication:** Seamless integration with hardware or Proteus simulations using the `jSerialComm` library.
* **Dynamic Audiogram GUI:** Real-time plotting of hearing thresholds for both Left (X) and Right (O) ears on a standard audiogram chart.
* **State Management:** Robust Object-Oriented architecture separating algorithmic logic, data models, and UI components.
* **Clinical Validation:** Built-in validators for standard clinical frequencies (250Hz - 8000Hz) and intensity levels (0dB - 120dB).

## 🛠️ Tech Stack
* **Language:** Java (JDK 25)
* **GUI Framework:** Java Swing
* **Serial Communication:** `jSerialComm` (v2.10.4)
* **Architecture:** Object-Oriented Programming (OOP)

## ⚙️ Getting Started

### Prerequisites
* Java Development Kit (JDK) 25 or higher.
* Virtual Serial Port emulator (e.g., Virtual Serial Port Driver or Proteus COMPIM).

### Running the Application
1. Clone the repository:
   ```bash
   git clone [https://github.com/ahmet-cann/audiometer-system.git](https://github.com/ahmet-cann/audiometer-system.git)