FROM python:3.9 
WORKDIR /app
COPY . .
RUN pip install -r requirements.txt
CMD ["bash", "-c", "sleep 20 && python /app/src/FinnhubProducer.py"]