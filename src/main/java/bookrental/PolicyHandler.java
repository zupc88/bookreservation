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

    @Autowired
    ReservationRepository reservationRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverRevfailed_Statusupdate(@Payload Revfailed revfailed){
        if(revfailed.isMe()){
            System.out.println("##### Revfailed ##### : " + revfailed.toJson());
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverRevcanceled_Statusupdate(@Payload Revcanceled revcanceled){
        if(revcanceled.isMe()){
            System.out.println("##### revcanceled ##### : " + revcanceled.toJson());
            reservationRepository.findBybookid(revcanceled.getBookid())
                    .ifPresent(
                            reservation -> {
                                reservation.setStatus("Canceled");
                                reservationRepository.save(reservation);
                            }
                    );
            ;
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverRevsuccessed_Statusupdate(@Payload Revsuccessed revsuccessed){
        if(revsuccessed.isMe()){
            System.out.println("##### Successed ##### : " + revsuccessed.toJson());
            reservationRepository.findBybookid(revsuccessed.getBookid())
                    .ifPresent(
                            reservation -> {
                                reservation.setStatus("Successed");
                                reservationRepository.save(reservation);
                            }
                    );
            ;
        }
    }

}
