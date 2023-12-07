package Web.WebT.repo.Controlles;

import Web.WebT.models.Catalog;
import Web.WebT.repo.CatalogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @Autowired
    private CatalogRepository catalogRepository;

    @GetMapping("/")
    public String home(Model model) {
        Iterable<Catalog> posts = catalogRepository.findAll();
        model.addAttribute("posts", posts);
        return "home";
    }

    @GetMapping("/homeAdmin")
    public String homeAdmin(Model model) {
        Iterable<Catalog> posts = catalogRepository.findAll();
        model.addAttribute("posts", posts);
        return "homeAdmin";
    }

}
