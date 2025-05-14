import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import javax.swing.*;

class Fruit {
    String name;
    double price;
    int stock;
    String unit;

    public Fruit(String name, double price, int stock, String unit) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.unit = unit;
    }

    public int getStock() {
        return stock;
    }

    public String getUnit() {
        return unit;
    }

    public double getPrice() {
        return price;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
}

public class FruitShopGUI {
    private JFrame frame;
    private JComboBox<String> fruitCombo;
    private JTextField quantityField;
    private JTextArea invoiceArea;
    private JLabel totalLabel;
    private JTextField nameField;
    private Map<String, Fruit> inventory = new LinkedHashMap<>();
    private Map<String, Integer> cart = new LinkedHashMap<>();
    private Map<String, String> comboToKeyMap = new HashMap<>();

    public FruitShopGUI() {
        initializeInventory();
        initializeUI();
    }

    private void initializeInventory() {
        inventory.put("🍎 Apple", new Fruit("Apple", 50, 10, "KG"));
        inventory.put("🍌 Banana", new Fruit("Banana", 10, 25, "Dozen"));
        inventory.put("🍊 Orange", new Fruit("Orange", 30, 15, "KG"));
        inventory.put("🥭 Mango", new Fruit("Mango", 60, 8, "KG"));
        inventory.put("🍇 Grapes", new Fruit("Grapes", 40, 12, "KG"));
    }

    private void initializeUI() {
        frame = new JFrame("Fruit Shop 💸");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 500);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(new Color(30, 30, 30));

        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        inputPanel.setBackground(new Color(30, 30, 30));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel nameLabel = createLabel("Customer Name:");
        nameField = createTextField();

        JLabel fruitLabel = createLabel("Select Fruit:");
        fruitCombo = new JComboBox<>();

        JLabel qtyLabel = createLabel("Quantity:");
        quantityField = createTextField();

        JButton addButton = createButton("Add to Cart ➕");
        addButton.addActionListener(e -> addToCart());

        inputPanel.add(nameLabel);
        inputPanel.add(nameField);
        inputPanel.add(fruitLabel);
        inputPanel.add(fruitCombo);
        inputPanel.add(qtyLabel);
        inputPanel.add(quantityField);
        inputPanel.add(new JLabel());
        inputPanel.add(addButton);

        invoiceArea = new JTextArea();
        invoiceArea.setEditable(false);
        invoiceArea.setLineWrap(true);
        invoiceArea.setWrapStyleWord(true);
        invoiceArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        invoiceArea.setBackground(new Color(40, 40, 40));
        invoiceArea.setForeground(Color.GREEN);

        JScrollPane invoiceScroll = new JScrollPane(invoiceArea);
        invoiceScroll.setPreferredSize(new Dimension(550, 250));

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(30, 30, 30));

        totalLabel = new JLabel("Total: ₹0.00");
        totalLabel.setForeground(Color.WHITE);
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        JButton generateButton = createButton("Generate Invoice 🧾");
        generateButton.addActionListener(e -> generateInvoice());

        JButton clearButton = createButton("Clear 🗑");
        clearButton.addActionListener(e -> clearCart());

        bottomPanel.add(totalLabel);
        bottomPanel.add(generateButton);
        bottomPanel.add(clearButton);

        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(invoiceScroll, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
        updateFruitCombo();
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        return label;
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setBackground(Color.DARK_GRAY);
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(Color.WHITE);
        return tf;
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(new Color(60, 90, 150));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        return btn;
    }

    private void addToCart() {
        String displayKey = (String) fruitCombo.getSelectedItem();
        String fruitKey = comboToKeyMap.get(displayKey);
        Fruit fruit = inventory.get(fruitKey);

        String qtyText = quantityField.getText().trim();
        if (qtyText.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Enter quantity.");
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(qtyText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Invalid quantity.");
            return;
        }

        if (qty <= 0) {
            JOptionPane.showMessageDialog(frame, "Quantity must be positive.");
            return;
        }

        if (fruit != null && fruit.getStock() < qty) {
            JOptionPane.showMessageDialog(frame, "Not enough stock!");
            return;
        }

        if (fruit != null) {
            fruit.setStock(fruit.getStock() - qty);
            cart.put(fruitKey, cart.getOrDefault(fruitKey, 0) + qty);
        }

        updateInvoiceArea(false);
        updateTotal();
        quantityField.setText("");
        updateFruitCombo();
    }

    private void updateTotal() {
        double total = 0;
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            String fruitKey = entry.getKey();
            Fruit fruit = inventory.get(fruitKey);
            if (fruit != null) {
                total += fruit.getPrice() * entry.getValue();
            }
        }
        double discounted = total * 0.9;
        totalLabel.setText(String.format("Total (10%% off): ₹%.2f", discounted));
    }

    private void updateInvoiceArea(boolean isFinal) {
        StringBuilder sb = new StringBuilder();
        sb.append("Customer: ").append(nameField.getText()).append("\n\n");
        double total = 0;

        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            String fruitKey = entry.getKey();
            Fruit fruit = inventory.get(fruitKey);
            if (fruit != null) {
                double sub = fruit.getPrice() * entry.getValue();
                sb.append(String.format("%s x %d %s = ₹%.2f\n",
                        fruit.name, entry.getValue(), fruit.unit, sub));
                total += sub;
            }
        }

        if (isFinal) {
            sb.append("\nSubtotal: ₹").append(String.format("%.2f", total));
            sb.append("\nDiscount (10%): ₹").append(String.format("%.2f", total * 0.1));
            sb.append("\nTotal: ₹").append(String.format("%.2f", total * 0.9));
        }

        invoiceArea.setText(sb.toString());
    }

    private void generateInvoice() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter customer name.");
            return;
        }

        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Cart is empty!");
            return;
        }

        updateInvoiceArea(true);
        try (FileWriter fw = new FileWriter("sales.txt", true)) {
            fw.write(invoiceArea.getText() + "\n\n");
            JOptionPane.showMessageDialog(frame, "Invoice saved to sales.txt 📄");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Failed to write invoice.");
        }
    }

    private void clearCart() {
        cart.clear();
        updateInvoiceArea(false);
        totalLabel.setText("Total: ₹0.00");
        quantityField.setText("");
        invoiceArea.setText("");
        initializeInventory();
        updateFruitCombo();
    }

    private void updateFruitCombo() {
        fruitCombo.removeAllItems();
        comboToKeyMap.clear();

        for (Map.Entry<String, Fruit> entry : inventory.entrySet()) {
            String fruitKey = entry.getKey();
            Fruit fruit = entry.getValue();
            String display = String.format("%s (Stock: %d %s)",
                    fruitKey, fruit.getStock(), fruit.getUnit());
            fruitCombo.addItem(display);
            comboToKeyMap.put(display, fruitKey);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FruitShopGUI::new);
    }
}
