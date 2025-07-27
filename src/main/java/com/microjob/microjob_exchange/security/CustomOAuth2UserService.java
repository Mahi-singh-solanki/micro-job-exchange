package com.microjob.microjob_exchange.security;

import com.microjob.microjob_exchange.model.User;
import com.microjob.microjob_exchange.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException{

        //to fetch details from google
        OAuth2User oAuth2User=super.loadUser(userRequest);

        //extract email and name from google
        String email=oAuth2User.getAttribute("email");
        String name=oAuth2User.getAttribute("name");

        //if user not exist then create a new one
        Optional<User> userOptional=userRepository.findByEmail(email);
        if(userOptional.isEmpty()){
            User newUser=new User();
            newUser.setEmail(email);
            newUser.setName(name);
            newUser.setPassword("GOOGLE_OAUTH");//placeholder since it will be used as dummy password for google logins
            newUser.setRole("TASK_UPLOADER");
            userRepository.save(newUser);

        }
        return oAuth2User;
    }
}
