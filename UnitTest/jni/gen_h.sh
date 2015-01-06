#! /bin/bash

javac ../src/com/catebloom/CateBloomJni.java
# mv *.class com/jni
javah -classpath ../bin/classes -d . com.catebloom.CateBloomJni
