# SDApp
Copyright (C) 2016 Jhansi Tavva. All Rights Reserved.
## Developer
**Jhansi Tavva**
## Version
1.0
## platform Requirements
Usual android setup

1. [Download and install the latest JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html)

2. [Download and install the Android SDK](http://developer.android.com/sdk/)

## How to run

### If you're using Android Studio...###
1. Open Android Studio and launch the Android SDK manager from it (Tools | Android | SDK Manager)
1. Check that these two components are installed and updated to the latest version. Install or upgrade
   them if necessary.
   1. *Android SDK Platforms*
   1. *Android SDK Build-tools*
1. Return to Android Studio and select *File->New->Import Project*
1. Select the **SDApp** directory


### Build directly from the command line if you prefer:###

    cd /path/to/SDApp
    ./gradlew build
    
    
## Android Version Targeting

DesignChallange is currently built to work with Android API 23(Marshmallow). **However**, DesignChallange's minimum SDK support is 15(Ice Cream Sandwich).

## Usage Instructions
SDApp application is developed as a part of codding challange.
Which scans the whole SDCard and displays Largest 10 files, Averae file size and frquency of top 5 files

##License
[License](https://github.com/jhansi/DesignChallenge/blob/master/LICENSE-2.0.txt)
