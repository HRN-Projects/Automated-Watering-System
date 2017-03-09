// ITelephony.aidl.aidl
package com.internal.android.telephony;

// Declare any non-default types here with import statements

interface ITelephony {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */

    boolean hangCall();

    void answerIncoming();

    void silenceRinger();
}
