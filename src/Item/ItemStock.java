package Item;

import Shop.Shop;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


import java.util.Objects;

public class ItemStock {
    private final int id;
    
    private final Item item;
    private double price;
    private int quantity;
    @JsonBackReference
    private Shop shop;

    private static int currentId = 30_000; // range: 30_000 -> 39_999

    @JsonCreator
    private ItemStock(
            @JsonProperty("id") int id,
            @JsonProperty("item")  Item item,
            @JsonProperty("price") double price,
            @JsonProperty("quantity") int quantity,
            @JsonProperty("shop") Shop shop
    ) {
        this.item = item;
        this.price = price;
        this.quantity = quantity;
        this.shop = shop;
        this.id = id;
        if (id > currentId) {
            currentId = id;
        }
    }

    public ItemStock( Item item, double price, int quantity,  Shop shop) {
        this.item = item;
        this.price = price;
        this.quantity = quantity;
        this.shop = shop;
        id = ++currentId;
        if ((currentId - 39_999) % 100_000 == 0) {
            currentId += 100_000 - 9999;
        }
    }

    public int getId() {
        return id;
    }

    public  Item getItem() {
        return item;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Shop getShop() {
        return shop;
    }

    public void setShop( Shop shop) {
        this.shop = shop;
    }

    @Override
    public String toString() {
        String shopName = shop == null ? "null" : shop.getName();
        return "ItemStock:" +
                "\nid: " + id +
                "\nitem name: " + item.getName() +
                "\nprice: " + price +
                "\nquantity: " + quantity +
                "\nshop: " + shopName + '\n';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemStock itemStock)) return false;
        return id == itemStock.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
