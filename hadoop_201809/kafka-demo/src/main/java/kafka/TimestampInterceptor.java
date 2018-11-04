package kafka;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Map;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/30 10:06
 */
public class TimestampInterceptor implements ProducerInterceptor<String, String> {

    //onSend方法会封装到send方法中，在发送前调用
    @Override
    public ProducerRecord<String, String> onSend(ProducerRecord<String, String> record) {
        String newValue = System.currentTimeMillis() + "-" + record.value();
        return new ProducerRecord<>(record.topic(), record.key(), newValue);
    }

    //该方法会在消息被应答前或消息发送时调用,通常是在produer回调之前调用
    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
        System.out.println("onAcknowledgement.. ");
    }

    @Override
    public void close() {
        System.out.println("========timestamp end===========");
    }

    @Override
    public void configure(Map<String, ?> configs) {
        System.out.println("timestamp interceptor .. ");
    }
}
