package cn.devcamp.cloud.oauth.service;

import cn.devcamp.cloud.oauth.entity.SimpleAuthority;
import cn.devcamp.cloud.oauth.entity.SimpleUser;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SimpleUser simpleUser = new SimpleUser();
        simpleUser.setUsername(username);
        simpleUser.setPassword(passwordEncoder.encode("root"));

        Set<SimpleAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleAuthority("ROLE_ROOT"));
        simpleUser.setAuthorities(authorities);
        return simpleUser;
    }

}
