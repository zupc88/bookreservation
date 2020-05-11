package bookrental;

import bookrental.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverRevfailed_Statusupdate(@Payload Revfailed revfailed){

        if(revfailed.isMe()){
            System.out.println("##### listener Statusupdate : " + revfailed.toJson());
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverRevcanceled_Statusupdate(@Payload Revcanceled revcanceled){

        if(revcanceled.isMe()){
            System.out.println("##### listener Statusupdate : " + revcanceled.toJson());
        }
    }

}
