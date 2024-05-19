package MainSystem;

import Item.Cart;
import Item.CartItem;
import Item.ItemStock;
import Order.Order;
import Order.OrderContent;
import Order.OrderState;
import Shop.Shop;
import User.Customer;
import User.Shipper;
import User.User;
import User.UserRole;
import Utils.Address;
import Utils.Utils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public final class SystemManager {
    private static final double SHIPPER_FEE = 5000.0;
    private static final double PROFIT = 0.09;
    private static final double SHOP_PORTION = 1.0 - PROFIT;
    private static final Path dataPath = Paths.get("data.json");
    private static final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private Hashtable<String, User> users;
    private double profit;
    private List<Order> orders;

    private SystemManager() {
        if (Files.exists(dataPath)) {
            try {
                String content = Files.readAllLines(dataPath).get(0);
                SystemDataHolder data = mapper.readValue(content, SystemDataHolder.class);
                users = data.users();
                profit = data.profit();
                orders = data.orders();
                for (Order order : orders) {
                    Customer customer = order.getCustomer();
                    if (customer != null) {
                        customer.addOrder(order);
                    }

                    Shipper shipper = order.getShipper();
                    if (shipper != null) {
                        shipper.addOrder(order);
                    }

                    Shop shop = order.getShop();
                    if (shop != null) {
                        shop.addOrder(order);
                    }
                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
//                e.printStackTrace();
                if (!Utils.promptInput("System message: Error reading data! Create new data? (y/n) ").equalsIgnoreCase("y")) {
                    System.exit(1);
                }
                users = new Hashtable<>();
                profit = 0.0;
                orders = new ArrayList<>();
            }
        } else {
            System.out.println("System message: Data file not found. Creating new data.");
            users = new Hashtable<>();
            profit = 0.0;
            orders = new ArrayList<>();
        }
    }

    private static final class SingletonHolder {
        private static final SystemManager instance = new SystemManager();
    }

    public static SystemManager getInstance() {
        return SingletonHolder.instance;
    }

    public void saveData() {
        try {
            SystemDataHolder data = new SystemDataHolder(users, profit, orders);
            String content = mapper.writeValueAsString(data);
            Files.writeString(dataPath, content);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("System message: Error opening file!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("System message: Error saving data!");
        }
    }

    public void displayMenu() {

        while (true) {
            saveData();
            User currentUser = null;
            String username;
            String password;
            System.out.println("====== LOGIN MENU ======");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.println("========================");

            String choice = Utils.promptInput("Enter your choice: ");
            switch (choice) {
                case "1":
                    System.out.println("------------Login menu--------------");
                    username = Utils.promptInput("Enter your username: ");
                    password = Utils.promptInput("Enter password: ");
                    currentUser = login(username, password);
                    if (currentUser == null) {
                        System.out.println("Cannot find account or wrong password.");
                    } else if (currentUser.getRole().equals(UserRole.Customer)) {
                        if (currentUser instanceof Customer) {
                            displayCustomerMenu((Customer) currentUser);
                        } else {
                            System.out.println("Error: User is not a Customer instance.");
                        }
                    } else {
                        if (currentUser instanceof Shipper) {
                            displayShipperMenu((Shipper) currentUser);
                        } else {
                            System.out.println("Error: User is not a Shipper instance.");
                        }

                    }
                    break;
                case "2":
                    displayRegisterMenu();
                    break;
                case "3":
                    System.out.println("Exit program...");
                    System.exit(0);
                default:
                    System.out.println("INVALID");
            }
        }
    }

    // Current user will be set to null if account is not found
    private User login(String username, String password) {
        return authorizeUser(username, password).orElse(null);
    }

    public Optional<User> authorizeUser(String username, String password) {
        if (username == null || password == null) return Optional.empty();
        User customer = users.get(username);
        if (customer == null) return Optional.empty();
        if (customer.getPassword().equals(password)) return Optional.of(customer);
        return Optional.empty();
    }

    private void displayRegisterMenu() {
        String username;
        String password;
        System.out.println("-------Register menu----------");
        System.out.println("Choose your role: ");
        System.out.println("1. Customer");
        System.out.println("2. Shipper");
        int roleChoice = Utils.promptIntInput("Option: ").orElse(-1);
        if (roleChoice != 1 && roleChoice != 2) {
            System.out.println("Invalid choice. Stop register.");
            return;
        }
        while (true) {
            username = Utils.promptInput("Enter your username: ");
            if (existsUser(username)) {
                System.out.println("This username existed. Try again!");
            } else break;
        }
        password = Utils.promptInput("Enter your password: ");
        String name = Utils.promptInput("Enter your name: ");
        String phone = Utils.promptInput("Enter your phone number: ");
        Address address = askForUpdateAddress().orElse(null);
        if (address == null) {
            System.out.println("Invalid input. Stop register.");
            return;
        }
        if (roleChoice == 1) {
            if (registerCustomer(username, password, name, phone, address)) {
                System.out.println("Register success!");
            } else System.out.println("Register failed!");
        } else {
            if (registerShipper(username, password, name, phone, address)) {
                System.out.println("Register success!");
            } else System.out.println("Register failed!");
        }
    }

    private void displayCustomerMenu(Customer c) {
        while (true) {
            saveData();
            System.out.println("========= Customer menu =========");
            System.out.println("Hello " + c.getName() + "!");
            System.out.println("1. View/Update Profile");
            System.out.println("2. Buy/view your cart");
            System.out.println("3. View orders");
            System.out.println("4. Your shop");
            System.out.println("5. Deposit");
            System.out.println("6. Withdraw");
            System.out.println("7. Log out");
            System.out.println("================================");

            String choice = Utils.promptInput("Enter your choice: ");
            switch (choice) {
                case "1":
                    updateProfile(c);
                    break;
                case "2":
                    viewCart(c);
                    System.out.println("========= Buy/Cart menu =========");
                    System.out.println("1. Add item");
                    System.out.println("2. Remove item");
                    System.out.println("3. Pay");
                    System.out.println("4. Exit");

                    String select = Utils.promptInput("Enter your choice: ");
                    switch (select) {
                        case "1":
                            addItemToCart(c);
                            break;
                        case "2":
                            removeItemFromCart(c);
                            break;
                        case "3":
                            pay(c);
                            break;
                        default:
                            if (!select.equals("4")) System.out.println("INVALID");
                    }
                    break;
                case "3":
                    System.out.println("Your orders:");
                    List<Order> customerOrders = c.getOrders();
                    if (customerOrders.isEmpty()) {
                        System.out.println("Your don't have any order.");
                        continue;
                    }
                    for (Order order : customerOrders) {
                        System.out.println(order.toString());
                    }
                    String input =
                            Utils.promptInput("Enter order id to confirm or type 'all' to confirm all or 'exit' to exit: ");
                    if (input.equalsIgnoreCase("exit")) continue;
                    if (input.equalsIgnoreCase("all")) {
                        for (Order order : customerOrders) {
                            c.confirmOrder(order.getId(),SHOP_PORTION);
                            System.out.printf("Order %s confirmed successfully!", order.getId());
                        }
                    } else {
                        try {
                            int cartId = Integer.parseInt(input);
                            if (!c.confirmOrder(cartId, SHOP_PORTION)) {
                                System.out.println("Error on confirming order. Order might not be in delivered state.");
                            }
                            else{
                                System.out.printf("Order %s confirmed successfully!", cartId);
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("INVALID");
                        }
                    }
                    break;
                case "4":
                    if (c.getOwnedShop() == null) {
                        createShop(c);
                    } else {
                        displayShopMenu(c);
                    }
                    break;
                case "5":
                    double amount = deposit();
                    if (amount >= 0.0) {
                        c.addBalance(amount);
                        System.out.println("Deposit successfully!");
                    } else {
                        System.out.println("Deposit failed!");
                    }
                    break;
                case "6":
                    withdraw(c);
                    break;
                case "7":
                    return;
                default:
                    System.out.println("Invalid choice");
            }
        }
    }

    private void displayShipperMenu(Shipper s) {
        while (true) {
            saveData();
            System.out.println("========= Shipper menu =========");
            System.out.println("Hello " + s.getName() + "!");
            System.out.println("Your balance: " + s.getBalance());
            System.out.println("1. Receive order");
            System.out.println("2. Deliver order");
            System.out.println("3. View task");
            System.out.println("4. View/Update Profile");
            System.out.println("5. Withdraw");
            System.out.println("6. Log out");

            int choice = Utils.promptIntInput("Enter your choice: ").orElse(-1);

            switch (choice) {
                case 1:
                    takesOrder(s);
                    break;
                case 2:
                    shipperFinishesOrder(s);
                    break;
                case 3:
                    viewShipperTask(s);
                    break;
                case 4:
                    updateProfile(s);
                    break;
                case 5:
                    withdraw(s);
                    break;
                case 6:
                    return;
                default:
                    System.out.println("INVALID");
            }
        }
    }

    private void takesOrder(Shipper s) {
        System.out.println("----------Take order menu---------");

        List<Order> orders = getOrdersReadyToShip(s);
        HashMap<Integer,Order> id_order = new HashMap<>();
        if (orders.isEmpty()) {
            System.out.println("There is no order nearby to ship");
            return;
        }

        for (Order order : orders) {
            System.out.println(order.toString());
            id_order.put(order.getId(),order);
        }
        do {
            int id = Utils.promptIntInput("Enter order id you want to take: ").orElse(-1);
            if (!id_order.containsKey(id)){
                System.out.println("Invalid");
            }
            else{
                s.takesOrder(id_order.get(id));
            }
        } while (Utils.promptInput("Continue? (y/n) ").equalsIgnoreCase("y"));
    }

    private void shipperFinishesOrder(Shipper s) {
        System.out.println("----------Deliver order menu-----------");

        List<Order> orders = s.getOrders();

        if (orders.isEmpty()) {
            System.out.println("You did not take any order.");
            return;
        }
        for (Order order : orders) {
            System.out.println(order.toString());
        }
        do {
            int id = Utils.promptIntInput("Enter order id you want to deliver and finish: ").orElse(-1);
            if (!s.finishesOrder(id,SHIPPER_FEE)) System.out.println("Invalid");
        } while (Utils.promptInput("Continue? (y/n) ").equalsIgnoreCase("y"));
    }

    private void viewShipperTask(Shipper s) {
        System.out.println("------Shipping order--------");
        for (Order order : s.getOrders()) {
            System.out.println(order.toString());
        }
    }

    private void createShop(Customer c) {
        System.out.println("You didn't have a shop before.");
        if (!Utils.promptInput("Do you want create shop? (y/[n]) ").equalsIgnoreCase("y")) {
            return;
        }
        String shopName = Utils.promptInput("Enter your shop name: ");
        System.out.println("Enter your shop address: ");
        Address address = askForUpdateAddress().orElse(null);
        if (address != null) {
            c.setOwnedShop(new Shop(shopName, address));
            System.out.println("Create shop successfully");
        } else {
            System.out.println("Failed to create shop");
        }
    }

    private void displayShopMenu(Customer c) {
        while (true) {
            saveData();
            System.out.println("========= Shop menu =========");
            System.out.println("Your shop's revenue: " + c.getOwnedShop().getRevenue());
            System.out.println("1. Change Shop information");
            System.out.println("2. Add/Delete item");
            System.out.println("3. Take Orders");
            System.out.println("4. Transfer money to account");
            System.out.println("Other key to exit.");

            String choice = Utils.promptInput("Enter your choice: ");

            switch (choice) {
                case "1":
                    changeShopInfo(c.getOwnedShop());
                    break;
                case "2":
                    System.out.println("1. Add item");
                    System.out.println("2. Delete item");
                    int select = Utils.promptIntInput("Enter your choice: ").orElse(-1);
                    switch (select) {
                        case 1:
                            addItemToShop(c.getOwnedShop());
                            break;
                        case 2:
                            deleteItemFromShop(c.getOwnedShop());
                            break;
                        default:
                            System.out.println("INVALID");
                    }
                    break;
                case "3":
                    acceptOrderByShop(c.getOwnedShop());
                    break;
                case "4":
                    c.addBalance(c.getOwnedShop().getRevenue());
                    c.getOwnedShop().setRevenue(0);
                    System.out.println("Transfer successfully!");
                    break;
                default:
                    System.out.println("Exit shop menu.");
                    return;
            }
        }
    }

    private static void addItemToShop(Shop shop) {
        String itemName = Utils.promptInput("Enter item name: ");
        double price = Utils.promptIntInput("Enter item price: ").orElse(-1);
        if (price <= 0.0) System.out.println("Invalid price. Stop adding.");
        int quantity = Utils.promptIntInput("Enter item quantity: ").orElse(-1);
        if (quantity <= 0) System.out.println("Invalid quantity. Stop adding.");
        shop.addItem(itemName, price, quantity);
    }

    private static void deleteItemFromShop(Shop shop) {
        System.out.println("List of shop stock: ");
        for (ItemStock itemStock : shop.getStock()) {
            System.out.println(itemStock.toString());
        }
        int itemId = Utils.promptIntInput("Enter item id to remove: ").orElse(-1);
        if (shop.removeItem(itemId))
            System.out.println("Successfully removed item.");
        else
            System.out.println("Failed to remove item.");
    }

    private void acceptOrderByShop(Shop shop) {
        List<Order> ordersByThisShop = shop.getShopOrdersReadyToTake();
        if (ordersByThisShop.isEmpty()) {
            System.out.println("Your shop doesn't have any order.");
            return;
        }
        HashMap<Integer,Order> id_order = new HashMap<>();
        for (Order order : ordersByThisShop) {
            System.out.println("List of order by this shop:");
            System.out.println(order.toString());
            id_order.put(order.getId(),order);
        }

        do {
            int id = Utils.promptIntInput("Enter order id you prepared and want to accept: ").orElse(-1);

            if (id <= 0 || !id_order.containsKey(id)) System.out.println("Invalid id.");
            else shop.addOrder(id_order.get(id));
        } while (Utils.promptInput("Continue? (y/n): ").equalsIgnoreCase("y"));

    }

    private static void changeShopInfo(Shop shop) {
        System.out.println("1. Change shop name");
        System.out.println("2. Change shop address");
        String choice = Utils.promptInput("Enter option: ");

        switch (choice) {
            case "1":
                String shopName = Utils.promptInput("Enter new shop name: ");
                if (!shopName.isEmpty()) shop.setName(shopName);
                break;
            case "2":
                System.out.println("Enter new shop address: ");
                Address address = askForUpdateAddress().orElse(null);
                if (address != null)
                    shop.setAddress(address);
                else
                    System.out.println("Failed to update address.");
                break;
            default:
                System.out.println("INVALID");
        }
    }

    private void pay(Customer c) {
        double totalPrice = c.getCart().getTotalPrice();
        System.out.println("Total price: " + totalPrice);

        if (!Utils.promptInput("Are you sure you want to pay? (y/[n]) ").equalsIgnoreCase("y")) {
            return;
        }

        if (c.getBalance() < totalPrice) {
            System.out.println("Your balance is not enough");
            return;
        }

        SystemResponse orderState = createOrder(c);

        if (orderState.isSuccess()) {
            System.out.println("Order successfully");
        } else {
            System.out.println("Failed to create order due to: " + orderState.getMessage());
        }
    }

    private static double deposit() {
        System.out.println("STK: 00888888888");
        System.out.println("Name: CTCP SHOPDEE");
        System.out.println("Bank: VCB");
        System.out.println("Transfer message: username");
        System.out.println("===============================");
        return Utils.promptDoubleInput("Enter amount you want to deposit: ").orElse(-1.0);
    }

    private void withdraw(User u) {
        double amount = Utils.promptDoubleInput("Enter amount you want to withdraw: ").orElse(-1.0);
        System.out.println("Success. Amount was withdrawn: " + u.withdraw(amount));
    }

    private void updateProfile(User u) {

        System.out.println("Your profile:\n" + u.toString());
        System.out.println("Choose an action:");
        System.out.println("1. Change name");
        System.out.println("2. Change phone");
        System.out.println("3. Change address");
        System.out.println("4. Change password");
        System.out.println("Other key to quit.");

        String choice = Utils.promptInput("Enter your option: ");
        try {
            switch (choice) {
                case "1":
                    String name = Utils.promptInput("Enter new name: ");
                    u.setName(name);
                    break;
                case "2":
                    String phone = Utils.promptInput("Enter new phone: ");
                    u.setName(phone);
                    break;
                case "3":
                    Address address = askForUpdateAddress().orElse(null);
                    if (address != null)
                        u.setAddress(address);
                    else System.out.println("Failed to update address.");
                    break;
                case "4":
                    String password = Utils.promptInput("Enter new password: ");
                    u.setPassword(password);
                default:
                    System.out.println("Quit updating profile.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid input! Quit updating profile.");
        }
    }

    private static Optional<Address> askForUpdateAddress() {
        Address.City city;
        String choice = Utils.promptInput("Choose your city:\n 1.Ha Noi\n 2.Ho Chi Minh City\n 3.Hai Phong\n 4.Can Tho\n 5.Da Nang\n");
        switch (choice) {
            case "1":
                city = Address.City.HANOI;
                break;
            case "2":
                city = Address.City.HCMC;
                break;
            case "3":
                city = Address.City.HAIPHONG;
                break;
            case "4":
                city = Address.City.CANTHO;
                break;
            case "5":
                city = Address.City.DANANG;
                break;
            default:
                System.out.println("Invalid choice. The address will not be updated.");
                return Optional.empty();
        }
        String addressLine = Utils.promptInput("Enter address line: ");
        return Optional.of(new Address(addressLine, city));
    }

    private static void viewCart(Customer c) {
        if (c.getCart().isEmpty()) {
            System.out.println("Your cart is empty!");
            return;
        }
        System.out.println("Your Cart: ");
        for (CartItem item : c.getCart().getItems()) {
            System.out.println(item.toString());
        }
    }

    private void addItemToCart(Customer c) {
        System.out.println("========= Add to cart =========");
        System.out.println("1. View all items");
        System.out.println("2. Search item");
        System.out.println("3. Search shop");
        String choice = Utils.promptInput("Enter your choice: ");
        switch (choice) {
            case "1":
                viewAllItem();
                break;
            case "2":
                String itemName = Utils.promptInput("Enter product name: ");
                searchItem(itemName);
                break;
            case "3":
                String shopName = Utils.promptInput("Enter shop name: ");
                searchShop(shopName);
                break;
            default:
                System.out.println("Invalid option. Stop adding item to cart.");
                return;
        }
        while (true) {
            int id = Utils.promptIntInput("Enter product id you want to add: ").orElse(-1);
            if (id == -1) {
                System.out.println("Invalid id.");
                if (Utils.promptInput("Continue? (y/[n]) ").equalsIgnoreCase("y")) continue;
                else break;
            }
            ItemStock product = getProductById(id).orElse(null);
            if (product == null) {
                System.out.println("Cannot find the product. Item will not added");
                if (Utils.promptInput("Continue? (y/[n]) ").equalsIgnoreCase("y")) continue;
                else break;
            }
            int quantity = Utils.promptIntInput("Enter item quantity you want to add: ").orElse(-1);
            if (quantity <= 0) {
                System.out.println("Invalid quantity. Item is not added.");
                if (Utils.promptInput("Continue? (y/[n]) ").equalsIgnoreCase("y")) continue;
                else break;
            }
            if (!c.addToCart(product, quantity)) System.out.println("Add failed!!!");
            if (!Utils.promptInput("Continue adding product? (y/[n]) ").equalsIgnoreCase("y")) {
                break;
            }
        }
    }

    private static void removeItemFromCart(Customer c) {
        while (true) {
            int id = Utils.promptIntInput("Enter cart id you want to remove: ").orElse(-1);
            if (id == -1 || !c.getCart().existsInCart(id)) {
                System.out.println("Invalid id. Item will not remove.");
                if (Utils.promptInput("Continue? (y/[n]) ").equalsIgnoreCase("y")) continue;
                else break;
            }
            int quantity = Utils.promptIntInput("Enter quantity you want to remove: ").orElse(-1);
            if (quantity < 0) {
                System.out.println("Invalid quantity. Item will not remove.");
                if (Utils.promptInput("Continue? (y/[n]) ").equalsIgnoreCase("y")) continue;
                else break;
            }
            c.removeFromCart(id, quantity);
            if (!Utils.promptInput("Continue removing product? (y/[n]) ").equalsIgnoreCase("y")) {
                break;
            }
        }
    }

    private void viewAllItem() {
        for (Shop shop : getAllShops()) {
            List<ItemStock> items = shop.getStock();
            if (items.isEmpty()) {
                System.out.printf("Shop %s currently has no product.\n", shop.getName());
                continue;
            }
            System.out.println("- Shop: " + shop.getName());
            for (ItemStock item : items) {
                System.out.println(item.toString());
            }
        }
    }

    private void searchItem(String itemName) {
        List<ItemStock> result = findProducts(itemName);
        if (result.isEmpty()) {
            System.out.println("Currently there is no product with that name on our shopping mall.");
            return;
        }
        result.forEach(product -> System.out.println(product.toString()));
    }

    private void searchShop(String shopName) {
        List<Shop> result = findShops(shopName);
        if (result.isEmpty()) {
            System.out.println("Currently there is no shop with that name on our shopping mall.");
            return;
        }
        result.forEach(shop -> System.out.println(shop.toString()));
    }
    public List<ItemStock> findProducts(String productName) {
        ArrayList<ItemStock> result = new ArrayList<>();
        for (ItemStock itemStock : getAllItemStocks()) {
            if (itemStock.getItem().getName().contains(productName)) {
                result.add(itemStock);
            }
        }
        return result;
    }

    public Optional<ItemStock> getProductById(int id) {
        for (ItemStock itemStock : getAllItemStocks()) {
            if (itemStock.getId() == id) return Optional.of(itemStock);
        }
        return Optional.empty();
    }

    public List<Shop> findShops(String shopName) {
        ArrayList<Shop> result = new ArrayList<>();
        for (Shop shop : getAllShops()) {
            if (shop.getName().contains(shopName)) {
                result.add(shop);
            }
        }
        return result;
    }

    public List<ItemStock> getAllItemStocks() {
        List<ItemStock> res = new ArrayList<>();
        for (Shop shop : getAllShops()) {
            res.addAll(shop.getStock());
        }
        return res;
    }

    public List<Shop> getAllShops() {
        List<Shop> res = new ArrayList<>();
        for (User user : users.values()) {
            if (user.getRole() != UserRole.Customer) continue;
            Customer customer = (Customer) user;
            Shop shop = customer.getOwnedShop();
            if (shop != null)
                res.add(shop);
        }
        return res;
    }


    public List<Order> getOrdersReadyToShip(Shipper shipper) {
        return orders.stream()
                .filter(order ->
                        order.getOrderState().equals(OrderState.SHOP_ACCEPTED) || order.getOrderState().equals(OrderState.AT_WAREHOUSE)
                )
                .filter(order -> order.getLocation().city() == shipper.getAddress().city())
                .toList();
    }

    public SystemResponse createOrder(Customer customer) {
        if (customer == null) return new SystemResponse(false, "Invalid customer when creating order.");
        Cart cart;
        try {
            cart = customer.buy(); // get all items from cart then delete user cart
        } catch (Error e) {
            return new SystemResponse(false, "Not enough balance to make order.");
        }

        if (cart.isEmpty()) return new SystemResponse(false, "Empty cart.");

        // list all shop from all items in the cart
        HashSet<Shop> allShopFromCart = new HashSet<>();
        cart.getItems().forEach(cartItem -> allShopFromCart.add(cartItem.getItemStock().getShop()));

        // create a list of orders corresponding to the shop then add to the order list
        allShopFromCart.forEach(shop -> {
            OrderContent orderContent = OrderContent.filterFromCustomerCart(shop, cart);
            orders.add(new Order(customer, new Date(), orderContent));
        });

        // reduce the quantity in the shop when create order
        cart.getItems().forEach(cartItem -> {
            ItemStock itemStock = cartItem.getItemStock();
            itemStock.setQuantity(itemStock.getQuantity() - cartItem.getQuantity());
        });

        profit += cart.getTotalPrice() * PROFIT;
        return new SystemResponse(true, "Order successfully created.");
    }

    public boolean registerCustomer(String username, String password, String name, String phone, Address address) {
        if (username == null || password == null) return false;
        if (users.containsKey(username)) {
            return false;
        }
        users.put(username, new Customer(username, password, name, phone, address));
        saveData();
        return true;
    }

    public boolean registerShipper(String username, String password, String name, String phone, Address address) {
        if (username == null || password == null) return false;
        if (users.containsKey(username)) {
            return false;
        }
        users.put(username, new Shipper(username, password, name, phone, address));
        saveData();
        return true;
    }



    public boolean existsUser(String username) {
        return users.containsKey(username);
    }
}
