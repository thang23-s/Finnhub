import os
import ast
import json
import websocket
from utils.functions import *

class FinnhubProducer:
    
    def __init__(self):
        
        print('Environment:')
        for k, v in os.environ.items():
            print(f'{k}={v}')

        self.finnhub_client = load_client(os.environ['FINNHUB_API_TOKEN'])
        self.producer = load_producer(os.environ['KAFKA_SERVER'])
        self.avro_schema = load_avro_schema('src/schemas/trades.avsc')
        self.tickers = ast.literal_eval(os.environ['FINNHUB_STOCKS_TICKERS'])
        self.validate = os.environ['FINNHUB_VALIDATE_TICKERS']

        websocket.enableTrace(True)
        self.ws = websocket.WebSocketApp(f'wss://ws.finnhub.io?token={os.environ["FINNHUB_API_TOKEN"]}',
                              on_message = self.on_message,
                              on_error = self.on_error,
                              on_close = self.on_close)
        self.ws.on_open = self.on_open
        self.ws.run_forever()

    def on_message(self, ws, message):
        message = json.loads(message)

        if message.get("type") != "trade":
           return

        if "data" not in message or message["data"] is None:
           return

        avro_message = avro_encode(
         {
            "data": message["data"],
            "type": message["type"]
         },
        self.avro_schema
    )

        self.producer.send(os.environ['KAFKA_TOPIC'], avro_message)

    def on_error(self, ws, error):
        print(error)

    def on_close(self, ws):
        print("### closed ###")

    def on_open(self, ws):
        for ticker in self.tickers:
            if self.validate=="1":
                if(ticker_validator(self.finnhub_client,ticker)==True):
                    self.ws.send(f'{"type":"subscribe","symbol":"{ticker}"}')
                    print(f'Subscription for {ticker} succeeded')
                else:
                    print(f'Subscription for {ticker} failed - ticker not found')
            else:
                self.ws.send(f'{{"type":"subscribe","symbol":"{ticker}"}}')
if __name__ == "__main__":
    FinnhubProducer()