package model;

import model.enums.PurchaseStatus;
import model.exception.MenuNotFoundException;
import model.exception.ServiceNotFoundException;
import model.enums.Category;
import model.enums.OfficeDays;
import model.enums.OfficeHours;
import org.hibernate.usertype.UserVersionType;
import org.joda.time.LocalDate;
import persistence.UnityOfWork;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ViendasYaFacade {

    private UnityOfWork unityOfWork;

    public ViendasYaFacade(UnityOfWork unityOfWork) {
        this.unityOfWork = unityOfWork;
    }

    public List<SupplierUser> getSuppliers() {
        return unityOfWork.suppliers;
    }

    public List<SupplierUser> getInvalidSuppliers() {
        return unityOfWork.invalidSuppliers;
    }

    public void addSupplier(SupplierUser supplier) {
        unityOfWork.suppliers.add(supplier);
    }

    public void addCustomer(CustomerUser customer) {
        unityOfWork.customers.add(customer);
    }

    public void addService(SupplierUser supp, String serviceName, String icon, String addressTown, String addressLocation, String description, String email, String phoneNumber, List<OfficeDays> officeDays, List<OfficeHours> officeHours, int deliveryDistance) throws Exception {
        if (unityOfWork.isServiceAlreadyCreated(serviceName))
            throw new Exception("Service already exists. Please, select another name.");
        if (supp.hasService())
            throw new Exception("Supplier already has a service. Please, delete it before creating new one");

        supp.addService(serviceName, icon, new Address(addressTown, addressLocation), description, email, phoneNumber, officeDays, officeHours, deliveryDistance);
    }

    public void addService(SupplierUser supp, Service service) throws Exception {
        if (unityOfWork.isServiceAlreadyCreated(service.getServiceName()))
            throw new Exception("Service already exists. Please, select another name.");
        if (supp.hasService())
            throw new Exception("Supplier already has a service. Please, delete it before creating new one");

        supp.addService(service.getServiceName(), service.getIcon(), service.getAddress(), service.getDescription(), service.getEmail(), service.getPhoneNumber(), service.getOfficeDays(), service.getOfficeHours(), service.getDeliveryDistance());
    }

    public void deleteService(SupplierUser supp) throws Exception {
        if (!supp.hasService())
            throw new Exception("Supplier does not have any active service.");

        supp.deleteService();
    }

    public void addMenuToService(String serviceName, int id, String name, String description, Category category, int deliveryFee, LocalDate startDate, LocalDate endDate, OfficeHours deliveryHours, int averageDeliveryMinutes, int price, int minQuantity, int minQuantityPrice, int maxDailySales) {
        SupplierUser supplier = findSupplierWithServiceName(serviceName);

        if (supplier == null)
            throw new ServiceNotFoundException();

        supplier.addMenu(id, name, description, category, deliveryFee, startDate, endDate, deliveryHours, averageDeliveryMinutes, price, minQuantity, minQuantityPrice, maxDailySales);
    }

    public void addMenuToService(String serviceName, Menu menu) {
        SupplierUser supplier = findSupplierWithServiceName(serviceName);

        if (supplier == null)
            throw new ServiceNotFoundException();

        supplier.addMenu(menu.getMenuId(), menu.getName(), menu.getDescription(), menu.getCategory(), menu.getDeliveryFee(), menu.getStartDate(), menu.getEndDate(), menu.getDeliveryHours(), menu.getAverageDeliveryMinutes(), menu.getPrice(), menu.getMinQuantity(), menu.getMinQuantityPrice(), menu.getMaxDailySales());
    }

    public List<Service> getAllServices() {
        return unityOfWork.getAllServices();
    }

    public List<Purchase> getAllPurchases() {
        return unityOfWork.purchases;
    }

    public Purchase purchase(CustomerUser customer, String serviceName, int menuId, int quantity) throws Exception {
        Service currentService = findServiceByName(serviceName);
        if (currentService == null)
            throw new ServiceNotFoundException();

        Menu currentMenu = currentService.getMenuByMenuId(menuId);
        if (currentMenu == null)
            throw new MenuNotFoundException();

        if (currentMenu.getMinQuantity() > quantity)
            throw new Exception(String.format("You cannot buy less than %s units.", currentMenu.getMinQuantity()));

        List<Purchase> purchasedMenus = getAllPurchasesForMenu(currentService, currentMenu);
        if (purchasedMenus.size() >= currentMenu.getMaxDailySales())
            throw new Exception("Maximun number of sales per day have been reached for this menu.");

        if (!customer.getAccount().haveEnoughFunds(currentMenu.getPrice() * quantity))
            throw new Exception("Customer does not have enough funds to purchase that quantity.");

        if (customer.hasPendingPunctuations())
            throw new Exception("Customer has pending scores to punctuate before purchasing.");

        int purchasedAmount = currentMenu.getPrice() * quantity;
        customer.getAccount().extractMoney(purchasedAmount);
        currentService.getSupplier().getAccount().depositMoney(purchasedAmount);
        CustomerScore customerScore = customer.addDefaultScore(currentService, currentMenu);

        return unityOfWork.addPurchase(customer, customerScore.getCustomerScoreId(), currentService, currentMenu, LocalDate.now(), purchasedAmount);
    }

    public void startDeliveryForPurchase(int purchaseId) {
        Purchase purchase = unityOfWork.getPurchaseById(purchaseId);
        purchase.startDelivery();
        unityOfWork.savePurchase(purchase);
    }

    public void finishDeliveryForPurchase(int purchaseId) {
        Purchase purchase = unityOfWork.getPurchaseById(purchaseId);
        purchase.finishDelivery();
        unityOfWork.savePurchase(purchase);
    }

    public void createMenuScore(CustomerUser customer, String serviceName, int menuId, int punctuation) throws Exception {
        CustomerScore customerScore = customer.findUserScore(serviceName, menuId);
        if (customerScore == null)
            throw new Exception("User Score does not exists.");

        customerScore.setPunctuation(punctuation);
        customerScore.getMenu().addScore(customerScore.getCustomerName(), punctuation);

        if (customerScore.getMenu().hasEnoughScores() && customerScore.getMenu().getScoreAverage() < 2) {
            Service service = customerScore.getService();
            service.markMenuAsInvalid(customerScore.getMenu());

            if (service.getInvalidMenus().size() == 10) {
                markSupplierAsInvalid(service.getSupplier());
            }
        }
    }

    /* Private Methods */
    private void markSupplierAsInvalid(SupplierUser invalidSupplier) {
        unityOfWork.suppliers.remove(invalidSupplier);
        unityOfWork.invalidSuppliers.add(invalidSupplier);
    }

    private SupplierUser findSupplierWithServiceName(String serviceName) {
        return unityOfWork.suppliers.stream().filter(s -> s.getService().getServiceName().equals(serviceName)).findFirst().orElse(null);
    }

    private Service findServiceByName(String serviceName) {
        return getAllServices().stream().filter(s -> s.getServiceName().equals(serviceName)).findFirst().orElse(null);
    }

    private List<Purchase> getAllPurchasesForMenu(Service currentService, Menu currentMenu) {
        List<Purchase> todayPurchases = unityOfWork.purchases.stream().filter(p -> p.getPurchasedDate().equals(LocalDate.now())).collect(Collectors.toList());
        return todayPurchases.stream().filter(p -> p.getService().equals(currentService) && p.getPurchasedMenu().equals(currentMenu)).collect(Collectors.toList());
    }
}