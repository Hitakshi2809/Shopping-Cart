package com.ecom.Shopping.Cart.util;

import com.ecom.Shopping.Cart.model.ProductOrder;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

@Component
public class CommonUtil {
     @Autowired
    private   JavaMailSender javaMailSender;

    public String generateUrl(HttpServletRequest request) {
        String siteUrl =  request.getRequestURL().toString();
      return   siteUrl .replace( request.getServletPath()," ");

    }

    public Boolean sendMail(String url, String reciepentEmail) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("hitakshigupta2805@gmail.com","Shoping Cart");

        helper.setTo(reciepentEmail);

        String content = "<p>Hello,</p>"+"<p>You hae requseted to reset your password.</p>"+
                "<p>Click the link below to change your password:</p>"+"<p><a href=\""+url+"\">Change my password</a></p>";

        helper.setSubject("Password Reset");
        helper.setText(content,true);

        javaMailSender.send(message);
        return true;


    }

}
