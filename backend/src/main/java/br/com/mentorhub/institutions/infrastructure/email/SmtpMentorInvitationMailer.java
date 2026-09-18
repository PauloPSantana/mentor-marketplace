package br.com.mentorhub.institutions.infrastructure.email;

import br.com.mentorhub.institutions.application.MentorInvitationMailer;
import br.com.mentorhub.shared.exception.BusinessException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class SmtpMentorInvitationMailer implements MentorInvitationMailer {

    private static final Logger log = LoggerFactory.getLogger(SmtpMentorInvitationMailer.class);

    private final JavaMailSender mailSender;
    private final boolean smtpEnabled;
    private final String from;

    public SmtpMentorInvitationMailer(
            ObjectProvider<JavaMailSender> mailSender,
            @Value("${spring.mail.host:}") String mailHost,
            @Value("${spring.mail.username:}") String mailUsername,
            @Value("${spring.mail.password:}") String mailPassword,
            @Value("${mentorhub.mail.from:Mentor Marketplace <noreply@localhost>}") String from
    ) {
        this.mailSender = mailSender.getIfAvailable();
        this.smtpEnabled = this.mailSender != null
                && mailHost != null && !mailHost.isBlank()
                && mailUsername != null && !mailUsername.isBlank()
                && mailPassword != null && !mailPassword.isBlank();
        this.from = from;
        log.info(
                "SMTP convite: enabled={} host={} user={} passwordLength={}",
                this.smtpEnabled,
                mailHost,
                mailUsername,
                mailPassword == null ? 0 : mailPassword.length()
        );
    }

    @Override
    public boolean isEnabled() {
        return smtpEnabled;
    }

    @Override
    public boolean sendInvite(String to, String mentorName, String institutionName, String acceptUrl) {
        String firstName = firstName(mentorName);
        if (!smtpEnabled) {
            log.warn("SMTP não configurado. O convite não foi enviado por e-mail. dest={}", to);
            return false;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("Você foi convidado para o Mentor Marketplace");
            helper.setText(plainBody(firstName, institutionName, acceptUrl), htmlBody(firstName, institutionName, acceptUrl));
            mailSender.send(message);
            log.info("Convite de mentor enviado. dest={}", to);
            return true;
        } catch (MailAuthenticationException ex) {
            log.warn("Falha de autenticação SMTP. dest={}", to);
            throw new BusinessException(
                    "INVITATION_MAIL_AUTH",
                    "O Gmail recusou o login. Use uma Senha de app, não a senha da conta."
            );
        } catch (Exception ex) {
            log.warn("Falha ao enviar convite de mentor. dest={} causa={}", to, ex.getMessage());
            throw new BusinessException("INVITATION_MAIL_FAILED", "Não foi possível enviar o e-mail do convite. Verifique o SMTP.");
        }
    }

    private static String firstName(String name) {
        if (name == null || name.isBlank()) {
            return "mentor";
        }
        String trimmed = name.trim();
        int space = trimmed.indexOf(' ');
        return space < 0 ? trimmed : trimmed.substring(0, space);
    }

    private static String plainBody(String firstName, String institutionName, String acceptUrl) {
        return """
                Olá, %s!

                Você foi convidado para participar do Mentor Marketplace como mentor.

                Através da plataforma, você poderá acompanhar seus mentorados, organizar sessões, disponibilizar materiais, criar tarefas e questionários e gerenciar sua agenda de mentorias.

                Instituição: %s

                Clique no link abaixo para aceitar o convite e concluir seu cadastro:
                %s

                Este convite possui prazo de validade e é de uso pessoal.

                Esperamos você!

                Mentor Marketplace
                """.formatted(firstName, institutionName, acceptUrl);
    }

    private static String htmlBody(String firstName, String institutionName, String acceptUrl) {
        return """
                <p>Olá, %s!</p>
                <p>Você foi convidado para participar do Mentor Marketplace como mentor.</p>
                <p>Através da plataforma, você poderá acompanhar seus mentorados, organizar sessões, disponibilizar materiais, criar tarefas e questionários e gerenciar sua agenda de mentorias.</p>
                <p><strong>Instituição:</strong> %s</p>
                <p>Clique abaixo para aceitar o convite e concluir seu cadastro:</p>
                <p><a href="%s" style="display:inline-block;padding:12px 20px;background:#2563eb;color:#fff;text-decoration:none;border-radius:6px;">Aceitar convite</a></p>
                <p>Este convite possui prazo de validade e é de uso pessoal.</p>
                <p>Esperamos você!<br/>Mentor Marketplace</p>
                """.formatted(escape(firstName), escape(institutionName), escape(acceptUrl));
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
