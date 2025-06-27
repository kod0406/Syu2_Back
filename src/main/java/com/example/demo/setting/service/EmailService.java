package com.example.demo.setting.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class EmailService {

    @Autowired(required = false)  // 빈이 없어도 오류 발생하지 않도록
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${frontend.url}")
    private String frontendUrl;

    /**
     * 매장 회원가입 환영 이메일 발송
     */
    @Async
    public void sendWelcomeEmail(String toEmail, String storeName, String ownerEmail) {
        if (!isEmailConfigured()) {
            log.info("📧 [TEST MODE] 회원가입 환영 이메일 - 수신자: {}, 매장: {}", toEmail, storeName);
            log.info("📧 [TEST MODE] 실제 이메일을 발송하려면 application.properties에서 이메일 설정을 완료해주세요.");
            return;
        }

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "IGO 매장 관리 시스템");
            helper.setTo(toEmail);
            helper.setSubject("🎉 IGO 매장 등록이 완료되었습니다!");

            String emailContent = createWelcomeEmailContent(storeName, ownerEmail);
            helper.setText(emailContent, true);

            javaMailSender.send(message);
            log.info("✅ 회원가입 환영 이메일 발송 성공 - 수신자: {}, 매장: {}", toEmail, storeName);

        } catch (Exception e) {
            log.error("❌ 회원가입 환영 이메일 발송 실패 - 수신자: {}, 오류: {}", toEmail, e.getMessage());
            log.info("💡 이메일 설정을 확인해주세요. application.properties에서 spring.mail.username과 spring.mail.password를 설정해주세요.");
        }
    }

    /**
     * 매장 로그인 알림 이메일 발송
     */
    @Async
    public void sendLoginNotificationEmail(String toEmail, String storeName, String deviceInfo, LocalDateTime loginTime) {
        if (!isEmailConfigured()) {
            log.info("📧 [TEST MODE] 로그인 알림 - 수신자: {}, 매장: {}, 기기: {}", toEmail, storeName, deviceInfo);
            return;
        }

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "IGO 보안 알림");
            helper.setTo(toEmail);
            helper.setSubject("🔐 IGO 매장 계정 로그인 알림");

            String emailContent = createLoginNotificationContent(storeName, deviceInfo, loginTime);
            helper.setText(emailContent, true);

            javaMailSender.send(message);
            log.info("✅ 로그인 알림 이메일 발송 성공 - 수신자: {}, 매장: {}", toEmail, storeName);

        } catch (Exception e) {
            log.error("❌ 로그인 알림 이메일 발송 실패 - 수신자: {}, 오류: {}", toEmail, e.getMessage());
        }
    }

    /**
     * 의심스러운 로그인 경고 이메일 발송
     */
    @Async
    public void sendSuspiciousLoginAlert(String toEmail, String storeName, String deviceInfo, LocalDateTime loginTime) {
        if (!isEmailConfigured()) {
            log.warn("⚠️ [TEST MODE] 의심스러운 로그인 경고 - 수신자: {}, 매장: {}, 기기: {}", toEmail, storeName, deviceInfo);
            return;
        }

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "IGO 보안 경고");
            helper.setTo(toEmail);
            helper.setSubject("⚠️ IGO 계정 보안 경고 - 새로운 기기에서 로그인");

            String emailContent = createSuspiciousLoginAlertContent(storeName, deviceInfo, loginTime);
            helper.setText(emailContent, true); // HTML 형식

            javaMailSender.send(message);
            log.info("✅ 의심스러운 로그인 경고 이메일 발송 성공 - 수신자: {}, 매장: {}", toEmail, storeName);

        } catch (Exception e) {
            log.error("❌ 의심스러운 로그인 경고 이메일 발송 실패 - 수신자: {}, 오류: {}", toEmail, e.getMessage());
        }
    }

    /**
     * 이메일 설정이 완료되었는지 확인
     */
    private boolean isEmailConfigured() {
        return javaMailSender != null &&
               fromEmail != null &&
               !fromEmail.trim().isEmpty() &&
               !fromEmail.equals("your-email@gmail.com");
    }

    /**
     * 회원가입 환영 이메일 HTML 내용 생성
     */
    private String createWelcomeEmailContent(String storeName, String ownerEmail) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; text-align: center; padding: 30px; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .button { display: inline-block; background: #667eea; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 환영합니다!</h1>
                        <p>IGO 매장 관리 시스템에 가입해주셔서 감사합니다</p>
                    </div>
                    <div class="content">
                        <h2>매장 등록이 완료되었습니다</h2>
                        <p><strong>매장명:</strong> %s</p>
                        <p><strong>로그인 이메일:</strong> %s</p>
                        <p><strong>등록 일시:</strong> %s</p>
                        
                        <p>이제 다음 기능들을 사용하실 수 있습니다:</p>
                        <ul>
                            <li>📋 메뉴 관리 (등록, 수정, 삭제)</li>
                            <li>🎫 쿠폰 발행 및 관리</li>
                            <li>📊 실시간 주문 관리</li>
                            <li>📈 매출 통계 조회</li>
                            <li>📱 QR코드 생성 및 다운로드</li>
                        </ul>
                        
                        <a href="%s" class="button">매장 관리 시작하기</a>
                        
                        <p><strong>⚠️ 보안 안내:</strong></p>
                        <ul>
                            <li>로그인 시마다 이메일로 알림을 받게 됩니다</li>
                            <li>새로운 기기에서 로그인 시 보안 경고를 발송합니다</li>
                            <li>의심스러운 활동이 감지되면 즉시 연락주세요</li>
                        </ul>
                    </div>
                    <div class="footer">
                        <p>IGO 매장 관리 시스템 | 문의: support@igo.ai.kr</p>
                        <p>이 이메일은 자동으로 발송되었습니다.</p>
                    </div>
                </div>
            </body>
            </html>
        """.formatted(
            storeName,
            ownerEmail,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm")),
            frontendUrl
        );
    }

    /**
     * 로그인 알림 이메일 HTML 내용 생성
     */
    private String createLoginNotificationContent(String storeName, String deviceInfo, LocalDateTime loginTime) {
        String[] deviceParts = deviceInfo.split(",");
        String ip = deviceParts.length > 0 ? deviceParts[0].replace("IP:", "") : "알 수 없음";
        String userAgent = deviceParts.length > 1 ? deviceParts[1].replace("UA:", "") : "알 수 없음";

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #4CAF50; color: white; text-align: center; padding: 20px; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .info-box { background: white; padding: 15px; border-left: 4px solid #4CAF50; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔐 로그인 알림</h1>
                        <p>IGO 매장 계정에 로그인되었습니다</p>
                    </div>
                    <div class="content">
                        <h2>로그인 정보</h2>
                        <div class="info-box">
                            <p><strong>매장명:</strong> %s</p>
                            <p><strong>로그인 시간:</strong> %s</p>
                            <p><strong>IP 주소:</strong> %s</p>
                            <p><strong>기기 정보:</strong> %s</p>
                        </div>
                        
                        <p>본인의 로그인이 맞다면 이 이메일을 무시하셔도 됩니다.</p>
                        <p><strong>⚠️ 본인의 로그인이 아니라면 즉시 비밀번호를 변경하고 고객센터에 연락해주세요.</strong></p>
                    </div>
                    <div class="footer">
                        <p>IGO 매장 관리 시스템 | 문의: support@igo.ai.kr</p>
                    </div>
                </div>
            </body>
            </html>
        """.formatted(
            storeName,
            loginTime.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm:ss")),
            ip,
            userAgent
        );
    }

    /**
     * 의심스러운 로그인 경고 이메일 HTML 내용 생성
     */
    private String createSuspiciousLoginAlertContent(String storeName, String deviceInfo, LocalDateTime loginTime) {
        String[] deviceParts = deviceInfo.split(",");
        String ip = deviceParts.length > 0 ? deviceParts[0].replace("IP:", "") : "알 수 없음";
        String userAgent = deviceParts.length > 1 ? deviceParts[1].replace("UA:", "") : "알 수 없음";

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #ff5722; color: white; text-align: center; padding: 20px; border-radius: 10px 10px 0 0; }
                    .content { background: #fff3e0; padding: 30px; border-radius: 0 0 10px 10px; }
                    .alert-box { background: #ffebee; padding: 15px; border-left: 4px solid #f44336; margin: 20px 0; }
                    .button { display: inline-block; background: #f44336; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>⚠️ 보안 경고</h1>
                        <p>새로운 기기에서 로그인이 감지되었습니다</p>
                    </div>
                    <div class="content">
                        <div class="alert-box">
                            <h2>🚨 즉시 확인 필요</h2>
                            <p><strong>매장명:</strong> %s</p>
                            <p><strong>로그인 시간:</strong> %s</p>
                            <p><strong>IP 주소:</strong> %s</p>
                            <p><strong>기기 정보:</strong> %s</p>
                        </div>
                        
                        <h3>⚠️ 보안 조치 안내</h3>
                        <p><strong>본인의 로그인이 맞다면:</strong> 이 이메일을 무시하셔도 됩니다.</p>
                        <p><strong>본인의 로그인이 아니라면 즉시:</strong></p>
                        <ol>
                            <li>비밀번호를 변경해주세요</li>
                            <li>다른 기기에서 로그아웃해주세요</li>
                            <li>고객센터에 신고해주세요</li>
                        </ol>
                        
                        <a href="%s" class="button">즉시 계정 보안 강화</a>
                    </div>
                    <div class="footer">
                        <p>IGO 매장 관리 시스템 보안팀 | 긴급 문의: security@igo.ai.kr</p>
                    </div>
                </div>
            </body>
            </html>
        """.formatted(
            storeName,
            loginTime.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm:ss")),
            ip,
            userAgent,
            frontendUrl
        );
    }
}
