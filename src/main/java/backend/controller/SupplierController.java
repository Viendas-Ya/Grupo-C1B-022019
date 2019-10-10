package backend.controller;

import backend.controller.requests.NewServiceRequest;
import backend.controller.requests.NewUserRequest;
import backend.model.Service;
import backend.model.SupplierUser;
import backend.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class SupplierController {

    @Autowired
    private SupplierService supplierService = new SupplierService();

    @RequestMapping(value = "/supplier", method = RequestMethod.POST)
    public SupplierUser createSupplier(@Valid @RequestBody NewUserRequest supplier) {
        return supplierService.createSupplier(supplier);
    }

    @RequestMapping(value = "/supplier", method = RequestMethod.GET)
    public Iterable<SupplierUser> getAllSuppliers() {
        return supplierService.getAllSuppliers();
    }

    @RequestMapping(value = "/supplier", method = RequestMethod.DELETE)
    public void deleteSupplier(long id) {
        supplierService.deleteSupplier(id);
    }

    @RequestMapping(value = "/supplier/extractMoney", method = RequestMethod.GET)
    public int extractMoney(long supplierId, int money) {
        return supplierService.extractMoney(supplierId, money);
    }

    @RequestMapping(value = "/supplier/addService", method = RequestMethod.POST)
    public void addService(@Valid @RequestBody NewServiceRequest newServiceRequest) throws Exception {
        supplierService.addService(newServiceRequest);
    }

    @RequestMapping(value = "/supplier/getSupplierService", method = RequestMethod.GET)
    public Service getSupplierService(long supplierId) {
        return supplierService.getSupplierService(supplierId);
    }

    @RequestMapping(value = "/supplier/deleteService", method = RequestMethod.GET)
    public void deleteService(long supplierId) {
        supplierService.deleteService(supplierId);
    }
}
