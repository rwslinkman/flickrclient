FlickrClient
Rick Slinkman
rick.slinkman@fourtress.nl

This app is a basic implementation of the Flickr API.
It contains 3 API calls in order to get and display images from Flickr.
There is an infinitely scrolling RecyclerView  to display the list of images.
By clicking on an image, you will see the details of the image.
Shake your phone when you see the images, it will shuffle the list order.

Architecture
    The architecture for this app is based on 3 mayor focus points.
    -   Flickr API
    -   Data transport
    -   Separation of responsibility

    The classes that interact with the Flickr API were put into a separate package.
    This should make the package reusable in other parts of the app.
    If the code is appreciated, you can easily extract it into a separate component or library.
    The content of this package is based on the AsyncTask from Android.
    I have written an abstract class that can be reused to do HTTP request using an AsyncTask.
    All other classes extend this class and leave the execution of the actual HTTP request to the abstract class.
    This leaves handling the input and output to the extending class.

    The Flickr API package also contains the 'model' sub package.
    These are the POJOs that represent the JSON results from the Flickr API.
    I have placed the 'model' within the 'flickr' packages because they originate there.
    They have only got accessor methods because their values do not change later.
    The GSON library instantiates them and puts the JSON value in the correct class attribute.

    In order to maintain a neat and tidy data transport inside the app, I have created the FlickrClient class.
    This class extends Application and acts as the main app object.
    I have used it to hand a data object from one activity to the other.
    Before using this implementation, I have used the Parcelable interface.
    The project became really slow when using Parcelable, because (amongst others) a Bitmap had to be serialized and deserialized.
    Using the Application class offered a nice way to gain performance.
    The Activity classes were moved to a separate package to keep the overview clean.
    They are responsible for handling the OS lifecycle events and user input events.

    The responsibilities within the app are tidily separated.
    I found it important to separate the drawing of list items from the management of list item collections.
    Therefor I have used the Presentable library which I wrote myself. The library is available on GitHub.
    Building the UI and setting the UI's values is done in the Presenter class.
    The adapter (which comes with the library) handles the list item collection.
    The PresentableAdapter is a "one size fits all" solution that takes custom Presenters.

    Classes that were used by the activities but do not belong to a distinct package are put in the 'util' package.
    Amongst others, the ShakeListener is there. This listener monitors the accelerometer output.
    When the accelerometers measure a acceleration higher than the threshold, an event is sent.
    Small and reusable methods are statically available through the Utils class.

3rd party dependencies
- com.android.support:design v24.2.0
    This lib includes additional Android widgets. The search button in the UI is a FloatingActionButton.
    These buttons are commonly used as they are part of Google's Material Design ideology.

- com.android.support:cardview-v7 v24.2.0
    The CardView is a new component with its origin in the Material Design ideology.
    It works well in a list. I've used it as a list item container.
    A CardView gives the app a nice, modern look.

- com.android.support:recyclerview-v7 v24.2.0
    The list of images in the app is a RecyclerView. The RecyclerView is the successor of the ListView.
    Unique about the RecyclerView is that it manages the list items individually.
    This is useful when displaying images as they may take a while to render.
    Managing them individually makes sure that only parts of the list need a redraw.

- com.android.support:appcompat-v7 v24.0.0
    Required to display a RecyclerView. It also helps to display the UI on devices with a lower API.

- nl.rwslinkman.presentable:presentable v1.5
    A library written by myself that is available via jcenter. More information is available on GitHub.
    I've written this library after some experience with Android TV.
    With Android TV, it is common to use the Presentable design pattern.
    On mobile, this was only partially possible by using multiple Adapters.
    The library offers a single adapter that takes a Presenter as its parameter.
    The Presenter only builds the view and is not responsible for managing the list item objects.
    I think it is important to separate responsibilities between classes.
    In this implementation, the Adapter manages the item (object) list and
    the Presenter decides how the individual item is displayed.

- com.google.code.gson:gson v2.4
    I've used this library to convert JSON strings to Java POJOs.
    In my opinion it is preferable to work with POJOs instead of composite JSON objects.
    By doing this, you can analyze the input early.

- com.github.pwittchen:infinitescroll v0.0.1
    A simple library to detect when the RecyclerView has reached its bottom.
    I used it in order to not repeat after other developers and to save time.