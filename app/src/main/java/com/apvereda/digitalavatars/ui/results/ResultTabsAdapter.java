package com.apvereda.digitalavatars.ui.results;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.apvereda.digitalavatars.ui.results.created.ResultsCreatedFragment;
import com.apvereda.digitalavatars.ui.results.subscribed.ResultsSubscribedFragment;

public class ResultTabsAdapter extends FragmentStateAdapter {

    public ResultTabsAdapter(Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return switch (position) {
            case 0 -> new ResultsCreatedFragment();
            case 1 -> new ResultsSubscribedFragment();
            default -> new Fragment();
        };
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
