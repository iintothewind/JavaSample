package sample.mail;

import org.junit.Test;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;

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
                .buildEmail());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
