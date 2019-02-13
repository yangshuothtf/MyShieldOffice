# MyShieldOffice
support for android 8.0
Already tested on HUAWEI Mate9 EMUI 8.0/android 8.0 -- 2019.2.13

Feature list:

Monitor incoming/outgoing calls.
1.1 Save every call number even if more than 2 calls at same time.
1.2 Save missing call number.
Report call record via email whenever network available.
2.1 maintain a list for all calls recorded, send out when network available, and then delete files on list.
-- above done by 2018.1.26
Send/receive command via email. -- (done, 2018.2.8)
3.1 Check command email when first call after reboot -- done, 2018.4.3
3.2 If GPS location enabled, ignore the flag for first phone call check, and check command email when GPS info reported.
3.3 Set parameters via email for device name, monitor type, GPS location interval, GPS report time etc. 
Keep mailbox clean
4.1 Delete phone call email after sent -- (done,2018.2.9)
4.2 Delete GPS info email except the latest one.
4.3 Keep only one latest GPS info mail for each device.
Power saving, no GPS enable needed.
5.1 GPS locate trigger by alarmManager, every 10 minutes --done, 2018.4.12
5.2 GPS locate when diff more than 300 meters.
Speech-To-Text by XunFei SDK. -- done, 2018.5.7
To do list:
Accessibility for weChat
Auto-update in background
More config options like mailbox via command
Blacklist for fraud calls.
Bluetooth
Tensorflow
