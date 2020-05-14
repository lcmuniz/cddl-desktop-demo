package br.ufma.lsdi.cddedesktopdemo;

import br.ufma.lsdi.cddl.CDDL;
import br.ufma.lsdi.cddl.Connection;
import br.ufma.lsdi.cddl.ConnectionFactory;
import br.ufma.lsdi.cddl.message.Message;
import br.ufma.lsdi.cddl.pubsub.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CddeDesktopDemoApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(CddeDesktopDemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("OI");

        //String host = CDDL.startMicroBroker();

        Connection con = ConnectionFactory.createConnection();
        con.setClientId("lcmuniz@gmail.com");
        con.setHost("lsdi.ufma.br");
        con.connect();

        CDDL cddl = CDDL.getInstance();
        cddl.setConnection(con);
        cddl.startService();

        Subscriber sub = SubscriberFactory.createSubscriber();
        sub.addConnection(con);
        sub.subscribeServiceByName("MyService");
        sub.setSubscriberListener(message -> System.out.println("+++++++++++++++++++++++" + message));

        sub.getMonitor().addRule("select * from Message", message -> {
            System.out.println("MONITORAMENTO");
            System.out.println(message);
        });

        Publisher pub = PublisherFactory.createPublisher();
        pub.addConnection(con);


        Message msg = new Message();
        msg.setServiceName("MyService");
        msg.setServiceValue("OLA");

        new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            pub.publish(msg);
        }).start();
    }
}
