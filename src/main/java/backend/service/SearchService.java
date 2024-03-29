package backend.service;

import backend.controller.requests.SearchRequest;
import backend.model.Menu;
import backend.model.Search;
import backend.repository.IMenuRepository;
import backend.repository.IServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class SearchService {

    @Autowired
    private IServiceRepository serviceRepository;
    @Autowired
    private IMenuRepository menuRepository;

    private Search search = new Search();

    public Iterable<Menu> searchValidMenusByCriteria(SearchRequest searchRequest) {
        List<backend.model.Service> services = StreamSupport.stream(serviceRepository.findAll().spliterator(), false).filter(backend.model.Service::isValidService).collect(Collectors.toList());
        List<Menu> menus = StreamSupport.stream(menuRepository.findAll().spliterator(), false).filter(Menu::isValidMenu).collect(Collectors.toList());
        return search.search(services, menus, searchRequest.getMenuName(), searchRequest.getMenuCategory(), searchRequest.getServiceTown());
    }
}
