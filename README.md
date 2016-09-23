# URL Shortener
Android application that can make URLs shorter using Google URL Shortener API (goo.gl).

In this app I used following libraries and technologies:
- Gson library (to serialize data before sending request);
- Android Asynchronous HTTP Client library (loopj) to send POST requests;
- Requests history are saved into SQLite database and displayed as a list using adapter (preliminarily saved to collection);
- For each generated short URL could be requested QR code and displayed as image;
- Link can be written manually or pasted from clipboard, generated short URL also could be copied to clipboard;

P.S. I know that there is a lot of spaghetti code now in Main Activity and I should do a lot of refactoring with my DB and adapter methods. And it will be done in nearest future.
