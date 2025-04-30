package com.mycompany.mavenproject1;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Mavenproject1 extends JFrame {
    private JTextField lowerBoundField, upperBoundField, stepField;
    private JTable table;
    private DefaultTableModel tableModel;
    private ArrayList<RecIntegral> records = new ArrayList<>();

    public Mavenproject1() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Вычисление интеграла");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(2, 3, 5, 5));
        lowerBoundField = new JTextField();
        upperBoundField = new JTextField();
        stepField = new JTextField();

        inputPanel.add(new JLabel("Нижняя граница:"));
        inputPanel.add(new JLabel("Верхняя граница:"));
        inputPanel.add(new JLabel("Шаг:"));
        inputPanel.add(lowerBoundField);
        inputPanel.add(upperBoundField);
        inputPanel.add(stepField);

        add(inputPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"Нижняя граница", "Верхняя граница", "Шаг", "Результат"}, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new GridLayout(2, 4, 5, 5));
        JButton addButton = new JButton("Добавить");
        JButton removeButton = new JButton("Удалить");
        JButton clearButton = new JButton("Очистить");
        JButton fillButton = new JButton("Заполнить");
        JButton saveTextButton = new JButton("Сохранить в текст");
        JButton loadTextButton = new JButton("Загрузить из текста");
        JButton saveBinaryButton = new JButton("Сохранить в бинарный");
        JButton loadBinaryButton = new JButton("Загрузить из бинарного");

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(fillButton);
        buttonPanel.add(saveTextButton);
        buttonPanel.add(loadTextButton);
        buttonPanel.add(saveBinaryButton);
        buttonPanel.add(loadBinaryButton);

        add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addRow());
        removeButton.addActionListener(e -> removeRow());
        clearButton.addActionListener(e -> clearTable());
        fillButton.addActionListener(e -> fillTable());
        saveTextButton.addActionListener(e -> saveToTextFile());
        loadTextButton.addActionListener(e -> loadFromTextFile());
        saveBinaryButton.addActionListener(e -> saveToBinaryFile());
        loadBinaryButton.addActionListener(e -> loadFromBinaryFile());

        setVisible(true);
    }

    private void addRow() {
        try {
            double lower = Double.parseDouble(lowerBoundField.getText());
            double upper = Double.parseDouble(upperBoundField.getText());
            double step = Double.parseDouble(stepField.getText());

            RecIntegral record = new RecIntegral(lower, upper, step, 0);
            double result = integrate(lower, upper, step);
            record.setResult(result);
            records.add(record);
            tableModel.addRow(new Object[]{record.getLowerBound(), record.getUpperBound(), record.getStep(), record.getResult()});
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Введите числовые значения!", "Ошибка", JOptionPane.ERROR_MESSAGE);
        } catch (InvalidIntegralException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeRow() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            records.remove(selectedRow);
            tableModel.removeRow(selectedRow);
        } else {
            JOptionPane.showMessageDialog(this, "Выберите строку для удаления!", "Ошибка", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void clearTable() {
        records.clear();
        tableModel.setRowCount(0);
    }

    private void fillTable() {
        tableModel.setRowCount(0);
        for (RecIntegral record : records) {
            tableModel.addRow(new Object[]{record.getLowerBound(), record.getUpperBound(), record.getStep(), record.getResult()});
        }
    }

    private double integrate(double lower, double upper, double step) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout((int) TimeUnit.SECONDS.toMillis(10));
            
            String message = lower + "," + upper + "," + step;
            byte[] sendData = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, 
                    InetAddress.getByName("localhost"), 5000);
            socket.send(sendPacket);

            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);

            String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
            return Double.parseDouble(response.split(",")[1]);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка при вычислении интеграла: " + e.getMessage(), 
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return 0.0;
        }
    }

    private void saveToTextFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter writer = new PrintWriter(fileChooser.getSelectedFile())) {
                for (RecIntegral record : records) {
                    writer.printf("%f %f %f %f%n", 
                            record.getLowerBound(), 
                            record.getUpperBound(), 
                            record.getStep(), 
                            record.getResult());
                }
            } catch (IOException e) {
                showError("Ошибка сохранения в текстовый файл");
            }
        }
    }

    private void loadFromTextFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedReader reader = new BufferedReader(new FileReader(fileChooser.getSelectedFile()))) {
                clearTable();
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length == 4) {
                        double lower = Double.parseDouble(parts[0]);
                        double upper = Double.parseDouble(parts[1]);
                        double step = Double.parseDouble(parts[2]);
                        double result = Double.parseDouble(parts[3]);
                        records.add(new RecIntegral(lower, upper, step, result));
                    }
                }
                fillTable();
            } catch (Exception e) {
                showError("Ошибка загрузки из текстового файла");
            }
        }
    }

    private void saveToBinaryFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(fileChooser.getSelectedFile()))) {
                oos.writeObject(records);
            } catch (IOException e) {
                showError("Ошибка сохранения в бинарный файл");
            }
        }
    }

    private void loadFromBinaryFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(fileChooser.getSelectedFile()))) {
                records = (ArrayList<RecIntegral>) ois.readObject();
                fillTable();
            } catch (Exception e) {
                showError("Ошибка загрузки из бинарного файла");
            }
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Mavenproject1::new);
    }
}