package com.app.Service;

import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EmailService {

  private final JavaMailSender mailSender;

  public void sendVerificationAccount(String to, String token) {
    SimpleMailMessage mailMessage = new SimpleMailMessage();
    mailMessage.setTo(to);
    mailMessage.setSubject("Verify your account");
    mailMessage.setText(
        "To verify your account, click on following link: http://localhost:8080/api/verify?token="
            + token);
    mailSender.send(mailMessage);
  }
}
