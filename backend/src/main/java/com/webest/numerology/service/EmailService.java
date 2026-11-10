package com.webest.numerology.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${numerology.app.public-url:http://localhost:4200}")
    private String publicUrl;

    @Value("${numerology.mail.from:noreply@localhost}")
    private String fromAddress;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    public EmailService(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String toEmail, String token) {
        String link = publicUrl.replaceAll("/$", "") + "/verify?token=" + token;
        String subject = "請驗證您的命理大師帳號信箱";
        String plainBody = """
                您好，

                請點擊以下連結完成信箱驗證（24 小時內有效）：
                %s

                若您未註冊，請忽略此郵件。
                """.formatted(link);
        String htmlBody = """
                <div style="font-family:sans-serif;line-height:1.6;color:#333;">
                  <p>您好，</p>
                  <p>請點擊下方按鈕完成信箱驗證（24 小時內有效）：</p>
                  <p>
                    <a href="%1$s"
                       style="display:inline-block;padding:10px 18px;background:#8b4513;color:#fff;
                              text-decoration:none;border-radius:6px;">驗證我的信箱</a>
                  </p>
                  <p style="font-size:13px;color:#666;">若按鈕無法開啟，請複製連結到瀏覽器：<br/>
                    <a href="%1$s">%1$s</a>
                  </p>
                  <p style="font-size:13px;color:#888;">若您未註冊，請忽略此郵件。</p>
                </div>
                """.formatted(link);

        if (!isMailReady()) {
            log.info(
                    "Mail not configured (sender={}, host={}, login={}, smtpKey={}). Verification link for {}: {}",
                    mailSender != null,
                    StringUtils.isNotBlank(mailHost),
                    StringUtils.isNotBlank(mailUsername),
                    StringUtils.isNotBlank(mailPassword),
                    toEmail,
                    link);
            return;
        }

        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(plainBody, htmlBody);
            mailSender.send(mime);
            log.info("Verification email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
            log.info("Fallback verification link for {}: {}", toEmail, link);
        }
    }

    private boolean isMailReady() {
        return mailSender != null
                && StringUtils.isNoneBlank(mailHost, mailUsername, mailPassword, fromAddress);
    }
}
