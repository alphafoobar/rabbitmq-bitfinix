package nz.kaizen7.gateway.config;

import com.google.common.collect.Lists;
import info.bitrich.xchangestream.binance.BinanceStreamingExchange;
import info.bitrich.xchangestream.bitfinex.BitfinexStreamingExchange;
import info.bitrich.xchangestream.poloniex.PoloniexStreamingExchange;
import java.util.Collections;
import nz.kaizen7.gateway.exchange.Stream;
import org.knowm.xchange.currency.CurrencyPair;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmpqConfig {

    @Bean
    public ConnectionFactory connectionFactory(
        @Value("${messaging.host:sidewinder.rmq.cloudamqp.com}") String host,
        @Value("${messaging.username:szjebtxq}") String username,
        @Value("${messaging.password:e9v-Y5A22OOrU0N3EcZWkincN1ZmziWT}") String password) {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setRequestedHeartBeat(30);
        connectionFactory.setConnectionTimeout(30000);
        return connectionFactory;
    }

    @Bean
    public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setExchange("tickerExchange");
        return rabbitTemplate;
    }

    @Bean
    public Stream bitfinexSender(RabbitTemplate rabbitTemplate) {
        return new Stream("orderbook.bitfinex.%s", rabbitTemplate,
            BitfinexStreamingExchange.class.getName(), "bitfinex",
            Collections.singletonList(CurrencyPair.BTC_USD));
    }

    @Bean
    public Stream binanceSender(RabbitTemplate rabbitTemplate) {
        return new Stream("orderbook.binance.%s", rabbitTemplate,
            BinanceStreamingExchange.class.getName(), "binance",
            Collections.singletonList(CurrencyPair.BTC_USDT));
    }

//    @Bean
//    public Stream gdaxSender(RabbitTemplate rabbitTemplate) {
//        return new Stream("orderbook.gdax.btc.usd", rabbitTemplate,
//            GDAXStreamingExchange.class.getName(), "gdax");
//    }

//    @Bean
//    public Stream okexSender(RabbitTemplate rabbitTemplate) {
//        return new Stream("orderbook.okex.btc.usd", rabbitTemplate,
//            OkCoinStreamingExchange.class.getName(), "okex");
//    }

    @Bean
    public Stream poloniexSender(RabbitTemplate rabbitTemplate) {
        return new Stream("orderbook.poloniex.%s", rabbitTemplate,
            PoloniexStreamingExchange.class.getName(), "poloniex",
            Collections.singletonList(CurrencyPair.BTC_USDT));
    }

    @Bean
    public TopicExchange fooExchange(AmqpAdmin ampqAdmin) {
        TopicExchange tickerExchange = new TopicExchange("tickerExchange");
        ampqAdmin.declareExchange(tickerExchange);
        return tickerExchange;
    }
}
