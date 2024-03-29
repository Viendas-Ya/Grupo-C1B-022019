package backend.model;

import backend.controller.helpers.LocalDateSerializer;
import backend.model.enums.PurchaseStatus;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.joda.time.LocalDate;

import javax.persistence.*;

@Entity
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PURCHASE_ID")
    private Long purchaseId;
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate purchasedDate;
    private int purchaseAmount;
    @Enumerated(EnumType.STRING)
    private PurchaseStatus purchaseStatus;

    @JoinColumn(name = "CUSTOMER_SCORE_ID")
    @OneToOne
    private CustomerScore customerScore;
    @JoinColumn(name = "ID")
    @OneToOne
    private CustomerUser customer;
    @JoinColumn(name = "SERVICE_ID")
    @OneToOne
    private Service service;
    @JoinColumn(name = "MENU_ID")
    @OneToOne
    private Menu purchasedMenu;

    public CustomerScore getCustomerScore() {
        return customerScore;
    }

    public int getPurchaseAmount() {
        return purchaseAmount;
    }

    public PurchaseStatus getPurchaseStatus() {
        return purchaseStatus;
    }

    public Long getPurchaseId() {
        return purchaseId;
    }

    public CustomerUser getCustomer() {
        return customer;
    }

    public Service getService() {
        return service;
    }

    public Menu getPurchasedMenu() {
        return purchasedMenu;
    }

    public LocalDate getPurchasedDate() {
        return purchasedDate;
    }

    public void setPurchaseId(Long purchaseId) {
        this.purchaseId = purchaseId;
    }

    public void setCustomer(CustomerUser customer) {
        this.customer = customer;
    }

    public void setCustomerScore(CustomerScore customerScore) {
        this.customerScore = customerScore;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public void setPurchasedMenu(Menu purchasedMenu) {
        this.purchasedMenu = purchasedMenu;
    }

    public void setPurchasedDate(LocalDate purchasedDate) {
        this.purchasedDate = purchasedDate;
    }

    public void setPurchaseAmount(int purchaseAmount) {
        this.purchaseAmount = purchaseAmount;
    }

    public void setPurchaseStatus(PurchaseStatus purchaseStatus) {
        this.purchaseStatus = purchaseStatus;
    }

    public void startDelivery() {
        this.purchaseStatus = PurchaseStatus.InDelivery;
    }

    public void finishDelivery() {
        this.purchaseStatus = PurchaseStatus.Finished;
    }

    public Purchase() {}

    public Purchase(CustomerUser customer, CustomerScore customerScore, Service service, Menu purchasedMenu, LocalDate dateOfPurchase, int purchaseAmount) {
        this.customer = customer;
        this.customerScore = customerScore;
        this.service = service;
        this.purchasedMenu = purchasedMenu;
        this.purchasedDate = dateOfPurchase;
        this.purchaseAmount = purchaseAmount;
        this.purchaseStatus = PurchaseStatus.InProgress;
    }
}
