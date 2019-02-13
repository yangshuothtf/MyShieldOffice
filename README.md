# MyShieldOffice
support for android 8.0<br>
Already tested on HUAWEI Mate9 EMUI 8.0/android 8.0 -- 2019.2.13<br>
<br>
Feature list:<br>
<br>
Monitor incoming/outgoing calls.<br>
1.1 Save every call number even if more than 2 calls at same time.<br>
1.2 Save missing call number.<br>
Report call record via email whenever network available.<br>
2.1 maintain a list for all calls recorded, send out when network available, and then delete files on list.<br>
-- above done by 2018.1.26<br>
Send/receive command via email. -- (done, 2018.2.8)<br>
3.1 Check command email when first call after reboot -- done, 2018.4.3<br>
3.2 If GPS location enabled, ignore the flag for first phone call check, and check command email when GPS info reported.<br>
3.3 Set parameters via email for device name, monitor type, GPS location interval, GPS report time etc. <br>
Keep mailbox clean<br>
4.1 Delete phone call email after sent -- (done,2018.2.9)<br>
4.2 Delete GPS info email except the latest one.<br>
4.3 Keep only one latest GPS info mail for each device.<br>
Power saving, no GPS enable needed.<br>
5.1 GPS locate trigger by alarmManager, every 10 minutes --done, 2018.4.12<br>
5.2 GPS locate when diff more than 300 meters.<br>
Speech-To-Text by XunFei SDK. -- done, 2018.5.7<br>
To do list:<br>
Accessibility for weChat<br>
Auto-update in background<br>
More config options like mailbox via command<br>
Blacklist for fraud calls.<br>
Bluetooth<br>
Tensorflow<br>
