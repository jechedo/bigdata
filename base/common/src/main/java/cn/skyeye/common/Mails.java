package cn.skyeye.common;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Date;
import java.util.Properties;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/11/10 14:05
 */
public class Mails {
    public static void main(String[] args) {

        try {
            String serverIp = "smtp.163.com";
            String serverPort = "25";

            final String username ="jechedo@163.com";
            final String password ="Lixc651024.!";


            Properties props = System.getProperties();
            props.setProperty("mail.smtp.host",serverIp);        //指定SMTP服务器
            props.setProperty("mail.smtp.auth","true");          //指定是否需要SMTP验证
            props.setProperty("mail.smtp.port", serverPort);     //指定端口

            //获得一个默认会话session
            Session mailSession = Session.getDefaultInstance(props,new Authenticator(){
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }});

            //是否在控制台显示debug信息
            mailSession.setDebug(true);
            Message message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress("jechedo@163.com"));//发件人

            message.setRecipient(Message.RecipientType.TO, new InternetAddress("jechedo@163.com"));//收件人
            message.setSubject("明天上班通知");//邮件主题

            //给消息对象设置内容
            String htmlContent = "<dev size='5'><table><tr><td>&nbsp;威胁类型&nbsp;</td><td>&nbsp;威胁名称&nbsp;</td><td>&nbsp;总数&nbsp;</td><td>&nbsp;新增&nbsp;</td></tr></table></dev>";
            //新建一个存放信件内容的BodyPart对象
            BodyPart mdp = new MimeBodyPart();
            //给BodyPart对象设置内容和格式/编码方式
            mdp.setContent(htmlContent,"text/html;charset=UTF-8");
            //新建一个MimeMultipart对象用来存放BodyPart对象(事实上可以存放多个)
            Multipart mm = new MimeMultipart();
            //将BodyPart加入到MimeMultipart对象中(可以加入多个BodyPart)
            mm.addBodyPart(mdp);
            //把mm作为消息对象的内容
            message.setContent(mm);

            message.setSentDate(new Date());
            Transport.send(message);
            System.out.println("Message sent.");

        }    catch(Exception e)    {

            System.out.println(e.toString());

        }



    }
}
