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
     * 이메일 인증 링크 발송
     */
    @Async
    public void sendEmailVerification(String toEmail, String storeName, String verificationToken) {
        if (!isEmailConfigured()) {
            log.info("📧 [TEST MODE] 이메일 인증 - 수신자: {}, 매장: {}, 토큰: {}", toEmail, storeName, verificationToken);
            log.info("📧 [TEST MODE] 인증 링크: {}/api/stores/verify-email?token={}", frontendUrl, verificationToken);
            return;
        }

        try {
            log.debug("[이메일 발송 시작] 수신자: {}, fromEmail: {}, fromEmail 타입: {}",
                    toEmail, fromEmail, fromEmail != null ? fromEmail.getClass().getSimpleName() : "null");

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // fromEmail이 null이거나 빈 문자열인 경우 처리
            String safeFromEmail = (fromEmail != null && !fromEmail.trim().isEmpty()) ? fromEmail.trim() : "no-reply@wte.ai.kr";

            helper.setFrom(safeFromEmail, "와따잇 (WTE,What To Eat?) 이메일 인증");
            helper.setTo(toEmail.trim());
            helper.setSubject("📧 와따잇 (WTE,What To Eat?) 매장 이메일 인증을 완료해주세요");

            String emailContent = createEmailVerificationContent(storeName, verificationToken);
            helper.setText(emailContent, true);

            log.debug("[이메일 발송 준비 완료] From: {}, To: {}", safeFromEmail, toEmail);
            javaMailSender.send(message);
            log.info("✅ 이메일 인증 링크 발송 성공 - 수신자: {}, 매장: {}", toEmail, storeName);

        } catch (Exception e) {
            log.error("❌ 이메일 인증 링크 발송 실패 - 수신자: {}, 오류: {}", toEmail, e.getMessage());
            log.error("❌ 상세 스택트레이스:", e);
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

            helper.setFrom(fromEmail, "와따잇 (WTE,What To Eat?) 보안 알림");
            helper.setTo(toEmail);
            helper.setSubject("🔐 와따잇 (WTE,What To Eat?) 매장 계정 로그인 알림");

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

            helper.setFrom(fromEmail, "와따잇 (WTE,What To Eat?) 보안 경고");
            helper.setTo(toEmail);
            helper.setSubject("⚠️ 와따잇 (WTE,What To Eat?) 계정 보안 경고 - 새로운 기기에서 로그인");

            String emailContent = createSuspiciousLoginAlertContent(storeName, deviceInfo, loginTime);
            helper.setText(emailContent, true); // HTML 형식

            javaMailSender.send(message);
            log.info("✅ 의심스러운 로그인 경고 이메일 발송 성공 - 수신자: {}, 매장: {}", toEmail, storeName);

        } catch (Exception e) {
            log.error("❌ 의심스러운 로그인 경고 이메일 발송 실패 - 수신자: {}, 오류: {}", toEmail, e.getMessage());
        }
    }

    /**
     * 비밀번호 재설정 이메일 발송
     */
    @Async
    public void sendPasswordResetEmail(String toEmail, String storeName, String resetToken) {
        if (!isEmailConfigured()) {
            log.info("📧 [TEST MODE] 비밀번호 재설정 - 수신자: {}, 매장: {}, 토큰: {}", toEmail, storeName, resetToken);
            log.info("📧 [TEST MODE] 재설정 링크: {}/reset-password?token={}", frontendUrl, resetToken);
            return;
        }

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "와따잇 (WTE,What To Eat?) 비밀번호 재설정");
            helper.setTo(toEmail);
            helper.setSubject("🔑 와따잇 (WTE,What To Eat?) 매장 비밀번호 재설정 요청");

            String emailContent = createPasswordResetContent(storeName, resetToken);
            helper.setText(emailContent, true);

            javaMailSender.send(message);
            log.info("✅ 비밀번호 재설정 이메일 발송 성공 - 수신자: {}, 매장: {}", toEmail, storeName);

        } catch (Exception e) {
            log.error("❌ 비밀번호 재설정 이메일 발송 실패 - 수신자: {}, 오류: {}", toEmail, e.getMessage());
        }
    }

    /**
     * 회원탈퇴 완료 이메일 발송
     */
    @Async
    public void sendWithdrawalNotificationEmail(String toEmail, String storeName) {
        if (!isEmailConfigured()) {
            log.info("📧 [TEST MODE] 회원탈퇴 알림 - 수신자: {}, 매장: {}", toEmail, storeName);
            return;
        }

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "와따잇 (WTE,What To Eat?) 서비스");
            helper.setTo(toEmail);
            helper.setSubject("💔 와따잇 (WTE,What To Eat?) 매장 서비스 탈퇴 완료 안내");

            String emailContent = createWithdrawalNotificationContent(storeName);
            helper.setText(emailContent, true);

            javaMailSender.send(message);
            log.info("✅ 회원탈퇴 완료 이메일 발송 성공 - 수신자: {}, 매장: {}", toEmail, storeName);

        } catch (Exception e) {
            log.error("❌ 회원탈퇴 완료 이메일 발송 실패 - 수신자: {}, 오류: {}", toEmail, e.getMessage());
        }
    }

    /**
     * 이메일 설정이 완료되었는지 확인
     */
    private boolean isEmailConfigured() {
        if (javaMailSender == null) {
            log.warn("[이메일 설정] javaMailSender 빈이 주입되지 않았습니다. TEST MODE 동작");
            return false;
        }
        if (fromEmail == null || fromEmail.trim().isEmpty()) {
            log.warn("[이메일 설정] spring.mail.username(fromEmail)이 비어있습니다. TEST MODE 동작");
            return false;
        }
        if (fromEmail.equals("test@gmail.com")) { //
            log.warn("[이메일 설정] fromEmail이 설정된 이메일이 아닙니다. TEST MODE 동작");
            return false;
        }
        log.info("[이메일 설정] 실제 이메일 발송 모드 동작 (fromEmail: {})", fromEmail);
        return true;
    }

    /**
     * 이메일 인증 링크 HTML 내용 생성
     */
    private String createEmailVerificationContent(String storeName, String verificationToken) {
// String verificationUrl = frontendUrl + "/api/stores/verify-email?token=" + verificationToken;
// 프론트엔드 인증 페이지로 리다이렉트 (백엔드 API가 아닌)
        String verificationUrl = frontendUrl + "/verify-email?token=" + verificationToken;

        return """
                <!DOCTYPE html>
                <html>
                <head>
                <meta charset=\"UTF-8\">
                <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: linear-gradient(135deg, #4CAF50 0%%, #45a049 100%%); color: white; text-align: center; padding: 30px; border-radius: 10px 10px 0 0; }
                .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                .button { display: inline-block; background: #4CAF50; color: white; padding: 15px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; font-weight: bold; }
                .button:hover { background: #45a049; }
                .warning { background: #fff3cd; padding: 15px; border-left: 4px solid #ffc107; margin: 20px 0; }
                .features { background: #e8f5e8; padding: 20px; border-radius: 8px; margin: 20px 0; }
                .footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }
                </style>
                </head>
                <body>
                <div class=\"container\">
                <div class=\"header\">
                <h1>📧 이메일 인증</h1>
                <p>와따잇 (WTE,What To Eat?) 매장 이메일 인증을 완료해주세요</p>
                </div>
                <div class=\"content\">
                <h2>안녕하세요, %s님!</h2>
                <p>와따잇 (WTE,What To Eat?) 매장 관리 시스템에 가입해주셔서 감사합니다.</p>
                <p>서비스 이용을 위해 <strong>이메일 인증</strong>을 완료해주세요.</p>
                
                <div style="text-align: center; margin: 30px 0;">
                <a href="%s" class="button">✅ 이메일 인증 완료하기</a>
                </div>
                
                <div class="warning">
                <h3>⚠️ 중요 안내</h3>
                <ul>
                <li>이 링크는 <strong>24시간 후</strong> 만료됩니다</li>
                <li>인증이 완료되어야 로그인이 가능합니다</li>
                <li>링크가 작동하지 않으면 URL을 복사하여 브라우저에 직접 붙여넣어 주세요</li>
                </ul>
                </div>
                
                <div class="features">
                <h3>🎉 인증 완료 후 이용 가능한 기능들</h3>
                <ul>
                <li>📋 <strong>메뉴 관리</strong> - 메뉴 등록, 수정, 삭제 및 가격 설정</li>
                <li>🎫 <strong>쿠폰 발행</strong> - 할인 쿠폰 생성 및 고객 혜택 관리</li>
                <li>📊 <strong>실시간 주문 관리</strong> - 들어오는 주문 확인 및 처리</li>
                <li>📈 <strong>매출 통계</strong> - 일별/월별 매출 현황 및 분석</li>
                <li>📱 <strong>QR코드 생성</strong> - 매장 전용 QR코드 다운로드</li>
                <li>🔔 <strong>알림 서비스</strong> - 주문, 리뷰 등 실시간 알림</li>
                </ul>
                </div>
                
                <p><strong>인증 링크:</strong></p>
                <p style="word-break: break-all; background: #f4f4f4; padding: 10px; border-radius: 4px;">
                %s
                </p>
                
                <p>링크가 만료된 경우, 로그인 페이지에서 "인증 메일 재발송"을 요청해주세요.</p>
                
                <div style="background: #f0f8ff; padding: 15px; border-left: 4px solid #2196F3; margin: 20px 0;">
                <h4>💡 다음 단계</h4>
                <ol>
                <li>위의 인증 버튼을 클릭하세요</li>
                <li>브라우저에서 인증 완료 메시지를 확인하세요</li>
                <li>와따잇! 매장 관리 시스템에 로그인하세요</li>
                <li>매장 정보를 설정하고 메뉴를 등록하세요</li>
                </ol>
                </div>
                </div>
                <div class="footer">
                <p>와따잇 (WTE,What To Eat?) 매장 관리 시스템 | 문의: support@wte.ai.kr</p>
                <p>이 이메일은 자동으로 발송되었습니다.</p>
                </div>
                </div>
                </body>
                </html>
                """.formatted(storeName, verificationUrl, verificationUrl);
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
                        <meta charset=\"UTF-8\">
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
                        <div class=\"container\">
                            <div class=\"header\">
                                <h1>🔐 로그인 알림</h1>
                                <p>와따잇 (WTE,What To Eat?) 매장 계정에 로그인되었습니다</p>
                            </div>
                            <div class=\"content\">
                                <h2>로그인 정보</h2>
                                <div class=\"info-box\">
                                    <p><strong>매장명:</strong> %s</p>
                                    <p><strong>로그인 시간:</strong> %s</p>
                                    <p><strong>IP 주소:</strong> %s</p>
                                    <p><strong>기기 정보:</strong> %s</p>
                                </div>
                
                                <p>본인의 로그인이 맞다면 이 이메일을 무시하셔도 됩니다.</p>
                                <p><strong>⚠️ 본인의 로그인이 아니라면 즉시 비밀번호를 변경하고 고객센터에 연락해주세요.</strong></p>
                            </div>
                            <div class=\"footer\">
                                <p>와따잇 (WTE,What To Eat?) 매장 관리 시스템 | 문의: support@wte.ai.kr</p>
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
                        <meta charset=\"UTF-8\">
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
                        <div class=\"container\">
                            <div class=\"header\">
                                <h1>⚠️ 보안 경고</h1>
                                <p>새로운 기기에서 와따잇 (WTE,What To Eat?) 계정 로그인이 감지되었습니다</p>
                            </div>
                            <div class=\"content\">
                                <div class=\"alert-box\">
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
                            <div class=\"footer\">
                                <p>와따잇 (WTE,What To Eat?) 매장 관리 시스템 보안팀 | 긴급 문의: security@wte.ai.kr</p>
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

    /**
     * 비밀번호 재설정 이메일 HTML 내용 생성
     */
    private String createPasswordResetContent(String storeName, String resetToken) {
        String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;

        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                <meta charset=\"UTF-8\">
                <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: #2196F3; color: white; text-align: center; padding: 30px; border-radius: 10px 10px 0 0; }
                .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                .button { display: inline-block; background: #2196F3; color: white; padding: 15px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; font-weight: bold; }
                .button:hover { background: #1976D2; }
                .footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }
                </style>
                </head>
                <body>
                <div class=\"container\">
                <div class=\"header\">
                <h1>🔑 비밀번호 재설정</h1>
                <p>와따잇 (WTE,What To Eat?) 매장 비밀번호를 재설정해주세요</p>
                </div>
                <div class=\"content\">
                <h2>안녕하세요, %s님!</h2>
                <p>비밀번호 재설정 요청을 받았습니다.</p>
                <p>아래 버튼을 클릭하여 비밀번호를 재설정해주세요.</p>
                
                <div style="text-align: center; margin: 30px 0;">
                <a href="%s" class="button">🔄 비밀번호 재설정하기</a>
                </div>
                
                <p>링크가 작동하지 않으면 URL을 복사하여 브라우저에 직접 붙여넣어 주세요.</p>
                
                <p><strong>인증 링크:</strong></p>
                <p style="word-break: break-all; background: #f4f4f4; padding: 10px; border-radius: 4px;">
                %s
                </p>
                
                <div style="background: #e8f5e9; padding: 15px; border-left: 4px solid #4CAF50; margin: 20px 0;">
                <h4>✅ 비밀번호 재설정 완료 후</h4>
                <ul>
                <li>새로운 비밀번호로 로그인하세요</li>
                <li>계정 정보를 최신 상태로 유지하세요</li>
                <li>정기적으로 비밀번호를 변경하세요</li>
                </ul>
                </div>
                </div>
                <div class="footer">
                <p>와따잇 (WTE,What To Eat?) 매장 관리 시스템 | 문의: support@wte.ai.kr</p>
                <p>이 이메일은 자동으로 발송되었습니다.</p>
                </div>
                </div>
                </body>
                </html>
                """, storeName, resetUrl, resetUrl);
    }

    /**
     * 회원탈퇴 완료 이메일 HTML 내용 생성
     */
    private String createWithdrawalNotificationContent(String storeName) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                <meta charset=\"UTF-8\">
                <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: linear-gradient(135deg, #f44336 0%%, #d32f2f 100%%); color: white; text-align: center; padding: 30px; border-radius: 10px 10px 0 0; }
                .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                .thank-you { background: #e8f5e8; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #4CAF50; }
                .info-box { background: #fff3cd; padding: 15px; border-left: 4px solid #ffc107; margin: 20px 0; }
                .footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }
                .highlight { color: #f44336; font-weight: bold; }
                .comeback { background: #e3f2fd; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #2196F3; }
                .alert { background: #ffebee; padding: 15px; border-left: 4px solid #f44336; margin: 20px 0; color: #b71c1c; font-weight: bold; }
                </style>
                </head>
                <body>
                <div class=\"container\">
                <div class=\"header\">
                <h1>💔 와따잇 (WTE,What To Eat?) 서비스 탈퇴 완료</h1>
                <p>%s님의 회원탈퇴가 완료되었습니다</p>
                </div>
                <div class=\"content\">
                <div class=\"thank-you\">
                <h2>🙏 감사의 인사</h2>
                <p><strong>%s</strong>님, 그동안 와따잇 (WTE,What To Eat?) 매장 관리 서비스를 이용해주셔서 진심으로 감사합니다.</p>
                <p>저희 서비스와 함께해주신 모든 시간들이 소중했습니다.</p>
                </div>
                <li>✅ <strong>주문 내역 삭제</strong> - 모든 주문 및 리뷰 데이터</li>
                <li>✅ <strong>개인정보 완전 삭제</strong> - 복구 불가능한 영구 삭제</li>
                </ul>

                <div class=\"info-box\">
                <h3>⚠️ 중요 안내사항</h3>
                <ul>
                <li>삭제된 데이터는 <span class=\"highlight\">복구가 불가능</span>합니다</li>
                <li>동일한 이메일로 재가입이 가능합니다</li>
                <li>재가입 시 이전 데이터는 복원되지 않습니다</li>
                <li>고객이 보유했던 쿠폰은 모두 사용할 수 없게 됩니다</li>
                </ul>
                </div>

                <div class=\"comeback\">
                <h3>🌟 언제든지 다시 돌아오세요!</h3>
                <p>앞으로 더 나은 서비스로 준비하여 기다리겠습니다.</p>
                <p>사업이 번창하시길 진심으로 응원합니다!</p>
                <p><strong>와따잇 (WTE,What To Eat?)과 함께했던 모든 순간에 감사드립니다. 💚</strong></p>
                </div>
                
                
                <div class=\"alert\">
                <h3>❗ 본인이 직접 탈퇴를 신청하지 않으셨나요?</h3>
                <p>
        만약 본인이 직접 회원탈퇴를 신청하지 않으셨다면, 즉시 고객센터로 연락해주시기 바랍니다.
                </p>
                <h3>📞 문의사항</h3>
                <p>탈퇴와 관련하여 궁금한 점이 있으시면 언제든지 연락해주세요.</p>
                <ul>
                <li><strong>고객센터 이메일:</strong> support@wte.ai.kr</li>
                <li><strong>전화:</strong> 1588-0000</li>
                <li><strong>운영시간:</strong> 평일 09:00 ~ 18:00</li>
                </ul>
                </div>

                <div style=\"text-align: center; margin: 30px 0; padding: 20px; background: #f0f8ff; border-radius: 8px;\">
                <h3>🎯 새로운 시작을 응원합니다!</h3>
                <p>%s님의 앞날에 항상 행운이 함께하시길 바랍니다.</p>
                <p><em>\"모든 끝은 새로운 시작입니다\"</em></p>
                </div>
                </div>
                <div class=\"footer\">
                <p><strong>와따잇 (WTE,What To Eat?) 서비스 운영팀</strong></p>
                <p>이 이메일은 회원탈퇴 완료 확인을 위해 자동으로 발송되었습니다.</p>
                <p>© 2025 와따잇 (WTE,What To Eat?). All rights reserved.</p>
                </div>
                </div>
                </body>
                </html>
                """.formatted(storeName, storeName, storeName);
    }
}
