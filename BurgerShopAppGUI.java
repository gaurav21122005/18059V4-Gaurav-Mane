import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

class Burger {
    private String name;
    private double price;
    private Vector<String> toppings;
    private Map<String, Double> toppingPrices;

    public Burger(String name, double price) {
        this.name = name;
        this.price = price;
        this.toppings = new Vector<>();
        this.toppingPrices = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        double totalPrice = price;
        for (double toppingPrice : toppingPrices.values()) {
            totalPrice += toppingPrice;
        }
        return totalPrice;
    }

    public void addTopping(String topping, double price) {
        toppings.add(topping);
        toppingPrices.put(topping, price);
    }

    @Override
    public String toString() {
        StringBuilder burgerDescription = new StringBuilder(name + " (₹" + price + " Rupees)");
        if (!toppings.isEmpty()) {
            burgerDescription.append(", Toppings: ");
            for (String topping : toppings) {
                burgerDescription.append(topping).append(" (₹").append(toppingPrices.get(topping)).append(") ");
            }
        }
        return burgerDescription.toString();
    }
}

class Menu {
    private Vector<Burger> burgers = new Vector<>();

    public Menu() {
        burgers.add(new Burger("Cheeseburger", 415.00));
        burgers.add(new Burger("Veggie Burger", 374.00));
        burgers.add(new Burger("Chicken Burger", 457.00));
        burgers.add(new Burger("Fish Burger", 485.00));
        burgers.add(new Burger("Bacon Burger", 525.00));
        burgers.add(new Burger("Double Patty Burger", 600.00));
    }

    public Vector<Burger> getBurgers() {
        return burgers;
    }

    public void addBurger(Burger burger) {
        burgers.add(burger);
    }
}

class Order {
    private Vector<Burger> orderItems = new Vector<>();

    public void addBurger(Burger burger) {
        orderItems.add(burger);
    }

    public Vector<Burger> getOrderItems() {
        return orderItems;
    }

    public void removeBurger(Burger burger) {
        orderItems.remove(burger);
    }

    public String viewOrder() {
        StringBuilder orderDetails = new StringBuilder("Your Order:\n");
        double total = 0;
        for (Burger burger : orderItems) {
            orderDetails.append(burger).append("\n");
            total += burger.getPrice();
        }
        orderDetails.append("Total: ₹").append(total).append(" Rupees");
        return orderDetails.toString();
    }
}

public class BurgerShopAppGUI {
    private JFrame frame;
    private Menu menu;
    private Map<String, Order> customerOrders;  // customerOrders map now uses customer ID
    private String currentCustomerId;  // Use customer ID instead of name
    private JTextArea orderSummary;
    private boolean isAdminMode;
    private final Map<String, Double> burgerKingToppings;
    private static final String ADMIN_PASSWORD = "admin123"; // Admin password

    public BurgerShopAppGUI() {
        menu = new Menu();
        customerOrders = new HashMap<>();
        isAdminMode = false; // Default to customer mode
        burgerKingToppings = new HashMap<>();
        initializeBurgerKingToppings();
        initialize();
    }

    private void initializeBurgerKingToppings() {
        burgerKingToppings.put("Lettuce", 10.0);
        burgerKingToppings.put("Tomato", 15.0);
        burgerKingToppings.put("Cheese Slice", 25.0);
        burgerKingToppings.put("Pickles", 5.0);
        burgerKingToppings.put("Onion Rings", 20.0);
        burgerKingToppings.put("Bacon", 30.0);
        burgerKingToppings.put("BBQ Sauce", 20.0);
    }

