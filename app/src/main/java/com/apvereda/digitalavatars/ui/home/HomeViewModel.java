package com.apvereda.digitalavatars.ui.home;

import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.apvereda.digitalavatars.R;

public class HomeViewModel extends ViewModel {
    private static HomeViewModel instance;
    private final MutableLiveData<Integer> offerBadgeVisibility = new MutableLiveData<>();
    private final MutableLiveData<Integer> requestBadgeVisibility = new MutableLiveData<>();

    public static void setInstance(HomeViewModel vm) {
        instance = vm;
    }

    public static HomeViewModel getInstance() {
        return instance;
    }

    public LiveData<Integer> getOfferBadgeVisibility() {
        return offerBadgeVisibility;
    }

    public LiveData<Integer> getRequestBadgeVisibility() {
        return requestBadgeVisibility;
    }

    public void setOfferBadgeVisibility(int visibility) {
        offerBadgeVisibility.setValue(visibility);
    }

    public void setRequestBadgeVisibility(int visibility) {
        requestBadgeVisibility.setValue(visibility);
    }

}