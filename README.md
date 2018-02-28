# RabbitMQ bitfinex stream

Start a MongoDB instance, can be done as follows

```bash
docker run -p 27017:27017 --name some-mongo -d mongo
```

You can also run a local instance and query

```bash
docker run -it --link some-mongo:mongo --rm mongo sh -c 'exec mongo "$MONGO_PORT_27017_TCP_ADDR:$MONGO_PORT_27017_TCP_PORT/test"'
```

Once records available you can query from MongoDb

```js
use kaizen7ExchangeDb

db.historicTickers.aggregate([
{$match : {"timestamp": {$gte: NumberLong("1515920290791")}}},
{$limit: 10000},
{
        $group : {
        	_id: "$pair", 
        	timestamp: {$push:"$timestamp"}, 
        	bids: {$push:"$bestBid"}, 
        	ask: {$push:"$bestAsk"}
        }
}])
db.historicTickers.count()
```

1. Start the gateway application (subscribes from bitfinex, publishes to rabbitMQ)
1. Start the persistence application (subscribes from bitfinex, persists to MongoDb)

