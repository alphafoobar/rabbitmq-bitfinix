package nz.kaizen7.gateway.exchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingExchangeFactory;
import io.reactivex.disposables.Disposable;
import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nonnull;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;

public class Stream implements Closeable {

    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private final StreamingExchange exchange;

    private final Set<Disposable> subscriptions = Sets.newConcurrentHashSet();
    private final Map<String, OrderBook> latestOrderBook = Maps.newConcurrentMap();
    private final AtomicInteger messageCounter = new AtomicInteger(0);
    private final String topic;
    private final ObjectMapper mapper = new ObjectMapper();

    private final RabbitTemplate rabbitTemplate;
    private final AtomicLong counter = new AtomicLong(0);
    private final String exchangeName;

    public Stream(String topic, RabbitTemplate rabbitTemplate, String exchangeClassName,
        String exchangeName, List<CurrencyPair> pairs) {
        this.topic = topic;
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = exchangeName;
        exchange = StreamingExchangeFactory.INSTANCE.createExchange(exchangeClassName);

        connect(pairs);
    }

    /**
     * Publish to rabbitMQ only every second.
     */
    @Scheduled(fixedDelay = 5000L)
    public void send() throws JsonProcessingException {
        Map<String, OrderBook> toRemove = Maps.newHashMap();
        for (Map.Entry<String, OrderBook> entry : latestOrderBook.entrySet()) {
            data.OrderBook ticker = createTicker(entry.getKey(), entry.getValue());
            String object = mapper.writeValueAsString(ticker);
            String format = String.format(topic, entry.getKey());
            this.rabbitTemplate.convertAndSend(format, object);
            logger.info(
                "publishing orderbook for topic [topic={}, updatesSinceLast={}, ticker=\"{}\"]",
                format,
                messageCounter.getAndSet(0), object);
            toRemove.put(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, OrderBook> entry : toRemove.entrySet()) {
            latestOrderBook.remove(entry.getKey(), entry.getValue());
        }
    }

    private data.OrderBook createTicker(String key, @Nonnull OrderBook orderBook) {
        data.OrderBook book = new data.OrderBook();

        book.withExchange(exchangeName);
        book.withTimeStamp(orderBook.getTimeStamp().getTime() / 1000);
        book.withMarketName(key);
        book.withBestAsk(getBestAsk(orderBook.getAsks()));
        book.withBestBid(getBestBid(orderBook.getBids()));

        return book;
    }

    private BigDecimal getBestBid(List<LimitOrder> orders) {
        BigDecimal price = null;
        for (LimitOrder order : orders) {
            if (price == null || price.compareTo(order.getLimitPrice()) < 0) {
                price = order.getLimitPrice();
            }
        }
        return price;
    }

    private BigDecimal getBestAsk(List<LimitOrder> orders) {
        BigDecimal price = null;
        for (LimitOrder order : orders) {
            if (price == null || price.compareTo(order.getLimitPrice()) < 0) {
                price = order.getLimitPrice();
            }
        }
        return price;
    }

    private long getCounter() {
        return counter.getAndIncrement();
    }

    public void connect(List<CurrencyPair> pairs) {
        // Connect to the Exchange WebSocket API. Blocking wait for the connection.
        exchange.connect().blockingAwait();
        logger.info("connected, subscribing to {} pairs", pairs.size());

        // Subscribe order book data with the reference to the subscription.
        for(CurrencyPair pair : pairs) {
            subscribe(pair);
        }
    }

    private void subscribe(CurrencyPair pair) {
        Disposable disposable = exchange.getStreamingMarketDataService()
            .getOrderBook(pair)
            .subscribe(orderBook -> {
                messageCounter.getAndIncrement();
                latestOrderBook.put(pair.toString(), orderBook);
            });
        subscriptions.add(disposable);
    }

    @Override
    public void close() throws IOException {
        logger.info("closing");
        for (Disposable subscription : subscriptions) {
            subscription.dispose();
        }
        exchange.disconnect().subscribe(() -> logger.info("Disconnected from the Exchange"));
    }
}
