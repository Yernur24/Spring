package book.store.service;

import book.store.model.BookModel;
import book.store.model.Permission;
import book.store.model.User;
import book.store.repository.PermissionRepository;
import book.store.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("User Not found");
        }
        if (user.getIsBanned()) {
            throw new UsernameNotFoundException("User is banned");
        }
        return user;
    }
    public User saveUser(User user) {   // save user method using for update user
        return userRepository.save(user);
    }
    public User addUser(User user){
        User checkUser = userRepository.findByEmail(user.getEmail());
        Permission defaultRole = permissionRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Default role not found"));
        if (checkUser==null){
            user.setPermissions(List.of(defaultRole));
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            return userRepository.save(user);
        }
        return null;
    }
    public User updatePassword(String newPassword,String oldPassword){
        User currentUser = getCurrentSessionUser();
        if (passwordEncoder.matches(oldPassword, currentUser.getPassword())){
            currentUser.setPassword(passwordEncoder.encode(newPassword));
            return userRepository.save(currentUser);
        }
        return null;
    }
    public User getCurrentSessionUser(){
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)){
            User user= (User) authentication.getPrincipal();
            if (user != null){
                return user;
            }
        }
        return null;
    }

    public void toggleBan(Long id, boolean banned) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setIsBanned(banned);
            userRepository.save(user);
        }
    }

    public List<User> getAllUsers() {
        return userRepository.findAllWithPermissions();
    }

    @Transactional
    public void changeUserRole(Long userId, String roles) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Permission role = permissionRepository.findByRole(roles)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.getPermissions().clear();
        user.addPermission(role);
        userRepository.save(user);
    }
}
