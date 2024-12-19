import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

class Burger {
    private int id;
    private String name;
    private double price;
    private Vector<String> toppings;
    private boolean extraCheese;

    public Burger(int id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.toppings = new Vector<>();
        this.extraCheese = false;
    }

    public int getId() {
        return id;
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

    public Vector<String> getToppings() {
        return toppings;
    }

    public boolean hasExtraCheese() {
        return extraCheese;
    }

    public void addTopping(String topping) {
        toppings.add(topping);
    }

    public void addExtraCheese() {
        this.extraCheese = true;
    }

    @Override
    public String toString() {
        StringBuilder burgerDescription = new StringBuilder(name + " (₹" + price + ")");
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
        loadBurgersFromDatabase();
    }

    private void loadBurgersFromDatabase() {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:mysql://sql12.freesqldatabase.com:3306/sql12752977", "sql12752977", "ZaihyeTeH6")) {

            String query = "SELECT * FROM burgers";
            try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(query)) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    double price = resultSet.getDouble("price");
                    burgers.add(new Burger(id, name, price));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    public void saveOrderToDatabase() {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:mysql://sql12.freesqldatabase.com:3306/sql12752977", "sql12752977", "ZaihyeTeH6")) {

            String insertOrderQuery = "INSERT INTO orders (burger_id, extra_cheese, toppings) VALUES (?, ?, ?)";
            for (Burger burger : orderItems) {
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertOrderQuery)) {
                    preparedStatement.setInt(1, burger.getId());
                    preparedStatement.setBoolean(2, burger.hasExtraCheese());
                    preparedStatement.setString(3, String.join(", ", burger.getToppings()));
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

public class BurgerShopApp {
    private JFrame frame;
    private Menu menu;
    private Order order;
    private JTextArea orderSummary;

    public BurgerShopApp() {
        menu = new Menu();
        order = new Order();
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Burger Shop");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        JScrollPane menuScrollPane = new JScrollPane(menuPanel);
        JLabel menuLabel = new JLabel("Select a Burger:");
        menuLabel.setFont(new Font("Arial", Font.BOLD, 16));
        menuPanel.add(menuLabel);

        for (Burger burger : menu.getBurgers()) {
            JButton burgerButton = new JButton(burger.toString());
            burgerButton.setFont(new Font("Arial", Font.PLAIN, 14));
            burgerButton.addActionListener(e -> selectBurger(burger));
            menuPanel.add(burgerButton);
        }

        orderSummary = new JTextArea("Your Order:\n");
        orderSummary.setFont(new Font("Arial", Font.PLAIN, 14));
        orderSummary.setEditable(false);
        JScrollPane orderScrollPane = new JScrollPane(orderSummary);

        JButton finishButton = new JButton("Finish Order");
        finishButton.setFont(new Font("Arial", Font.BOLD, 14));
        finishButton.addActionListener(e -> {
            order.saveOrderToDatabase();
            JOptionPane.showMessageDialog(frame, order.viewOrder(), "Order Summary", JOptionPane.INFORMATION_MESSAGE);
            frame.dispose();
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(finishButton, BorderLayout.CENTER);

        frame.add(menuScrollPane, BorderLayout.WEST);
        frame.add(orderScrollPane, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

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
        SwingUtilities.invokeLater(BurgerShopApp::new);
    }
}
 