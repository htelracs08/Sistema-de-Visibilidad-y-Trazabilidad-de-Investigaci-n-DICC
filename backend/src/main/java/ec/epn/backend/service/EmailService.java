package ec.epn.backend.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

  private final JavaMailSender mailSender;

  public EmailService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  @Async
  public void sendPlainText(String to, String subject, String body) {
    try {
      SimpleMailMessage msg = new SimpleMailMessage();
      msg.setTo(to);
      msg.setFrom("sistemasoft26@gmail.com"); // ✅ recomendado
      msg.setSubject(subject);
      msg.setText(body);
      mailSender.send(msg);
      System.out.println("[MAIL OK] to=" + to + " subject=" + subject);
    } catch (Exception e) {
      System.out.println("[MAIL ERROR] to=" + to + " subject=" + subject + " err=" + e.getMessage());
    }
  }
}