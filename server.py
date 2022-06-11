from asyncio.windows_events import NULL
import pyrebase
import os
import smtplib
import json

from email.mime.text import MIMEText
from docx2pdf import convert
from docx import Document


firebaseConfig={
  'apiKey': "AIzaSyB6CY7Efd3HdNQSSmxUYgKi3uDpCy9fRhQ",
  'authDomain': "newbluetoothbus.firebaseapp.com",
  'databaseURL': "https://newbluetoothbus-default-rtdb.firebaseio.com",
  'projectId': "newbluetoothbus",
  'storageBucket': "newbluetoothbus.appspot.com",
  'messagingSenderId': "174859247827",
  'appId': "1:174859247827:web:6e86af3fb23706260f3426",
  'measurementId': "G-GX3HD5P9ZP"
}

firebase = pyrebase.initialize_app(firebaseConfig)

db =firebase.database()
storage=firebase.storage()

check_time = []
check_id = []
check_bus = []
check_cost = []
name = []


def stream_handler_check(message):

  #print(message["event"]) 
  #print(message["path"]) 
  #print(message["data"]) 

  print("Working...")
  
  check_time = []
  check_id = []
  check_bus = []
  check_cost = []
  name = []
  

  if message["data"] == "cost":
    for i in range(message["path"].index('т ')+2,len(message["path"])-3):
      check_time.append(message["path"][i])
      #print(message["path"][i])
    #print(''.join(check_time))
    for i in range(1,29):
      check_id.append(message["path"][i])
      #print(message["path"][i])
    #print(''.join(check_id))
    users=db.child("User").get()
    for person in users.each():
      if person.key()==''.join(check_id):
        name.append(person.val()['name'])
    for i in range(30,32):
      check_bus.append(message["path"][i])
      #print(message["path"][i])
    # print(''.join(check_bus))
    for i in range(61,63):
      check_cost.append(message["path"][i])

    print(name)
    name = ''.join(name)

    document = Document()
    document.add_paragraph(
      name + ' | Время оплаты ' + 
      ''.join(check_time) + ' | Маршрут ' + 
      ''.join(check_bus) + ' | Сумма оплаты ' + 
      ''.join(check_cost) + " руб")

    check_time[2] = check_time[5] = check_time[14] = check_time[17] = '-'
    check_time[10] = check_time[11] = '_'

    docx_name=name+'__'+ ''.join(check_time)+'.docx'
    pdf_name=name+'__' + ''.join(check_time)+'.pdf'

    document.save(docx_name)
    convert(r"C:/УЧЕБА/Project/"+docx_name,r"C:/УЧЕБА/Project/"+pdf_name)
    
    storage.child(pdf_name).put(pdf_name)
    storage.child(pdf_name).put(pdf_name)

    os.remove(docx_name)
    os.remove(pdf_name)

    #print(storage.child(pdf_name).get_url(None))
    data={'last_check':""+storage.child(pdf_name).get_url(None)}
    db.child("User").child(''.join(check_id)).update(data)


    people = db.child("User").get()
    for person in people.each():
      if person.key() == ''.join(check_id):
        data={'check_list':""+
        storage.child(pdf_name).get_url(None) + "\n " + 
        person.val()['check_list']}
        db.child("User").child(''.join(check_id)).update(data)



def stream_handler_history(message):
  #print(message["event"]) # put
  #print(message["path"]) # /-K7yGTTEp7O549EzTYtI
  #print(json.dumps(message["data"])) # {'title': 'Pyrebase', "body": "etc..."}
  
  

  if message["data"] == "1":
    print(message["event"]+"\n")
    print(message["path"]+"\n")
    print(message["data"])
    #print(message["event"]) 

    history_id = []
    for i in range(2,30):
      history_id.append(json.dumps(message["path"])[i])
    history_id = ''.join(history_id)
    print(history_id)

    sender_email = "bluetoothbus.autoemailsender@gmail.com"
    password1 = "1234qwe/"
    password = "rorrswuympftkxna"
    text = ""
    
    people = db.child("User").get()
    for person in people.each():
      if person.key() == history_id:
        text = person.val()['check_list']
        rec_email = person.val()['email']

    msg = MIMEText(text)
    server = smtplib.SMTP('smtp.gmail.com', 587)
    server.starttls()
    server.login(sender_email, password)
    server.sendmail(sender_email, rec_email, "Subject: BluetoothBus payment history\n{}"+msg.as_string())
    db.child("History_requests").child(history_id).remove()

check_stream = db.child("Check").stream(stream_handler_check)

history_stream = db.child("History_requests").stream(stream_handler_history)



