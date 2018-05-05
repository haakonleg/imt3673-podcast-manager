# imt3673-podcast-manager
Project for IMT3673 Mobile Development

## Author
Håkon Legernæs

## Features
* Log in and register a user using Firebase
* Save podcasts URLs to a list which is synced across devices
* Listen to podcast episodes in the app or in the background
* Download podcast episodes to the local storage and listen offline
* Explore the most popular and top rated podcasts which other users of the app have added, and add them to your own list

**YouTube demo**: https://youtu.be/xsvCOeZHhis

**Trello board**: https://trello.com/b/sAWoGFWo/imt3673-podcast-manager

## Report

### Project Idea
The idea is to create a native Android app where the user can add RSS or Atom feeds containing podcasts with audio files. These will be saved and shown in the main drawer menu of the app. The feeds will be synced and the user can listen to episodes by clicking on them. The user can also choose to download audio files to the local storage and listen to them when there is no internet avilable. It is possible for users to diplay podcasts subscribed to by other users, and post comments and rate podcasts by quality. These feeds can be sorted by the user according to popularity and rating, and can easily be added to his list. The user must register a username and password and authenticate via Firebase and his URLs to podcasts will be saved in the real-time database, along with other data such as settings, ratings and comments.

### Organization
The structure of the code is organized by type, with the top level package used for classes for activities and fragments. In addition to this there are 4 other packages used for organization of code. The package "database" contains classes relating to the local SQLite database (or Room library). The second, "models" contains all data model classes for the project, such as "User", "Podcast" and "PodcastEpisode". The package "parsers" contains code related to feed parsing, with a base class called "Parser" containing common code used among all XML parsers. The class "RSSParser", is used for parsing podcasts using RSS. The next package "tasks" contains classes which extends the class "Task". These classes are tasks that should be performed in a background thread, which can return some value. These tasks objects are passed to the "ThreadManager" class, which executes them using a cached thread pool.

### Register/Login
This app requires logging in with a registrered user using e-mail and password (e-mail can be fake). When the app first starts, a login activity is shown, where the user can login or register a new user. After logging in, future launches of the app will not require logging in again as the app remembers sign in details (using firebase). User authentication is performed using Firebase.

### Development
When developing the app I used a Trello board, with three sections: "Backlog", "In Progress" and "Done" for tasks. The board can be found here: https://trello.com/b/sAWoGFWo/imt3673-podcast-manager. I first laid out and prioritized the most important features, such as a login/register, XML parsers and a background service for playback of podcast episodes. Later I added more tasks while development was in progress if I saw the need for something. The hardest part during development was probably implementing the background service for episode playback, I followed suggestions from Google for building a media app (https://developer.android.com/guide/topics/media-apps/audio-app/building-an-audio-app), but it took some time to get it working because it was a bit complicated especially to get the service to communicate with the UI. One of the biggest problems was getting the playback time indicator to sync with the service, but I solved this eventually by just sending out an update every second to the UI. Some of the more easier parts was implementing the parser for the podcast feeds (using XmlPullParser) as I have experience with this from before when I created a news reader, and it was quite similar.

### Future Improvement
There are some issues that are currenttly missing that I would like to add in the future to improve the app. One of these is pagination for the "explore podcasts" list (where you view podcasts from other users). As there can be potentialy hundreds of podcasts, pagination should be implemented to improve load time and memory usage. One other thing that is missing is a parser for podcasts using the ATOM specification. The reason this is not implemented is that I did not find any podcasts using this format, but i'm sure they exist (I only checked maybe 20 podcasts).

## Linter warnings
There are a few linter warnings which have not been addressed, but there are reasons for that. There are two warnings against performance, the first is a warning about using HashMap instead of SparseArray in MainActivity.class, the reason I have not changed this to SparseArray is because I need a feature from HashMap where I can copy it to an ArrayList quickly (eg. "new ArrayList<>(hashmap.values())"). This is used a few places and I do not see the need to create a workaround for this using SparseArray as I don't think the benefits are enough. The other warning complains about a "very long vector path" for the vector "ic_settings_24dp.xml". This is a bit strange as the vector is auto generated using Android studio and nothing I have control over. There are also several "probable bugs" (over 60 of these). All of these are instances where there might be a NullPointerException, but most places this is not really true (for example when calling GetActivity from a fragment that is attached to an activity) and I have never had a NullPointerException in any of these cases, so I choose to ignore them. There are also a lot of "spelling" warnings (almost 600!), which is quite ridicilous as I am not writing an essay, I am coding.
## Libraries/APIs used
* [Firebase](https://firebase.google.com/docs/android/setup)
* [XmlPullParser](https://developer.android.com/reference/org/xmlpull/v1/XmlPullParser.html)
* [Room Persistence Library](https://developer.android.com/topic/libraries/architecture/room.html)
* [MediaPlayer](https://developer.android.com/guide/topics/media/mediaplayer.html)
* [Glide](https://github.com/bumptech/glide)
