package data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class OrderBook {

    private long timeStamp;
    private String exchange;
    private String marketName;
    private BigDecimal bestAsk;
    private BigDecimal bestBid;

    public long getTimeStamp() {
        return timeStamp;
    }

    public OrderBook withTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
        return this;
    }

    public String getMarketName() {
        return marketName;
    }

    public OrderBook withMarketName(String marketName) {
        this.marketName = marketName;
        return this;
    }

    public BigDecimal getBestAsk() {
        return bestAsk;
    }

    public OrderBook withBestAsk(BigDecimal bestAsk) {
        this.bestAsk = bestAsk;
        return this;
    }

    public BigDecimal getBestBid() {
        return bestBid;
    }

    public OrderBook withBestBid(BigDecimal bestBid) {
        this.bestBid = bestBid;
        return this;
    }

    public String getExchange() {
        return exchange;
    }

    public OrderBook withExchange(String exchange) {
        this.exchange = exchange;
        return this;
    }
}
