package br.ufma.lsdi.cddedesktopdemo;

import br.ufma.lsdi.cddl.CDDL;
import br.ufma.lsdi.cddl.Connection;
import br.ufma.lsdi.cddl.ConnectionFactory;
import br.ufma.lsdi.cddl.message.Message;
import br.ufma.lsdi.cddl.pubsub.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.greenrobot.eventbus.EventBus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;
import java.util.Random;

@SpringBootApplication
public class CddeDesktopDemoApplication implements CommandLineRunner {

    InterSCityService service = InterSCityService.getInstance();

    public static void main(String[] args) {
        SpringApplication.run(CddeDesktopDemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        // 1a4446ec-07cb-41ed-824a-cf5ed9484a9c
        Resource salaP = service.getResource("da305579-9f8c-4c87-93bd-186638b86581");
        //Resource salaP = service.getResource("efad126d-3068-4d8c-bc1e-1a97c9cca466");

        Connection con = ConnectionFactory.createConnection();
        con.setClientId("lcmuniz@gmail.com");
        con.setHost("broker.mqttdashboard.com");
        con.connect();

        CDDL cddl = CDDL.getInstance();
        cddl.setConnection(con);
        cddl.startService();

        Publisher pub = PublisherFactory.createPublisher();
        pub.addConnection(con);

        Subscriber sub = SubscriberFactory.createSubscriber();
        sub.addConnection(con);
        sub.subscribeServiceByName("LocalTemperature");
        sub.setSubscriberListener(message -> {
            new Thread(() -> {
                ObjectMapper mapper = new ObjectMapper();
                Temperature temp = mapper.convertValue(message.getServiceValue()[0], Temperature.class);
                EventBus.getDefault().postSticky(new NewLocalTemperature(temp));

                SensorData sensorData = new SensorData();
                sensorData.setValue(temp);
                sensorData.setDate(LocalDateTime.now().toString());
                service.publishSensorData(salaP.getUuid(), "temperatureABC", sensorData);
            }).start();
        });

        new Thread(() -> {
            while(true) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Double d = new Random().nextDouble() * 100;
                Temperature t = new Temperature();
                t.setValue(d);
                t.setDate(LocalDateTime.now().toString());

                Message msg = new Message();
                msg.setServiceName("LocalTemperature");
                msg.setServiceValue(t);
                pub.publish(msg);

            }
        }).start();
    }

    private void cadastrarRecursos() {
        Resource resource = new Resource();
        resource.setStatus("active");
        resource.setLat(10.0);
        resource.setLon(10.0);
        resource = service.publishResource(resource);
        System.out.println(resource);
    }

}
