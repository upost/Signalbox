# Signalbox

An Android SRCP client

I control my model railroad with the Simple Railroad Protocol. This client runs nicely on tablets.
Construct your layout, control switches and trains.
Tested with the SRCPd server on a Raspberry Pi.
http://srcpd.sourceforge.net

This app is currently total Beta and thus not available on Google Play.

## Limitations

The app (nor the server) cannot know switch settings after a server reboot.

Currently you cannot use two instances of the app against one server, because instance 1 does not receive
any data about what instance 2 does. This results in contradictory speed commands and incorrectly set switches.

Sometimes GA won't be initialized correctly (does not react on switching it). In that case, open
the GA's dialog, tap save.

## Import/Export

You export your layout using the About dialog (tap the logo).
To import or recover, name the file signalbox.json and restart the app, afterwards rename or remove the file. 

## Disclaimer

Source code is under LGPL. Use Android Studio 3.0 to build.

If you would like to have the APK without building it for yourself, contact me.

I cannot be made responsible for any damage the app could do to your railroad.

