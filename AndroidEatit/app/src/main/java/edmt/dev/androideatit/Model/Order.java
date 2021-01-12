package edmt.dev.androideatit.Model;

/**
 * Created by Minhaz on 2/14/2018.
 */

public class Order {
    private String ProductId;
    private String ProductName;
    private String Quantity;
    private String Price;
    private String Discount;

    public Order() {
    }

    public Order(String PproductId, String PproductName, String Pquantity, String Pprice, String Pdiscount) {
        ProductId = PproductId;
        ProductName = PproductName;
        Quantity = Pquantity;
        Price = Pprice;
        Discount = Pdiscount;
    }

    public String getProductId() {
        return ProductId;
    }

    public void setProductId(String PproductId) {
        ProductId = PproductId;
    }

    public String getProductName() {
        return ProductName;
    }

    public void setProductName(String PproductName) {
        ProductName = PproductName;
    }

    public String getQuantity() {
        return Quantity;
    }

    public void setQuantity(String pquantity) {
        Quantity = pquantity;
    }

    public String getPrice() {
        return Price;
    }

    public void setPrice(String Pprice) {
        Price = Pprice;
    }

    public String getDiscount() {
        return Discount;
    }

    public void setDiscount(String Pdiscount) {
        Discount = Pdiscount;
    }
}
