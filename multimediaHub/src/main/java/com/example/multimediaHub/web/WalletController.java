package com.example.multimediaHub.web;

import com.example.multimediaHub.config.UserData;
import com.example.multimediaHub.model.User;

import com.example.multimediaHub.service.UserService;
import com.example.multimediaHub.web.dto.Register;
import com.example.multimediaHub.web.dto.WalletDto;
import com.example.multimediaHub.web.enums.WalletBackground;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;


@Controller
@RequestMapping("/wallet")
public class WalletController {

    private final UserService userService;

    public WalletController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String wallet(Model model) {
        if (!model.containsAttribute("walletDto")) {
            model.addAttribute("walletDto", new WalletDto());
        }
        model.addAttribute("backgroundImage", WalletBackground.random().getImageName());
        return "wallet";
    }

    @PostMapping
    public String chargeWallet(@AuthenticationPrincipal UserData userDetails,
                               @Valid @ModelAttribute("walletDto") WalletDto walletDto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "org.springframework.validation.BindingResult.walletDto", bindingResult);
            redirectAttributes.addFlashAttribute("walletDto", walletDto);
            return "redirect:/wallet";
        }

        // Вече подаваме чистата сума от новото DTO
        userService.chargeWallet(userDetails.getUsername(), walletDto.getAmount());

        return "redirect:/home";
    }
}