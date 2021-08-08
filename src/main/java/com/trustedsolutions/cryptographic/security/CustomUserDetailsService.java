package com.trustedsolutions.cryptographic.security;

import com.trustedsolutions.cryptographic.exception.ResourceNotFoundException;
import com.trustedsolutions.cryptographic.model.Privilege;
import com.trustedsolutions.cryptographic.model.Role;
import com.trustedsolutions.cryptographic.model.User;
import com.trustedsolutions.cryptographic.repository.RoleRepository;
import com.trustedsolutions.cryptographic.repository.UserRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by rajeevkumarsingh on 02/08/17.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(()
                        -> new UsernameNotFoundException("User not found with email : " + email)
                );

           
        return new UserPrincipal(user.getId(), user.getEmail(), user.getPassword(), this.getAuthorities(user.getRoles()));
    }

    @Transactional
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", id)
        );

      
        return new UserPrincipal(user.getId(), user.getEmail(), user.getPassword(), this.getAuthorities(user.getRoles()));
    }

    private Collection<? extends GrantedAuthority> getAuthorities(
            Collection<Role> roles) {

        return getGrantedAuthorities(getPrivileges(roles));
    }

    private List<String> getPrivileges(Collection<Role> roles) {

        List<String> privileges = new ArrayList<>();
        List<Privilege> collection = new ArrayList<>();
        for (Role role : roles) {
         
            privileges.add(role.getName());
            collection.addAll(role.getPrivileges());
        }
        for (Privilege item : collection) {
            privileges.add(item.getName());
        }
        return privileges;
    }

    private List<GrantedAuthority> getGrantedAuthorities(List<String> privileges) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String privilege : privileges) {
          
            authorities.add(new SimpleGrantedAuthority(privilege));
        }
        return authorities;
    }

}
