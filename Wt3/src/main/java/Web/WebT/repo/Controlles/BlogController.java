package Web.WebT.repo.Controlles;

import Web.WebT.models.Cart;
import Web.WebT.models.Catalog;
import Web.WebT.models.Orders;
import Web.WebT.models.User;
import Web.WebT.repo.CartRepository;
import Web.WebT.repo.CatalogRepository;
import Web.WebT.repo.OrderRepository;
import Web.WebT.repo.UserRepository;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import jakarta.servlet.http.HttpServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.ArrayList;
import java.util.Optional;

@Controller
public class BlogController extends HttpServlet {
    @Autowired
    private CatalogRepository catalogRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/registration")
    public String Registration(Model model) {
        return "registration";
    }

    @PostMapping("/registration")
    public String RegistrationEntry(@RequestParam String login, @RequestParam String password, Model model) {
        List<User> allUsers = (List<User>) userRepository.findAll();
        for (User user : allUsers) {
            if (user.getLogin().equals(login)) {
                return "authorization";
            }
        }

        User user = new User(login, password);

        userRepository.save(user);
        return "redirect:/";
    }

    @GetMapping("/cartAdd/{id}")
    public String Cart(@PathVariable(value = "id") long id, Model model) throws IOException, TimeoutException {
        Catalog product = catalogRepository.findById(id).orElseThrow();
        Cart cart = new Cart(product.getName(), product.getPrice());
        cartRepository.save(cart);

        Iterable<Cart> posts = cartRepository.findAll();
        model.addAttribute("posts", posts);
        return "cart";
    }

    @GetMapping("/cart")
    public String Cart(Model model) {
        Iterable<Cart> posts = cartRepository.findAll();
        model.addAttribute("posts", posts);
        return "cart";
    }

    @PostMapping("/cart")
    public String CartAdd(Model model) {
        return "cart";
    }

    @GetMapping("/order")
    public String OrderView(Model model) {
        return "order";
    }

    @GetMapping("/users")
    public String UserView(Model model) {
        Iterable<User> users = userRepository.findAll();
        List<User> userList = new ArrayList<>();
        users.forEach(userList::add);
        userList.remove(0);

        model.addAttribute("posts", userList);
        return "users";
    }

    @GetMapping("/users/{id}/delete")
    public String UserDelete(@PathVariable(value = "id") long id, Model model) {
        User user = userRepository.findById(id).orElseThrow();

        userRepository.delete(user);

        Iterable<User> users = userRepository.findAll();
        List<User> userList = new ArrayList<>();
        users.forEach(userList::add);
        userList.remove(0);

        model.addAttribute("posts", userList);
        return "users";
    }

    @PostMapping("/order")
    public String Order(@RequestParam String address, @RequestParam String number, @RequestParam String card, Model model) {
        List<Cart> allCartItems = (List<Cart>) cartRepository.findAll();
        List<Orders> ordersCopies = new ArrayList<>();
        for (Cart cartItem : allCartItems) {
            Orders ordersCopy = new Orders(cartItem.getName(),cartItem.getPrice(),address,number,card);
            ordersCopies.add(ordersCopy);
        }
        cartRepository.deleteAll();
        orderRepository.saveAll(ordersCopies);
        return "cart"; //Оформа заказа
    }


    @PostMapping("/cart/{id}/delete")
    public String CartDel(@PathVariable(value = "id") long id, Model model) {
        Cart cart = cartRepository.findById(id).orElseThrow();

        cartRepository.delete(cart);
        return "redirect:/cart";
    }

    @GetMapping("/authorization")
    public String Authorization(Model model) {
        cartRepository.deleteAll();
        return "authorization";
    }

    @PostMapping("/authorization")
    public String AuthorizationEntry(@RequestParam String login, @RequestParam String password, Model model) {
        List<User> allUsers = (List<User>) userRepository.findAll();
        for (User user : allUsers) {
            if (user.getLogin().equals(login) && user.getPassword().equals(password)) {
                if (Objects.equals(user.getId(), Long.valueOf(1)))
                {
                     return "redirect:/homeAdmin";
                }
                else
                {
                    String QUEUE_NAME = "Users";
                    ConnectionFactory factory = new ConnectionFactory();
                    factory.setHost("localhost");
                    try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
                        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                        String message = "User " + user.getLogin() + " join the service";
                        channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
                        System.out.println(" [x] Sent '" + message + "'");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (TimeoutException e) {
                        throw new RuntimeException(e);
                    }
                    return "redirect:/";
                }
            }
        }
        return "authorization";
    }

    @GetMapping("/blog/add")
    public String blogAdd(Model model) {
        return "blog-add";
    }

    @PostMapping("/blog/add")
    public String blogPostAdd(@RequestParam String title, @RequestParam String anons, @RequestParam String file_text, Model model) throws IOException, TimeoutException {
        Catalog post = new Catalog(title, anons, file_text);
        catalogRepository.save(post);
        return "redirect:/homeAdmin";
    }

    @GetMapping("/blog/{id}")
    public String blogDetails(@PathVariable(value = "id") long id, Model model) {
        if (!catalogRepository.existsById(id)) {
            return "blog-details";
        }
        Optional<Catalog> post = catalogRepository.findById(id);
        ArrayList<Catalog> res =  new ArrayList<>();
        post.ifPresent(res::add);
        model.addAttribute("post", res);
        return "blog-details";
    }

    @GetMapping("/blog/{id}/admin")
    public String blogDetailsAdmin(@PathVariable(value = "id") long id, Model model) {
        if (!catalogRepository.existsById(id)) {
            return "blog-details-admin";
        }
        Optional<Catalog> post = catalogRepository.findById(id);
        ArrayList<Catalog> res =  new ArrayList<>();
        post.ifPresent(res::add);
        model.addAttribute("post", res);
        return "blog-details-admin";
    }

    @GetMapping("/blog/{id}/edit")
    public String blogEdit(@PathVariable(value = "id") long id, Model model) {
        if (!catalogRepository.existsById(id)) {
            return "redirect:/blog";
        }
        Optional<Catalog> post = catalogRepository.findById(id);
        ArrayList<Catalog> res =  new ArrayList<>();
        post.ifPresent(res::add);
        model.addAttribute("post", res);
        return "blog-edit";
    }

    @PostMapping("/blog/{id}/edit")
    public String blogPostUpdate(@PathVariable(value = "id") long id, @RequestParam String title, @RequestParam String anons, @RequestParam String file_text, Model model) {
        Catalog post = catalogRepository.findById(id).orElseThrow();
        post.setName(title);
        post.setPrice(anons);
        post.setDescription(file_text);

        catalogRepository.save(post);
        return "redirect:/homeAdmin";
    }

    @PostMapping("/blog/{id}/remove")
    public String blogPostDelete(@PathVariable(value = "id") long id, Model model) {
        Catalog post = catalogRepository.findById(id).orElseThrow();
        catalogRepository.delete(post);
        return "redirect:/homeAdmin";
    }
}
