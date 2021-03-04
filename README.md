# openosrs-mobile
A bring-up of the RuneLite API for Android devices  
  
I decided to release the source. Big deal. Hopefully others will help with the refactor because I have been spending a lot of time on it.  
I also hope someone can get around to deobfuscating the packets for use with rsmod, Tomm is killing it as always.  
  
To build, I suggest you have a decent understanding of Android Studio. (https://developer.android.com/studio/)  
Download adb drivers for your android device, enable developer settings, and enable USB debugging. This is required for Android Studio to see your device.  
Configure Android Studio the same way you would IntelliJ (It is built off of it after all)  
  
Install SDK 26 & 29. You must accept the license terms to download and build.  
  
There are 2 ways you can run the final build:  
  
Copy vanilla osrs apk (current ver 194) to both client/lib & injector/lib (I don't wanna hear it)  
  
Gradle: 
Run the "inject assembleDebug" tasks. This will install the apk to your device, but won't launch it. Launch it manually.  
  
Studio:  
Run the "inject" gradle task then select the client module in the run configurations drop down, and click the start button. It will build, and launch automatically on your device.  
  
I will accept practically ANY correct api additions, but only complete functionality for "plugins"  
  
P.S. I know the tasks and libs are ugly atm, I was doing a lot of it manually, so moved on when it was simply working with gradle tasks. Not a priority so again, I don't want to hear it lol.  