    private void initialize() {
        frame = new JFrame("Burger Shop");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 500);
        frame.setLayout(new BorderLayout());

        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(menu.getBurgers().size() + 2, 1));
        JLabel menuLabel = new JLabel("Select a Burger:");
        menuPanel.add(menuLabel);

        // Create buttons for each burger
        refreshMenu(menuPanel);

        orderSummary = new JTextArea("Your Order:\n");
        orderSummary.setEditable(false);
        JScrollPane orderScrollPane = new JScrollPane(orderSummary);

        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new FlowLayout());

        JButton switchModeButton = new JButton("Switch to Admin Mode");
        switchModeButton.addActionListener(e -> switchMode(switchModeButton, menuPanel));

        JButton viewOrderButton = new JButton("View Order");
        viewOrderButton.addActionListener(e -> JOptionPane.showMessageDialog(frame, getCurrentOrder().viewOrder()));

        JButton deleteOrderButton = new JButton("Delete Order");
        deleteOrderButton.addActionListener(e -> deleteOrder());

        // Declare and initialize finishButton
        JButton finishButton = new JButton("Finish Order");
        finishButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, getCurrentOrder().viewOrder());
            JOptionPane.showMessageDialog(frame, "Order for customer ID " + currentCustomerId + " has been finished.");
            customerOrders.remove(currentCustomerId); // Remove the current customer order
            orderSummary.setText("Your Order:\n");
            switchCustomer(menuPanel);
        });

        JButton switchCustomerButton = new JButton("Switch Customer");
        switchCustomerButton.addActionListener(e -> switchCustomer(menuPanel));

        actionPanel.add(switchModeButton);
        actionPanel.add(viewOrderButton);
        actionPanel.add(deleteOrderButton);
        actionPanel.add(finishButton); // Add finishButton to the panel
        actionPanel.add(switchCustomerButton);

        frame.add(menuPanel, BorderLayout.CENTER);
        frame.add(orderScrollPane, BorderLayout.EAST);
        frame.add(actionPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private void refreshMenu(JPanel menuPanel) {
        menuPanel.removeAll();
        JLabel menuLabel = new JLabel("Select a Burger:");
        menuPanel.add(menuLabel);

        // Only allow burger selection after customer ID is set
        if (currentCustomerId == null || currentCustomerId.trim().isEmpty()) {
            JButton setCustomerButton = new JButton("Set Customer ID");
            setCustomerButton.addActionListener(e -> switchCustomer(menuPanel));
            menuPanel.add(setCustomerButton);
        } else {
            for (Burger burger : menu.getBurgers()) {
                JButton burgerButton = new JButton(burger.toString());
                burgerButton.addActionListener(e -> {
                    selectBurger(burger);
                });
                menuPanel.add(burgerButton);
            }
        }

        if (isAdminMode) {
            JButton addBurgerButton = new JButton("Add Burger");
            addBurgerButton.addActionListener(e -> addBurger());
            menuPanel.add(addBurgerButton);
        }

        menuPanel.revalidate();
        menuPanel.repaint();
    }

    private void selectBurger(Burger selectedBurger) {
        Burger burger = new Burger(selectedBurger.getName(), selectedBurger.getPrice());

        boolean addingToppings = true;
        while (addingToppings) {
            Object[] options = burgerKingToppings.keySet().toArray();
            String topping = (String) JOptionPane.showInputDialog(frame, "Select a topping to add:", "Add Toppings", JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

            if (topping == null || topping.isEmpty()) {
                addingToppings = false;
            } else {
                burger.addTopping(topping, burgerKingToppings.get(topping));
            }
        }

        getCurrentOrder().addBurger(burger);
        orderSummary.append(burger + "\n");
    }

    private void addBurger() {
        String name = JOptionPane.showInputDialog(frame, "Enter burger name:", "Add Burger", JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Burger name cannot be empty.");
            return;
        }

        String priceStr = JOptionPane.showInputDialog(frame, "Enter burger price:", "Add Burger", JOptionPane.PLAIN_MESSAGE);
        try {
            double price = Double.parseDouble(priceStr);
            menu.addBurger(new Burger(name, price));
            JOptionPane.showMessageDialog(frame, "Burger added successfully.");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Invalid price entered.");
        }
    }

    private void deleteOrder() {
        Vector<Burger> currentOrder = getCurrentOrder().getOrderItems();
        if (currentOrder.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Your order is empty.");
            return;
        }

        Object[] orderItems = currentOrder.toArray();
        Burger burgerToDelete = (Burger) JOptionPane.showInputDialog(
            frame,
            "Select a burger to remove from your order:",
            "Delete Order",
            JOptionPane.PLAIN_MESSAGE,
            null,
            orderItems,
            orderItems[0]
        );

        if (burgerToDelete != null) {
            getCurrentOrder().removeBurger(burgerToDelete);
            orderSummary.setText(getCurrentOrder().viewOrder());
            JOptionPane.showMessageDialog(frame, "Burger removed from your order.");
        }
    }

    private void switchMode(JButton switchModeButton, JPanel menuPanel) {
        if (!isAdminMode) {
            String password = JOptionPane.showInputDialog(frame, "Enter admin password:", "Admin Login", JOptionPane.PLAIN_MESSAGE);
            if (ADMIN_PASSWORD.equals(password)) {
                isAdminMode = true;
                switchModeButton.setText("Switch to Customer Mode");
                refreshMenu(menuPanel);
            } else {
                JOptionPane.showMessageDialog(frame, "Incorrect password. Access denied.");
            }
        } else {
            isAdminMode = false;
            switchModeButton.setText("Switch to Admin Mode");
            refreshMenu(menuPanel);
        }
    }

    private void switchCustomer(JPanel menuPanel) {
        String customerId = JOptionPane.showInputDialog(frame, "Enter customer ID:", "Switch Customer", JOptionPane.PLAIN_MESSAGE);

        if (customerId == null || customerId.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Customer ID cannot be empty.");
            return;
        }

        currentCustomerId = customerId;
        customerOrders.putIfAbsent(currentCustomerId, new Order());
        orderSummary.setText(getCurrentOrder().viewOrder());
        refreshMenu(menuPanel);
    }

    private Order getCurrentOrder() {
        return customerOrders.get(currentCustomerId);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BurgerShopAppGUI::new);
    }
}
