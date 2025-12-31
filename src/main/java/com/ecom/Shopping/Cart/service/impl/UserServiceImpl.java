package com.ecom.Shopping.Cart.service.impl;

import com.ecom.Shopping.Cart.model.UserDtls;
import com.ecom.Shopping.Cart.repository.UserRepository;
import com.ecom.Shopping.Cart.service.UserService;
import com.ecom.Shopping.Cart.util.AppConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.ecom.Shopping.Cart.util.AppConstant.UNLOCK_DURATION_TIME;

@Service
public class UserServiceImpl implements UserService {
    /**
     * @param id
     * @param status
     * @return
     */
    @Override
    public Boolean updateAccountStatus(Integer id, Boolean status) {
        Optional<UserDtls> findByUser= userRepository.findById(id);
        if(findByUser.isPresent()){
            UserDtls userdtls = findByUser.get();
            userdtls.setIsEnable(status);
            userRepository.save(userdtls);
            return true;
        }
        return false;


    }

    /**
     * @param user
     */
    @Override
    public void increaseFailAttempt(UserDtls user) {
        int attempt = user.getFailedAttempt() + 1;
        user.setFailedAttempt(attempt);
        userRepository.save(user);
    }

    /**
     * @param user
     */
    @Override
    public void userAccountLock(UserDtls user) {
              user.setAccountNonLocked(false);
              user.setLockTime((new Date()));
        userRepository.save(user);
    }

    /**
     * @param user
     * @return
     */
    @Override
    public Boolean unlockAccountTimeExpired(UserDtls user) {
        long lockTime = user.getLockTime().getTime();
        long unlockTime = lockTime + AppConstant. UNLOCK_DURATION_TIME;
        long currentTime = System.currentTimeMillis();

        if(unlockTime<currentTime){
            user.setAccountNonLocked(true);
            user.setFailedAttempt(0);
            user.setLockTime(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    /**
     * @param userId
     */
    @Override
    public void resetAttempt(int userId) {

    }

    /**
     * @param email
     * @param resetToken
     */
    @Override
    public void updateUserResetToken(String email, String resetToken) {
        UserDtls findByEmail = userRepository.findByEmail(email);
        findByEmail.setResetToken((resetToken));
        userRepository.save(findByEmail);

    }

    /**
     * @param token
     * @return
     */
    @Override
    public UserDtls getUserByToken(String token) {
        return  userRepository.findByResetToken(token);

    }

    /**
     * @param user
     * @return
     */
    @Override
    public UserDtls updateUser(UserDtls user) {
      return   userRepository.save(user);

    }

    /**
     * @param user
     * @return
     */
    @Override
    public UserDtls updateUserProfile(UserDtls user, MultipartFile img) {
        UserDtls dbUser = userRepository.findById(user.getId()).get();

        if(!img.isEmpty()){
            dbUser.setProfileImage(img.getOriginalFilename());
        }
        if(!ObjectUtils.isEmpty(dbUser)){
            dbUser.setName(user.getName());
            dbUser.setMobileNumber(user.getMobileNumber());
            dbUser.setAddress(user.getAddress());
            dbUser.setCity(user.getCity());
            dbUser.setState(user.getState());
            dbUser.setPincode(user.getPincode());
           dbUser =  userRepository.save(dbUser);

        }
try{


        if(!img.isEmpty()){
            File saveFile = new ClassPathResource("static/img").getFile();

            Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator + img.getOriginalFilename());

            Files.copy(img.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);

        }}
catch(Exception e){
    e.printStackTrace();
}

        return dbUser;
    }

    /**
     * @param email
     * @return
     */
    @Override
    public UserDtls getUserByEmail(String email) {
        return  userRepository.findByEmail(email);
    }

    /**
     * @return
     */
    @Override
    public List<UserDtls> getUsers(String role) {
        return  userRepository.findByRole(role);

    }

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Override
    public UserDtls saveUser(UserDtls user) {
        user.setRole("ROLE_USER");
        user.setIsEnable(true);
        user.setAccountNonLocked(true);
        user.setFailedAttempt(0);

        String encodePassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodePassword);
        UserDtls saveUser = userRepository.save(user);
        return saveUser;

    }

    public UserDtls saveAdmin(UserDtls user) {
        user.setRole("ROLE_ADMIN");
        user.setIsEnable(true);
        user.setAccountNonLocked(true);
        user.setFailedAttempt(0);

        String encodePassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodePassword);
        UserDtls saveUser = userRepository.save(user);
        return saveUser;

    }

    /**
     * @param email
     * @return
     */
    @Override
    public Boolean existsEmail(String email) {
        return userRepository.existsByEmail(email);
    }


}
