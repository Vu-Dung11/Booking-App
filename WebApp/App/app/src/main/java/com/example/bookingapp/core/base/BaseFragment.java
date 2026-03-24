package com.example.bookingapp.core.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

public abstract class BaseFragment<VB extends ViewBinding> extends Fragment {

    private VB binding;

    @NonNull
    protected VB getBinding() {
        return binding;
    }

    public interface Inflate<T> {
        T inflate(LayoutInflater inflater, ViewGroup container, boolean attach);
    }

    protected abstract Inflate<VB> getInflate();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = getInflate().inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews();
        observeViewModel();
    }

    protected abstract void setupViews();
    protected abstract void observeViewModel();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
