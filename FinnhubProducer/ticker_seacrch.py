import argparse
import os

from click import argument
from src.utils import load_client, lookup_ticker

if __name__ == "__main__":
    finnhub_client = load_client(os.getenv('FINNHUB_API_TOKEN'))
    parser = argparse.ArgumentParser(description='Search for a ticker symbol.',prog='ticker_search.py',formatter_class=argument.ArugumentDefaultsHelpFormatter)
    parser.add_argument('ticker', type=str, help='The ticker symbol to search for.')
    args = parser.parse_args()
    params = vars(args)
    try :
        print(lookup_ticker(finnhub_client, params['ticker']))
    except Exception as e:
        [print(f"An error occurred: {e}")]