package com.marinboy.user.controller;

import com.marinboy.user.dto.LoginUserDto;
import com.marinboy.user.dto.UserDto;
import com.marinboy.user.service.UserService;
import com.marinboy.user.tool.LoginSessionTool;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 회원 가입, 로그인, 로그아웃, 내 정보 수정 요청을 담당합니다.
 */
@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final LoginSessionTool loginSessionTool;

    public UserController(UserService userService, LoginSessionTool loginSessionTool) {
        this.userService = userService;
        this.loginSessionTool = loginSessionTool;
    }

    @GetMapping("/insertForm")
    public String insertForm(Model model) {
        if (!model.containsAttribute("userDto")) {
            model.addAttribute("userDto", new UserDto());
        }

        return "user/insertForm";
    }

    /**
     * 일반 폼 전송이므로 @ModelAttribute로 회원 입력값을 받습니다.
     */
    @PostMapping("/insert")
    public String insert(@Valid @ModelAttribute UserDto userDto,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "user/insertForm";
        }

        try {
            userService.signup(userDto);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            return "user/insertForm";
        }

        redirectAttributes.addFlashAttribute("successMessage", "회원 가입이 완료되었습니다. 로그인해 주세요.");
        return "redirect:/user/loginForm";
    }

    @GetMapping("/loginForm")
    public String loginForm(@RequestParam(required = false) String returnTo, Model model) {
        model.addAttribute("returnTo", getSafeReturnTo(returnTo));
        return "user/loginForm";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        @RequestParam(required = false) String returnTo,
                        HttpServletRequest request,
                        Model model) {
        UserDto userDto = userService.login(email, password);

        if (userDto == null) {
            model.addAttribute("errorMessage", "이메일 또는 비밀번호를 확인해 주세요.");
            model.addAttribute("email", email);
            model.addAttribute("returnTo", getSafeReturnTo(returnTo));
            return "user/loginForm";
        }

        // 로그인 전 세션을 버리고 새 세션을 발급해 세션 고정 공격을 막습니다.
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        HttpSession newSession = request.getSession(true);
        loginSessionTool.saveLoginUser(newSession, userDto);
        return "redirect:" + getSafeReturnTo(returnTo);
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        loginSessionTool.clearLoginUser(session);
        return "redirect:/";
    }

    @GetMapping("/detail")
    public String detail(HttpSession session, Model model) {
        LoginUserDto loginUserDto = loginSessionTool.getLoginUser(session);

        if (loginUserDto == null) {
            return "redirect:/user/loginForm?returnTo=/user/detail";
        }

        UserDto userDto = userService.getUser(loginUserDto.getId());
        model.addAttribute("userDto", userDto);
        return "user/detail";
    }

    @PostMapping("/update")
    public String update(@RequestParam String name,
                         @RequestParam String phone,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        LoginUserDto loginUserDto = loginSessionTool.getLoginUser(session);

        if (loginUserDto == null) {
            return "redirect:/user/loginForm?returnTo=/user/detail";
        }

        UserDto userDto = new UserDto();
        userDto.setId(loginUserDto.getId());
        userDto.setName(name);
        userDto.setPhone(phone);
        userService.update(userDto);

        UserDto updatedUserDto = userService.getUser(loginUserDto.getId());
        loginSessionTool.saveLoginUser(session, updatedUserDto);

        redirectAttributes.addFlashAttribute("successMessage", "회원 정보가 수정되었습니다.");
        return "redirect:/user/detail";
    }

    /**
     * 외부 사이트로 강제 이동시키는 주소가 들어오지 못하게 내부 경로만 허용합니다.
     */
    private String getSafeReturnTo(String returnTo) {
        if (returnTo == null || returnTo.isBlank()) {
            return "/";
        }

        if (!returnTo.startsWith("/") || returnTo.startsWith("//")) {
            return "/";
        }

        return returnTo;
    }
}
