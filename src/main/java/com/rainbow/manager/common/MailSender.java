package com.rainbow.manager.common;

import com.rainbow.manager.config.EmailConfig;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

/**
 * Created by xuming on 2017/5/11.
 */
public class MailSender {

    private Session session = null;
    private EmailConfig config = null;

    public MailSender(EmailConfig config) {
        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", config.getProtocol());
        props.setProperty("mail.smtp.host", config.getServer());
        props.setProperty("mail.smtp.auth", "true");
        props.setProperty("mail.smtp.connectiontimeout", "3000");
        props.put("mail.smtp.timeout", "3000");

//        props.setProperty("mail.smtp.port", port);
//        props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//        props.setProperty("mail.smtp.socketFactory.fallback", "false");
//        props.setProperty("mail.smtp.socketFactory.port", port);

        this.session = Session.getDefaultInstance(props);
        this.config = config;
    }

    public void send(String content) throws Exception {
        Transport transport = session.getTransport();
        transport.connect(config.getSendEmailUser(),  config.getSendEmailPwd());

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(config.getSendEmailPwd(), "ServiceManager", "UTF-8"));

        String[] splits = config.getReceiveEmailUsers().split(",", -1);
        for (String split : splits) {
            message.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(split, split, "UTF-8"));
        }

        message.setSubject("服务运行失败通知", "UTF-8");
        message.setContent(content, "text/html;charset=UTF-8");
        message.setSentDate(new Date());

        message.saveChanges();

        transport.sendMessage(message, message.getAllRecipients());
        transport.close();
    }
}
