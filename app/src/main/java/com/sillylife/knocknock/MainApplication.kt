package com.sillylife.knocknock

import android.app.Application
import android.content.IntentFilter
import android.net.ConnectivityManager
import com.sillylife.knocknock.database.KnockNockDatabase
import com.sillylife.knocknock.events.RxBus
import com.sillylife.knocknock.events.RxEvent
import com.sillylife.knocknock.managers.FirebaseRemoteConfigManager
import com.sillylife.knocknock.services.APIService
import com.sillylife.knocknock.services.AppDisposable
import com.sillylife.knocknock.services.ConnectivityReceiver
import com.sillylife.knocknock.services.IAPIService
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


class MainApplication : Application(), ConnectivityReceiver.ConnectivityReceiverListener {
    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        appDisposable.dispose()
        if (!isConnected) {
            appDisposable.add(Observable.timer(1, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        RxBus.publish(RxEvent.NetworkConnectivity(isConnected))
                    })
        } else {
            RxBus.publish(RxEvent.NetworkConnectivity(isConnected))
        }
    }

    @Volatile
    private var mIAPIService: IAPIService? = null
    @Volatile
    private var mIAPIServiceCache: IAPIService? = null
    @Volatile
    private var mKnockNockDatabase: KnockNockDatabase? = null

    private var connectivityReceiver: ConnectivityReceiver? = null

    var appDisposable: AppDisposable = AppDisposable()

    companion object {
        @Volatile
        private var application: MainApplication? = null

        @Synchronized
        fun getInstance(): MainApplication {
            return application!!
        }
    }

    override fun onCreate() {
        super.onCreate()
        application = this@MainApplication
        FirebaseRemoteConfigManager.fetchRemoteConfig()
        val intentFilter = IntentFilter()
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        connectivityReceiver = ConnectivityReceiver(this)
        registerReceiver(connectivityReceiver, intentFilter)
    }

    @Synchronized
    fun getAPIService(): IAPIService {
        if (mIAPIService == null) {
            mIAPIService = APIService.build()
        }
        return mIAPIService!!
    }

    @Synchronized
    fun getAPIService(cacheEnabled: Boolean): IAPIService {
        if (mIAPIService == null) {
            mIAPIService = APIService.build()
        }
        if (mIAPIServiceCache == null) {
            val cacheDuration = 3600.toLong()
            mIAPIServiceCache = APIService.build(this, cacheDuration)

        }
        return if (cacheEnabled) mIAPIServiceCache!! else mIAPIService!!
    }

    @Synchronized
    fun getKnockNockDatabase(): KnockNockDatabase? {
        if (mKnockNockDatabase == null) {
            mKnockNockDatabase = KnockNockDatabase.getInstance(this)
        }
        return mKnockNockDatabase
    }

}