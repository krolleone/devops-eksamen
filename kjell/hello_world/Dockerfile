FROM python:3

ADD app.py /

COPY requirements.txt ./
RUN pip install -r requirements.txt

CMD [ "python3", "./app.py" ]