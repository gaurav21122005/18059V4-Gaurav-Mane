import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

class Burger {
    private String name;
    private double price;
    private Vector<String> toppings;
    private boolean extraCheese;

    public Burger(String name, double price) {
        this.name = name;
        this.price = price;
        this.toppings = new Vector<>();
        this.extraCheese = false;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        double totalPrice = price;
        if (extraCheese) {
            totalPrice += 50; // Extra cheese costs ₹50
        }
        return totalPrice;
    }

    public void addTopping(String topping) {
        toppings.add(topping);
    }

    public void addExtraCheese() {
        this.extraCheese = true;
    }

    @Override
    public String toString() {
        StringBuilder burgerDescription = new StringBuilder(name + " (₹" + price + " Rupees)");
        if (extraCheese) {
            burgerDescription.append(", Extra Cheese");
        }
        if (!toppings.isEmpty()) {
            burgerDescription.append(", Toppings: " + String.join(", ", toppings));
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
    }

    public Vector<Burger> getBurgers() {
        return burgers;
    }
}

class Order {
    private Vector<Burger> orderItems = new Vector<>();

    public void addBurger(Burger burger) {
        orderItems.add(burger);
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
    private Order order;
    private JTextArea orderSummary;

    public BurgerShopAppGUI() {
        menu = new Menu();
        order = new Order();
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Burger Shop");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(menu.getBurgers().size(), 1));
        JLabel menuLabel = new JLabel("Select a Burger:");
        menuPanel.add(menuLabel);

        // Create buttons for each burger
        for (Burger burger : menu.getBurgers()) {
            JButton burgerButton = new JButton(burger.toString());
            burgerButton.addActionListener(e -> selectBurger(burger));
            menuPanel.add(burgerButton);
        }

        orderSummary = new JTextArea("Your Order:\n");
        orderSummary.setEditable(false);
        JScrollPane orderScrollPane = new JScrollPane(orderSummary);

        JButton finishButton = new JButton("Finish Order");
        finishButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, order.viewOrder());
            frame.dispose();
        });

        frame.add(menuPanel, BorderLayout.CENTER);
        frame.add(orderScrollPane, BorderLayout.EAST);
        frame.add(finishButton, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private void selectBurger(Burger burger) {
        int cheeseChoice = JOptionPane.showConfirmDialog(frame, "Add extra cheese for ₹50?", "Extra Cheese", JOptionPane.YES_NO_OPTION);
        if (cheeseChoice == JOptionPane.YES_OPTION) {
            burger.addExtraCheese();
        }

        boolean addingToppings = true;
        while (addingToppings) {
            String topping = JOptionPane.showInputDialog(frame, "Enter a topping to add (leave blank to finish):", "Add Toppings", JOptionPane.PLAIN_MESSAGE);
            if (topping == null || topping.isEmpty()) {
                addingToppings = false;
            } else {
                burger.addTopping(topping);
            }
        }

        order.addBurger(burger);
        orderSummary.append(burger + "\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BurgerShopAppGUI::new);
    }
}