package backend.service;

import backend.controller.requests.NewScorePunctuationRequest;
import backend.controller.requests.PurchaseRequest;
import backend.model.*;
import backend.model.exception.ServiceNotFoundException;
import backend.model.exception.UserNotFoundException;
import backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class CustomerService {

    @Autowired
    private ICustomerRepository customerRepository;
    @Autowired
    private IServiceRepository serviceRepository;
    @Autowired
    private IPurchaseRepository purchaseRepository;
    @Autowired
    private ICustomerScoreRepository customerScoreRepository;
    @Autowired
    private CommunicationService communicationService;

    private ViendasYaFacade viendasYaFacade = new ViendasYaFacade();

    public Iterable<CustomerUser> getAllCustomers() {
        return customerRepository.findAll();
    }

    public CustomerUser getCustomerById(long customerId) {
        return customerRepository.findById(customerId).orElseThrow(() -> new UserNotFoundException(customerId));
    }

    public void deleteCustomer(long id) {
        customerRepository.deleteById(id);
    }

    public int depositMoney(long customerId, int money) {
        CustomerUser customer = customerRepository.findById(customerId).orElseThrow(() -> new UserNotFoundException(customerId));
        customer.getAccount().depositMoney(money);
        customerRepository.save(customer);
        return customer.getAccount().getFunds();
    }

    public Iterable<HistoricalPurchases> getCustomerPurchases(long customerId) {
        List<Purchase> customerPurchases = StreamSupport.stream(purchaseRepository.findAll().spliterator(), false).filter(p -> p.getCustomer().getId() == customerId).collect(Collectors.toList());
        return viendasYaFacade.getCustomerHistoricalPurchases(customerPurchases);
    }

    public Purchase purchaseMenu(PurchaseRequest purchaseRequest) throws Exception {
        CustomerUser customer = customerRepository.findById(purchaseRequest.getCustomerId()).orElseThrow(() -> new UserNotFoundException(purchaseRequest.getCustomerId()));
        backend.model.Service service = serviceRepository.findById(purchaseRequest.getServiceId()).orElseThrow(ServiceNotFoundException::new);
        Menu menu = service.getMenuByMenuId(purchaseRequest.getMenuId());

        List<Purchase> purchasedMenus = StreamSupport.stream(purchaseRepository.findAll().spliterator(), false).filter(p -> p.getPurchasedMenu() == menu).collect(Collectors.toList());
        if (purchasedMenus.size() >= menu.getMaxDailySales())
            throw new Exception("Maximun number of sales per day have been reached for this menu.");

        CustomerScore customerScore = new CustomerScore(customer.getEmail(), service.getServiceId(), menu.getMenuId());
        Purchase purchase = viendasYaFacade.purchaseMenu(customer, service, menu, purchaseRequest.getQuantity(), customerScore);
        customerScoreRepository.save(customerScore);
        customerRepository.save(customer);
        serviceRepository.save(service);
        purchaseRepository.save(purchase);

        return purchase;
    }

    public MenuScore createScoreForMenu(NewScorePunctuationRequest newScorePunctuationRequest) throws Exception {
        CustomerUser customer = getCustomerById(newScorePunctuationRequest.getCustomerId());
        backend.model.Service service = serviceRepository.findById(newScorePunctuationRequest.getServiceId()).orElseThrow(ServiceNotFoundException::new);
        Menu menu = service.getMenuByMenuId(newScorePunctuationRequest.getMenuId());

        MenuScore menuScore = viendasYaFacade.createMenuScore(customer, service, menu, newScorePunctuationRequest.getPunctuation());
        viendasYaFacade.checkMenuAndServiceValidity(service, menu);

        if (!menu.isValidMenu())
            communicationService.sendInvalidMenuEmail(service.getSupplier().getEmail(), "You have lost a menu!", menu.getName(), menu.getScoreAverage());
        if (!service.isValidService())
            communicationService.sendInvalidServiceEmail(service.getSupplier().getEmail(), "Oh man, your service sucks!", service.getServiceName());

        serviceRepository.save(service);
        customerRepository.save(customer);

        return menuScore;
    }
}
