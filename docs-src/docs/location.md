For application which need to access the device location can use our **Easy Location** module, which will make it easy to get the information about the last known location like latitude, longutude and address. It also uses the **Easy Permissions** while requesting for the location data, which will make it easy with runtime permissions.

Developers can use the `LocationTracker` class in the activity where they want to request for the permissions. This class works along with the `PermissionRequestAdapter` to handle the runtime permission requesting for devices with Marshmallow and above.

