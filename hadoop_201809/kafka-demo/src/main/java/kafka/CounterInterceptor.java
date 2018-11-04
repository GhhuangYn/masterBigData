package kafka;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Map;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/30 10:12
 */
public class CounterInterceptor implements ProducerInterceptor<String, String> {

    private int errorCount = 0;
    private int successCount = 0;

    @Override
    public ProducerRecord<String, String> onSend(ProducerRecord<String, String> record) {
        return record;
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
        if (exception == null) {
            successCount++;
        } else {
            errorCount++;
        }
    }

    @Override
    public void close() {
        System.out.println("successful sent: " + successCount);
        System.out.println("error sent: " + errorCount);
        System.out.println("========Counter end===========");

    }

    @Override
    public void configure(Map<String, ?> configs) {
        System.out.println("Counter Interceptor..");
    }
}
