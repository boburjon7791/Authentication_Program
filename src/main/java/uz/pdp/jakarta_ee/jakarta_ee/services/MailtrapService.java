package uz.pdp.jakarta_ee.jakarta_ee.services;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.NonNull;
import uz.pdp.jakarta_ee.jakarta_ee.utils.ConfirmationCodes;
import uz.pdp.jakarta_ee.jakarta_ee.utils.PasswordUtils;

import javax.activation.MimeType;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.awt.event.WindowFocusListener;
import java.time.LocalDateTime;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Random;

public class MailtrapService {

    private static MailtrapService mailtrapService;

    public static MailtrapService getMailtrapService() {
        if (mailtrapService == null) {
            mailtrapService = new MailtrapService();
        }
        return mailtrapService;
    }

    private static final String username = "soliyevboburjon95@gmail.com";
    private static final String password = "wtizwvgmydpornes";

    public static void sendActivationEmail(@NonNull String userID,@NonNull String email){
        try {
            Properties properties = getProperties();
            Session session = getSession(properties, username, password);
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            email="soliyevboburjon95@gmail.com";
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
            message.setSubject("This is confirm code for our library platform");
            int i = new Random().nextInt(1000, 100000);
            Hashtable<String,LocalDateTime> hashtable=new Hashtable<>();
            ConfirmationCodes.confirmationCodes.put(userID,hashtable);
            hashtable.put(String.valueOf(i),LocalDateTime.now());
            message.setText(hashtable.toString());
            Transport.send(message);
            System.out.println(ConfirmationCodes.confirmationCodes);
            System.out.println("Message Sent Successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Properties getProperties() {
        Properties properties = new Properties();
        /*properties.put("mail.smtp.host", "sandbox.smtp.mailtrap.io");
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.auth", "true");*/


        // Setup mail server
        // Assuming you are sending email from through gmails smtp
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");
        return properties;
    }


    private static Session getSession(Properties properties, String username, String password) {
        return Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }
}