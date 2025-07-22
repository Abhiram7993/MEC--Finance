package com.example.finance.controller;

import com.example.finance.dto.StockQuote;
import com.example.finance.model.Holding;
import com.example.finance.model.Transaction;
import com.example.finance.model.User;
import com.example.finance.service.PortfolioService;
import com.example.finance.service.StockApiService;
import com.example.finance.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Controller
public class AppController {

    private final UserService userService;
    private final PortfolioService portfolioService;
    private final StockApiService stockApiService;

    public AppController(UserService userService, PortfolioService portfolioService, StockApiService stockApiService) {
        this.userService = userService;
        this.portfolioService = portfolioService;
        this.stockApiService = stockApiService;
    }

    private User getCurrentUser(UserDetails userDetails) {
        return userService.findByUsername(userDetails.getUsername());
    }

    @GetMapping("/")
    public String index(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        Set<Holding> holdings = portfolioService.getUserHoldingsWithCurrentPrices(user);
        BigDecimal totalStockValue = holdings.stream()
                .map(Holding::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCash = user.getCash().add(totalStockValue);

        model.addAttribute("holdings", holdings);
        model.addAttribute("cash", user.getCash());
        model.addAttribute("totalCash", totalCash);
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam String confirmation,
                               RedirectAttributes redirectAttributes) {
        if (username.isBlank() || password.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Username and password cannot be blank.");
            return "redirect:/register";
        }
        if (!password.equals(confirmation)) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match.");
            return "redirect:/register";
        }
        try {
            userService.registerUser(username, password);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please log in.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/quote")
    public String quoteForm() {
        return "quote";
    }

    @PostMapping("/quote")
    public String getQuote(@RequestParam String symbol, Model model, RedirectAttributes redirectAttributes) {
        if (symbol.isBlank()) {
             redirectAttributes.addFlashAttribute("error", "Symbol cannot be empty.");
             return "redirect:/quote";
        }
        StockQuote quote = stockApiService.lookup(symbol);
        if (quote == null) {
            redirectAttributes.addFlashAttribute("error", "Invalid symbol.");
            return "redirect:/quote";
        }
        model.addAttribute("stock", quote);
        return "quoted";
    }

    @GetMapping("/buy")
    public String buyForm() {
        return "buy";
    }

    @PostMapping("/buy")
    public String buyStock(@RequestParam String symbol, @RequestParam int shares,
                           @AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes) {
        if (symbol.isBlank() || shares < 1) {
            redirectAttributes.addFlashAttribute("error", "Please provide a valid symbol and positive number of shares.");
            return "redirect:/buy";
        }
        User user = getCurrentUser(userDetails);
        try {
            portfolioService.buyStock(user, symbol, shares);
            redirectAttributes.addFlashAttribute("success", "Bought!");
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/buy";
        }
    }

    @GetMapping("/sell")
    public String sellForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        Set<Holding> holdings = portfolioService.getUserHoldingsWithCurrentPrices(user);
        model.addAttribute("holdings", holdings);
        return "sell";
    }

    @PostMapping("/sell")
    public String sellStock(@RequestParam String symbol, @RequestParam int shares,
                            @AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes) {
        if (symbol.isBlank() || shares < 1) {
            redirectAttributes.addFlashAttribute("error", "Please select a stock and provide a positive number of shares.");
            return "redirect:/sell";
        }
        User user = getCurrentUser(userDetails);
        try {
            portfolioService.sellStock(user, symbol, shares);
            redirectAttributes.addFlashAttribute("success", "Sold!");
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/sell";
        }
    }

    @GetMapping("/history")
    public String history(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        List<Transaction> transactions = portfolioService.getTransactionHistory(user);
        model.addAttribute("transactions", transactions);
        return "history";
    }
}