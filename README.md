![alt tag](http://i.imgur.com/4AyPrAu.jpg)

This is an Android application for a magic mirror.
I had an old Nexus 7 (2012) lying around collecting dust and got the idea from [HomeMirror by Hannah Mitt](https://github.com/HannahMitt/HomeMirror).

I started this project because I wanted to explore the MVP pattern for Android and Hannah's app did not have all the functionality I needed. Feel free to make this application look good on your own device (it was designed for the Nexus 7). Have any great ideas to make this more configurable or usable on more devices? 
Submit a pull request!

###Mirror

![alt tag](http://nielsmasdorp.github.io/Speculum-Android/img/mirror_shot3.jpg)

###Application screens

![alt tag](http://i.imgur.com/SMdJDOd.jpg)

Features
====
* Date and time
* Switch between two custom layouts (Verbose and Simple)
* Weather powered by Forecast.IO
  * Metric or Imperial
  * Wind information
    * Temperature
    * Speed
    * Direction
  * Humidity
  * Pressure
  * Visibility
  * Four day forecast
* Your upcoming Google Calendar event
* One of the top posts of your favorite subreddit
* Interact with the mirror via voice commands (pull up map of your home, ask for a joke, update the visible data or be creative and implement your own)

How do I get started
====

1. Clone project in Android Studio
2. Select JDK8 in Project Settings
3. Make nescessary adjustments for your device
4. Go to [Forecast.io](https://developer.forecast.io/) and register
5. Go to [Google Static Maps API](https://developers.google.com/maps/documentation/static-maps/) and register for a browser key (this is optional, only do this if you want to have the mirror show a maps view of your home on command)
6. Create a `keys.xml` file in `/res/values/` (sample found below these steps)
7. Run on device or generate .APK
8. Turn on "Stay Awake" in Developer Options on your device
9. If you turned on voice recognition change the Text to Speech language to English in the language options on your device

Example `keys.xml`
====

```java
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="forecast_api_key">your forecast io api key</string>
    <string name="static_maps_api_key">your static maps api key</string>
</resources>
```

Update data with voice command
====

When you've turned on the voice command option in the setup screen you can talk to your device and make it update the data. To do this wake the device up by saying **hello magic mirror**, the device will then respond with **hello there, how can I help you?**. You can then speak commands, for example: say **update** and the data will refresh, if you don't say anything for a minute the device will go back to sleep by itself, you can also force this by saying **go to sleep**. There are also some other built in commands like **tell me a joke** which tells you a joke and **show my location** which pulls up a maps view of your home (if you did all the steps in the tutorial above). A small demonstration of the concept can be found below.

[![Alt text for your video](http://img.youtube.com/vi/bRPGOPEYoYI/0.jpg)](https://www.youtube.com/watch?v=bRPGOPEYoYI)

###Add your own commands

Although this is a bit out of the scope for this document I had a hard time understanding and configuring the Pocketsphinx library so I figure this might come in handy when you want to add your own functions to this application.
The `SpeechRecognizer` in this application has two modes; listen to one keyphrase (the wake up phrase) and listen to a list of keywords. The keyphrase to wake up the device is pretty straight forward, if you want to edit the phrase go to `Constants.java`,  and change 

```java
public static final String KEYPHRASE = "hello magic mirror";
```

to anything you want. When this phrase is recognized the `SpeechRecognizer` will switch to listening to any of the commands in a grammar file you specified. I specified the `commands.gram` file in the initialization of the `SpeechRecognizer` as shown below.

```java
// Create grammar-based search for command recognition
File commands = new File(assetsDir, "commands.gram");
recognizer.addKeywordSearch(Constants.COMMANDS_SEARCH, commands);
```

The actual grammar file can be found in `/assets/sync/commands.gram`, you can change the command list or create a new file in that folder and pass it to the `SpeechRecognizer` at initialization. The grammar file must have this format:

```
update/1e-1/
go to sleep/1e-40/
tell me a joke/1e-40/
show my location/1e-40/
```
The number between the `//` is the threshold for detecting the keywords, the general rule is; the longer the keyword the bigger the threshold (e.g. one-word phrases `/1e-1/` and four-word phrases `/1e-40/`) but you can experiment with these values yourself. Also, every word in the commands you choose **must** exist in the dictionary file, which can be found in `/assets/sync/cmudict-en-us.dict`.

After you add your commands add them individually to `Constants.java` as well.

```java
public static final String KEYPHRASE = "hello magic mirror";
public static final String UPDATE_PHRASE = "update";
public static final String SLEEP_PHRASE = "go to sleep";
public static final String JOKE_ON_PHRASE = "tell me a joke";
public static final String MAP_PHRASE = "show my location";
```

Now you set your `SpeechRecognizer` to listen to the commands list.

```java
recognizer.stop();
//second parameter is the time to listen specified in milliseconds
recognizer.startListening(Constants.COMMANDS_SEARCH, TimeUnit.MINUTES.toMillis(1);
```
Whenever the `SpeechRecognizer` recognizes a command I pass the command for processing.

```java
 @Override
 public void onPartialResult(Hypothesis hypothesis) {
    if (hypothesis == null)
        return;
    processCommand(hypothesis.getHypstr());
}
```
Now you only have to assign your custom actions to the commands in the presenter. You can assign your own actions to the commands in the `processCommand()` method in the `MainPresenterImpl.java`.

```java
@Override
public void processCommand(String command) {
     switch (command) {
         case Constants.KEYPHRASE:
            // start listening for commands
            setListeningMode(Constants.COMMANDS_SEARCH);
            break;
         case Constants.SLEEP_PHRASE:
             // listen to wake up phrase again
             setListeningMode(Constants.KWS_SEARCH);
             break;
         case Constants.UPDATE_PHRASE:
             // update data and keep restart recognizer to listen for keywords
             setListeningMode(Constants.COMMANDS_SEARCH);
             break;
        }
    }
}
```

The way you make the `SpeechRecognizer` listen to the wake up phrase is similar to the commands method only this time do not pass a time to listen value as parameter.

```java
recognizer.stop();
recognizer.startListening(Constants.KWS_SEARCH);
```

After changing anything related to the commands **reinstall the application** wake up the device and enjoy!

###Thresholds
More info can be found in the [Pocketsphinx  tutorial](http://cmusphinx.sourceforge.net/wiki/tutoriallm).

###Other languages and dictionaries
I have not experimented with this, however it is possible to create your own dictionary file. Please refer to the [CMU Sphinx tutorial](http://cmusphinx.sourceforge.net/wiki/tutorial).

How I made my mirror
====

Since pre-made two way mirrors are very expensive and hard to get by in my country (The Netherlands) I decided to make my own.
I bought some polished transparent plexiglass (60x40cm and 4mm thick) and some two way mirror foil normally used on windows for privacy. I built a wooden frame around it and used some small pieces of wood to hold the tablet in place (I left an opening on the bottom for the charger and on the top to expose the microphone). I then painted the back of the plexiglass black and that's it!

Thanks to
====

#####[gijsdewit](https://github.com/gijsdewit) for **awesome** designs

###Used libraries
* [Android Design Library](http://developer.android.com/tools/support-library/index.html)
* [Retrofit](https://github.com/square/retrofit)
* [RxAndroid](https://github.com/ReactiveX/RxAndroid)
* [Dagger2](http://google.github.io/dagger/)
* [Butterknife](https://github.com/JakeWharton/butterknife)
* [Assent](https://github.com/afollestad/assent)
* [Material Dialogs](https://github.com/afollestad/material-dialogs)
* [Picasso](http://square.github.io/picasso/)
* [Pocketsphinx](http://cmusphinx.sourceforge.net/wiki/tutorialandroid)

License
====
```
The MIT License (MIT)

Copyright (c) 2016 Niels Masdorp

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
