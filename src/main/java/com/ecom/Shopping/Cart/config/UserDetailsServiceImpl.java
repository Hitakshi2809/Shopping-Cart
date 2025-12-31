package com.ecom.Shopping.Cart.config;

import com.ecom.Shopping.Cart.model.UserDtls;
import com.ecom.Shopping.Cart.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
   private UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDtls user = userRepository.findByEmail(username);

        if(user == null){
            throw new UsernameNotFoundException("userNot Found");
        }
        return new CustomUser(user);
    }
}
