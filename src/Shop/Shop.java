package Shop;

import Item.Item;
import Item.ItemStock;
import Order.Order;
import Order.OrderState;
import User.Customer;
import Utils.Address;
import com.fasterxml.jackson.annotation.*;


import java.util.ArrayList;
import java.util.List;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
public class Shop {
    private final int id;
    
    private String name;

    @JsonManagedReference
    private final List<ItemStock> stock;


    @JsonIgnore
    private List<Order> orders;

    private double revenue;
    
    private Address address;

    private static int currentId = 40_000; // range 40_000 - 49_999

    @JsonCreator
    private Shop(
            @JsonProperty("id") int id,
            @JsonProperty("name")  String name,
            @JsonProperty("stock") List<ItemStock> stock,
            @JsonProperty("revenue") double revenue,
            @JsonProperty("address")  Address address
    ){
        this.id = id;
        this.name = name;
        this.stock = stock;
        this.revenue = revenue;
        this.address = address;
        this.orders = new ArrayList<>();
        for (ItemStock itemStock : stock) {
            if (itemStock.getShop() == null)
                itemStock.setShop(this);
        }
        if (id > currentId) currentId = id;
    }

    public Shop(String name,  Address address) {
        id = ++currentId;
        if ((currentId - 49_999) % 100_000 == 0) {
            currentId += 100_000 - 9999;
        }
        this.name = name;
        this.revenue = 0.0;
        this.address = address;
        this.stock = new ArrayList<>();
        this.orders = new ArrayList<>();
    }


    public  String getName() {
        return name;
    }

    public void setName( String name) {
        this.name = name;
    }

    public List<ItemStock> getStock() {
        return stock;
    }

    public int getId() {
        return id;
    }

    public double getRevenue() {
        return revenue;
    }

    public  Address getAddress() {
        return address;
    }

    public void setAddress( Address address) {
        this.address = address;
    }

    public void setRevenue(double revenue) {
        this.revenue = Math.max(revenue, 0.0);
    }

    public void increaseRevenue(double amount) {
        revenue += amount;
    }

    public List<Order> takeShopOrdersReadyToTake() {
        return this.orders.stream()
                .filter(order ->order.getOrderState().equals(OrderState.CREATED))
                .toList();
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void addOrder(Order order) {
        this.orders.add(order);
    }

    public void withdraw(Customer customer, double amount) {
        if (customer.getOwnedShop().equals(this)) { // check customer to ensure only shop owner can withdraw
            revenue -= amount;
            customer.addBalance(amount);
        }
    }

    public void addItem(String itemName, double price, int quantity) {
        this.stock.add(new ItemStock(new Item(itemName), price, quantity, this));
    }

    public boolean removeItem(int itemId) {
        for (int i = 0; i < stock.size(); i++) {
            if (stock.get(i).getId() == itemId) {
                stock.remove(i);
                return true;
            }
        }
        return false;
    }

    public void acceptOrder(Order order) {
        if (order != null && order.getShop() == this && order.getOrderState() == OrderState.CREATED) {
            order.setOrderState(OrderState.SHOP_ACCEPTED);
        }
    }

    @Override
    public String toString() {
        return "Shop: " +
                "\nname='" + name + '\'' +
                "\naddress=" + address +
                "\nItems are selling:\n" + stockToString() + "-------------------------\n";
    }

    private String stockToString() {
        StringBuilder sb = new StringBuilder();
        for (ItemStock itemStock : stock) {
            sb.append(itemStock.toString()).append("\n");
        }
        return sb.toString();
    }
}
