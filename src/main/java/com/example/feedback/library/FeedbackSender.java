/*
 * Copyright (C) 2014 The Android Open Source Project, Tyler Heck
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.feedback.library;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;
import java.util.Properties;

public class FeedbackSender extends javax.mail.Authenticator {
    private DataHandler handler;
    private FileDataSource fileImage;
    private FileDataSource fileLog;
    private MimeBodyPart partBody;
    private MimeBodyPart partImage;
    private MimeBodyPart partLog;
    private MimeMessage message;
    private Multipart multipart;
    private Session session;
    private String mailhost = "smtp.gmail.com";
    private String password;
    private String user;

    static {
        Security.addProvider(new JSSEProvider());
    }

    public FeedbackSender(String user, String password) {
        this.user = user;
        this.password = password;

        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", mailhost);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.quitwait", "false");

        session = Session.getDefaultInstance(props, this);
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password);
    }

    public synchronized void sendMail(String subject, String body, String sender, String recipients) throws Exception {
        message = new MimeMessage(session);
        handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), "text/plain"));
        message.setSender(new InternetAddress(sender));
        message.setSubject(subject);
        message.setDataHandler(handler);

        if (recipients.indexOf(',') > 0) {
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
        } else {
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));
        }

        Transport.send(message);
    }

    public synchronized void sendMail(String subject, String body, String sender, String recipients, File attachmentImage, File attachmentLog) throws Exception {
        message = new MimeMessage(session);
        message.setSender(new InternetAddress(sender));
        message.setSubject(subject);

        partBody = new MimeBodyPart();
        partBody.setText(body);

        partImage = new MimeBodyPart();
        fileImage = new FileDataSource(attachmentImage);
        partImage.setDataHandler(new DataHandler(fileImage));
        partImage.setFileName(fileImage.getName());

        partLog = new MimeBodyPart();
        fileLog = new FileDataSource(attachmentLog);
        partLog.setDataHandler(new DataHandler(fileLog));
        partLog.setFileName(fileLog.getName());

        multipart = new MimeMultipart();
        multipart.addBodyPart(partBody);
        multipart.addBodyPart(partImage);
        multipart.addBodyPart(partLog);
        message.setContent(multipart);

        if (recipients.indexOf(',') > 0) {
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
        } else {
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));
        }

        Transport.send(message);
    }

    public class ByteArrayDataSource implements DataSource {
        private byte[] data;
        private String type;

        public ByteArrayDataSource(byte[] data, String type) {
            super();
            this.data = data;
            this.type = type;
        }

        public String getContentType() {
            if (type == null)
                return "application/octet-stream";
            else
                return type;
        }

        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(data);
        }

        public String getName() {
            return "ByteArrayDataSource";
        }

        public OutputStream getOutputStream() throws IOException {
            throw new IOException("Not Supported");
        }
    }
}
