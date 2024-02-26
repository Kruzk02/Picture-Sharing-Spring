package com.app.Service;

import com.app.DAO.Impl.RoleDaoImpl;
import com.app.DAO.Impl.UserDaoImpl;
import com.app.Model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired private UserDaoImpl userDao;
    @Autowired private RoleDaoImpl roleDao;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userDao.findUserByUsername(username);
        if (user == null){
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        return new CustomUserDetails(user);
    }
}
