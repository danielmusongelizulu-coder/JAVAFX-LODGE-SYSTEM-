public class Bill {
    private final String billId;
    private final String guestName;
    private final String roomType;
    private final int nights;
    private final double total;
    private final double paid;
    private final double balance;

    public Bill(String billId, String guestName, String roomType, int nights, double total, double paid, double balance) {
        this.billId = billId;
        this.guestName = guestName;
        this.roomType = roomType;
        this.nights = nights;
        this.total = total;
        this.paid = paid;
        this.balance = balance;
    }

    public String getBillId() { return billId; }
    public String getGuestName() { return guestName; }
    public String getRoomType() { return roomType; }
    public int getNights() { return nights; }
    public String getTotal() { return String.format("K %.2f", total); }
    public String getPaid() { return String.format("K %.2f", paid); }
    public String getBalance() { return String.format("K %.2f", balance); }
    public double getTotalAmount() { return total; }
    public double getPaidAmount() { return paid; }
    public double getBalanceAmount() { return balance; }
}