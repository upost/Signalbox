# Signalbox

An Android SRCP client

I control my model railroad with the Simple Railroad Protocol. This client runs nicely on tablets.
Construct your layout, control switches and trains.
Tested with the SRCPd server on a Raspberry Pi and DDL connection thru a Märklin Delta divice 
with a medium sized DCC H0 scale layout.
For SRCPd, see: http://srcpd.sourceforge.net

## Limitations

This is a quite simple implementation meant for users interested in building an open source based
digital model railroad.

The app (nor the server) cannot know switch settings after a server reboot.

Currently you cannot use two instances of the app against one server, because instance 1 does not receive
any data about what instance 2 does. This results in contradictory speed commands and incorrectly set switches.

Sometimes GA won't be initialized correctly (does not react on switching it). In that case, open
the GA's dialog, tap save.

## Import/Export

You export your layout using the About dialog (tap the logo).
To import or recover, name the file signalbox.json and restart the app, afterwards rename or remove the file. 

## Disclaimer

Source code is under Apache 2 License. Use Android Studio 3.x to build.

I cannot be made responsible for any damage the app could do to your model railroad.

