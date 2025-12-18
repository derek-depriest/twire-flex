package com.indev.twireflex.tasks

import com.netflix.hystrix.exception.HystrixRuntimeException
import com.indev.twireflex.activities.main.LazyFetchingActivity
import com.indev.twireflex.utils.Execute
import timber.log.Timber
import java.util.concurrent.Callable

class GetVisualElementsTask<T>(private val mLazyActivity: LazyFetchingActivity<T>) :
    Callable<MutableList<T>> {
    override fun call(): MutableList<T> {
        val resultList: MutableList<T> = ArrayList()

        try {
            resultList.addAll(mLazyActivity.visualElements)
        } catch (e: HystrixRuntimeException) {
            if (e.cause is InterruptedException) return resultList
            else Timber.e(e)
        } catch (e: Exception) {
            Timber.e(e)
        }

        Execute.ui {
            if (resultList.isEmpty()) {
                Timber.i("ADDING 0 VISUAL ELEMENTS")
                mLazyActivity.notifyUserNoElementsAdded()
            }
            mLazyActivity.addToAdapter(resultList)
            mLazyActivity.stopProgress()
            mLazyActivity.stopRefreshing()
        }

        return resultList
    }
}
