Feedback
====

Feedback is an example application which shows you how to help users provide feedback about the application and/or for debugging.

Feedback allows the user to submit feedback using a Google account on the device or anonymously, enter multiple lines of text, and gives the option to include a device screenshot and log file.

The log file includes the following information.

|Field               |Example                |
|--------------------|-----------------------|
|Date/Time           |2014-10-19 13:37:45.254|
|Package             |com.example.feedback   |
|Code                |1                      |
|Version             |1.0.0                  |
|Network             |AT&T                   |
|Model               |SAMSUNG-SGH-I747       |
|Brand               |samsung                |
|Device              |d2att                  |
|Product             |cm_d2lte               |
|Build ID            |KTU84Q                 |
|Build Type          |userdebug              |
|SDK                 |19                     |
|Release             |4.4.4                  |
|Build Version       |a2f8bbb873             |
|Build Codename      |REL                    |
|Build Board         |MSM8960                |
|Applications Running|...                    |

The images below show the feedback screen in portrait orientation and the preview dialog of the portrait screenshot in landscape orientation.

<center><img src="http://www.tylerheck.com/github_android_feedback_01.png" alt="Screenshot Image 1" width="360px"></center>

<center><img src="http://www.tylerheck.com/github_android_feedback_02.png" alt="Screenshot Image 2" width="640px"></center>


ActivityFeedback.java
====

Edit the three lines shown below in the **ActivityFeedback.java** file.

```java
private final static String RECEIVER = "receiver@email.com";
private final static String SENDER = "sender@gmail.com";
private final static String PASSWORD = "password";
```

The `RECEIVER` variable is the email address you want to receive the feedback.  The `SENDER` variable is a dummy Gmail address used to send the feedback.  The `PASSWORD` variable is the password for the dummy Gmail address.  The dummy account must be a Gmail address.  **DO NOT** use your personal Gmail account as the dummy account unless you want opportunistic users to potentially discover your personal Gmail account password.


License
====

    Copyright (C) 2014 The Android Open Source Project, Tyler Heck
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License
