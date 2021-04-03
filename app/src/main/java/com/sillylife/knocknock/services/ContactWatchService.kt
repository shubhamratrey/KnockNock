package com.sillylife.knocknock.services

import android.app.Service
import android.content.Intent
import android.os.*
import android.provider.ContactsContract
import android.widget.Toast

class ContactWatchService : Service() {
    private var mServiceLooper: Looper? = null
    private var mServiceHandler: ServiceHandler? = null

    private inner class ServiceHandler(looper: Looper?) : Handler(looper!!) {
        override fun handleMessage(msg: Message) {
            try {
                //Register contact observer
                startContactObserver()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startContactObserver() {
        try {
            //Registering contact observer
            application.contentResolver.registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, ContactsObserver(Handler(Looper.getMainLooper()), applicationContext))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        val thread = HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND)
        thread.start()

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.looper
        mServiceHandler = ServiceHandler(mServiceLooper)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        val msg = mServiceHandler!!.obtainMessage()
        msg.arg1 = startId
        mServiceHandler!!.sendMessage(msg)

        // If we get killed, after returning from here, restart
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        // We don't provide binding, so return null
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            //Code below is commented.
            //Turn it on if you want to run your service even after your app is closed
            /*Intent intent=new Intent(getApplicationContext(), ContactWatchService.class);
            startService(intent);*/
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}