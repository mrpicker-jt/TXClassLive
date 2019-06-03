package com.tencent.ticsdk.core.impl.observer;

import com.tencent.imsdk.TIMUserStatusListener;
import com.tencent.ticsdk.core.TICManager;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;

public class TICIMStatusObservable extends TICObservable<TICManager.TICIMStatusListener>  implements TIMUserStatusListener {

    @Override
    public void onForceOffline() {
        LinkedList<WeakReference<TICManager.TICIMStatusListener>> tmpList = new LinkedList<>(listObservers);
        Iterator<WeakReference<TICManager.TICIMStatusListener>> it = tmpList.iterator();

        while(it.hasNext())
        {
            TICManager.TICIMStatusListener t = it.next().get();
            if (t != null) {
                t.onTICForceOffline();
            }
        }
    }

    @Override
    public void onUserSigExpired() {

        LinkedList<WeakReference<TICManager.TICIMStatusListener>> tmpList = new LinkedList<>(listObservers);
        Iterator<WeakReference<TICManager.TICIMStatusListener>> it = tmpList.iterator();

        while(it.hasNext())
        {
            TICManager.TICIMStatusListener t = it.next().get();
            if (t != null) {
                t.onTICUserSigExpired();
            }
        }
    }
}
