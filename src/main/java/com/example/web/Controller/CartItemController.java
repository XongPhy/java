package com.example.web.Controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


import com.example.web.Model.CartItem;
import com.example.web.Model.Order;
import com.example.web.Model.Product;
import com.example.web.Model.Size;
import com.example.web.Model.User;
import com.example.web.Repositories.OrderRepository;
import com.example.web.Repositories.SizeRepository;
import com.example.web.Services.CartService;
import com.example.web.Services.ProductServices;
import com.example.web.Services.UserService;

@Controller
public class CartItemController {
	
	@Autowired
	private CartService cartService;
	
	@Autowired
	private ProductServices productService;

	@Autowired
	private UserService userService;
	
	@Autowired
	private SizeRepository sizeRepository;
	
	@Autowired
	private OrderRepository orderRepository;
	
	@GetMapping("/cart")
    public String viewCart(Model model, Principal principal) {
		String username = principal.getName();
        User user = userService.findByUsername(username);
        List<CartItem> cartItems = cartService.getCartItems(user);
        Long totalPrice = cartService.getTotalPrice(cartItems);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("cartItemCount", cartItems.size());
        return "products/cart";
    }
	
	@PostMapping("/cart/add")
	public String addToCart(@RequestParam("productId") Long id,@RequestParam("sizeId") Long sizeId,  Principal principal) {
		String username = principal.getName(); 
		User user = userService.findByUsername(username); 
		 if (user != null) {
			 Product product = productService.get(id);
			 Size size = sizeRepository.findById(sizeId).orElse(null);
		        if (product != null && size != null) {
		            cartService.addToCart(user, product,size);
		        }
		 }
		return "redirect:/cart";
	}
	
    @GetMapping("/cart/remove/{id}")
    public String removeFromCart(@PathVariable Long id) {
        cartService.removeFromCart(id);
        return "redirect:/cart";
    }

    
    @PostMapping("/cart/update/{id}")
    public String updateCartItem(@PathVariable Long id, @RequestParam("quantity") int quantity) {
        cartService.updateQuantity(id, quantity);
        return "redirect:/cart";
    }
    
    @GetMapping("/cart/place-order")
	public String checkoutCart(Model model, Principal principal) {
		String username = principal.getName();
		User user = userService.findByUsername(username);
		List<CartItem> cartItems = cartService.getCartItems(user);
		Long totalPrice = cartService.getTotalPrice(cartItems);
		model.addAttribute("totalPrice", totalPrice);
		model.addAttribute("cartItems", cartItems);
		return "products/place-order";
	}
    
    @PostMapping("/place-order/checkout")
    public String placeOrder(Model model, Principal principal,Product product) {
    	String username = principal.getName();
        User user = userService.findByUsername(username);
        List<CartItem> cartItems = cartService.getCartItems(user);
    	for (CartItem cartItem : cartItems) {
    		Order order = new Order();
            order.setUserId(user.getId());
            order.setName(cartItem.getName());
            order.setBrand(cartItem.getBrand());
            order.setPrice(cartItem.getPrice());
            order.setQuantity(cartItem.getQuantity());
            order.setImageUrl(cartItem.getImageUrl());
            order.setTotal(cartItem.getTotal());
            order.setProductId(cartItem.getId());
            orderRepository.save(order);
        }
    	
        for (CartItem cartItem : cartItems) {
            cartService.removeFromCart(cartItem.getId());
        }
        return "redirect:/place-order/success";
    }
    
    @GetMapping("/place-order/success")
    public String orderSuccess() {
        return "products/payment-success";
    }
}
