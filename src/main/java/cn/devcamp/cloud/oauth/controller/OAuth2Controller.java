package cn.devcamp.cloud.oauth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;

@Controller
@SessionAttributes("authorizationRequest")
public class OAuth2Controller {

    @RequestMapping("/login")
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String login() {
        return "login";
    }

    @RequestMapping("/oauth/confirm_access")
    public ModelAndView getAccessConfirmation(Map<String, Object> model, HttpServletRequest request) {
        AuthorizationRequest authorizationRequest = (AuthorizationRequest) model.get("authorizationRequest");
        Set<String> scopes = authorizationRequest.getScope();
        String clientId = authorizationRequest.getClientId();
        String redirectUri = authorizationRequest.getRedirectUri();
        ModelAndView modelAndView = new ModelAndView("authorize");
        modelAndView.addObject("scopes", scopes);
        modelAndView.addObject("clientId", clientId);
        modelAndView.addObject("redirectUri", redirectUri);
        return modelAndView;
    }

}
