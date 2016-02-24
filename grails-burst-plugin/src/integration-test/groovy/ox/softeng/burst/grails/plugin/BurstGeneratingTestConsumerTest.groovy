package ox.softeng.burst.grails.plugin

import com.budjb.rabbitmq.RabbitContext
import com.budjb.rabbitmq.consumer.MessageContext
import com.budjb.rabbitmq.publisher.RabbitMessagePublisher
import geb.spock.GebSpec
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import grails.util.Holders
import grails.web.mime.MimeType
import org.springframework.context.ApplicationContext
import ox.softeng.burst.grails.plugin.test.BurstListeningConsumer
import ox.softeng.burst.grails.plugin.test.Test

/**
 * @since 18/02/2016
 */
@Rollback
@Integration
class BurstGeneratingTestConsumerTest extends GebSpec {

    RabbitMessagePublisher rabbitMessagePublisher
    BurstListeningConsumer burstListeningConsumer

    def setup() {
        // We use Spy so we can see the generated BuRST message XML
        burstListeningConsumer = Spy(BurstListeningConsumer)

        ApplicationContext applicationContext = Holders.grailsApplication.mainContext
        // Load and start the rabbit service, without starting consumers.
        RabbitContext rabbitContext = (RabbitContext) applicationContext.getBean('rabbitContext')
        rabbitContext.registerConsumer(burstListeningConsumer)
        rabbitContext.reload()

    }

    void 'testing unhandled exception'() {

        when: 'place a message on the exception queue'
        rabbitMessagePublisher.send {
            exchange = 'test'
            routingKey = 'test.exception'
            body = 'Unhandled Exception fail'
        }
        sleep(2000)

        then: 'the consumer will get it and generate an error which is seen by the burst listening consumer'
        1 * burstListeningConsumer.handleMessage({it.contains('BURST01')} as String, _ as MessageContext)
    }

    void 'testing handled exception'() {

        when: 'place a message on the exception queue'
        rabbitMessagePublisher.send {
            exchange = 'test'
            routingKey = 'test.exception'
            body = 'Handled Exception fail'
        }
        sleep(2000)

        then: 'the consumer will get it and generate an error which is seen by the burst listening consumer'
        1 * burstListeningConsumer.handleMessage({it.contains('INT01')} as String, _ as MessageContext)
    }

    void 'testing nested exception'() {

        when: 'place a message on the exception queue'
        rabbitMessagePublisher.send {
            exchange = 'test'
            routingKey = 'test.exception'
            body = 'Nested Exception fail'
        }
        sleep(2000)

        then: 'the consumer will get it and generate an error which is seen by the burst listening consumer'
        1 * burstListeningConsumer.handleMessage({it.contains('INT02')} as String, _ as MessageContext)
    }

    void 'testing validation errors'() {

        when: 'place a message on the validation queue'
        rabbitMessagePublisher.send {
            exchange = 'test'
            routingKey = 'test.validation'
            body = '<Test></Test>'
            contentType = MimeType.TEXT_XML.name
        }
        sleep(2000)

        then: 'the consumer will get it and generate an error which is seen by the burst listening consumer'
        1 * burstListeningConsumer.handleMessage({it.contains('VAL02')} as String, _ as MessageContext)

    }

    void 'testing valid'() {

        when: 'place a message on the validation queue'
        rabbitMessagePublisher.send {
            exchange = 'test'
            routingKey = 'test.validation'
            body = '<Test><name>test valid</name><type>test valid</type></Test>'
            contentType = MimeType.TEXT_XML.name
        }
        sleep(2000)

        then: 'the consumer will get it and generate an error which is seen by the burst listening consumer'
        0 * burstListeningConsumer.handleMessage(_ as String, _ as MessageContext)

        and: 'we should have an instance of test'
        Test.countByNameAndType('test valid', 'test valid')


    }

    void 'testing bad xml'() {

        when: 'place a message on the validation queue'
        rabbitMessagePublisher.send {
            exchange = 'test'
            routingKey = 'test.validation'
            body = '<Test><name>test valid</name><type>test valid</Test>'
            contentType = MimeType.TEXT_XML.name
        }
        sleep(1000)

        then: 'the consumer will get it and generate an error which is seen by the burst listening consumer'
        1 * burstListeningConsumer.handleMessage({it.contains('VAL01')} as String, _ as MessageContext)


    }

    void 'testing failed save'() {

        when: 'place a message on the validation queue'
        rabbitMessagePublisher.send {
            exchange = 'test'
            routingKey = 'test.validation'
            body = '<Test><name>test valid</name><type>test valid</type><failSave>true</failSave></Test>'
            contentType = MimeType.TEXT_XML.name
        }
        sleep(1000)

        then: 'the consumer will get it and generate an error which is seen by the burst listening consumer'
        1 * burstListeningConsumer.handleMessage({it.contains('BURST02')} as String, _ as MessageContext)


    }
}
