package sample.mail;

import org.junit.jupiter.api.Test;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import sample.http.ResourceUtil;

public class SimpleJavaMailTest {


    @Test
    public void testSendMail() {
        try (final Mailer mailer = MailerBuilder
//            .withSMTPServerHost("smtp.gmail.com")
//            .withSMTPServerPort(587)
//            .withSMTPServerUsername("ivar.chen@ulala.ca")
//            .withSMTPServerPassword("lkor tigb xgou mwmu")
            .buildMailer()) {
            mailer.sendMail(EmailBuilder.startingBlank()
                .from("ivar.chen@ulala.ca")
                .to("ivarchen@gmail.com")
                .withSubject("test")
                .withPlainText("this is a test")
                .withAttachment("img.png", ResourceUtil.loadResource("classpath:ProjectScheduleNetworkDiagram.png").getInputStream().readAllBytes(), "image/png")
                .buildEmail());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
