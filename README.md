# VAC
## Overview
VAC (Video-Audio Converter) is a student project for educational purposes. Our main goal was learning how to use network technologies (TCP/IP protocols) in practice. We also developed application by adding database, which collects data about client, server, data usage etc. We also made logging interface which works but is not even close to be safe. Our Flask server is equipped with swagger specification generation for ease of getting familiar with its methods. Main functionality of VAC (as full name suggests) is interpreting video as audio spectogram. To achieve this we have created three programs:
* Android client
* Flask server
* RTP streamer (Java)

## How does it works
Once you have installed android app and both server and streamer are running, you have to either login or register in the system. Communication with server for authorization purposes is accomplished by sending HTTP requests with data passed by arguments. Succesful login will move application to menu with two main parts.
### Database
Press DATABASE button to start the databases part. When you enter, you will see buttons which represents particular types of requests for statistics. Every statistic is personalized, assigned to your account. While pressing button, you send HTTP request to server, server executes SQL query in database and sends back response in JSON form. JSON is interpreted and displayed on the screen in user-friendly form.
### Camera
Press CAMERA button to start the video-audio converting part, remember to press CONNECT button first, otherwise client wouldn't be able to obtain TCP/RTP ports. Ports are send by server in HTTP response, JSON form. While using CAMERA funcionality, android client sends frames via TCP connection to Flask server in order to process them and then server pass results to streamer, which sends sound stream back to the client using RTP.
###

## How to run
As our project implements primitive microservice architecture, it needs more than one program to run in order to achieve full functionality. In brief you have to run RTP streamer, docker with database container and Flask application to setup server. Then all you have to do is installing client application, log in and here you go!

## Used Tools
We used JetBrains IDE's (intelliJ IDEA, PyCharm) to program server applications and Android Studio for client. For database setup we used Docker platform and MS SQL container.

## Contributions
  * [Encode Image In Sound With Python](https://www.hackster.io/sam1902/encode-image-in-sound-with-python-f46a3f)
  * [jlibrtp](https://sourceforge.net/projects/jlibrtp/)
  
## Credits
[Wojciech Lulek (Zerkles)](https://github.com/Zerkles)<br>
[Tomasz Kiljańczyk (Gunock)](https://github.com/Gunock)
