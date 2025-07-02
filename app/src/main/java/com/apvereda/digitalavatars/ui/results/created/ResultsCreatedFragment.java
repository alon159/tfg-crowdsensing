package com.apvereda.digitalavatars.ui.results.created;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.apvereda.db.AbstractEntity;
import com.apvereda.db.Entity;
import com.apvereda.db.Value;
import com.apvereda.digitalavatars.R;
import com.apvereda.digitalavatars.ui.results.ResultsAdapter;
import com.apvereda.utils.DigitalAvatarController;

import java.util.ArrayList;
import java.util.List;

public class ResultsCreatedFragment extends Fragment {
    View root;
    ResultsAdapter adapter;
    ListView listResults;
    List<AbstractEntity> surveys;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_results_created, container, false);

        DigitalAvatarController dac = new DigitalAvatarController();
        List<AbstractEntity> aux = dac.getAll();
        surveys = new ArrayList<>();
        for (int i = 0; i < aux.size(); i++) {
            Entity eaux = (Entity) aux.get(i);
            Value v = (Value) eaux.getValue("creator");
            if (v != null) {
                Boolean creator = (Boolean) v.get();
                if (creator) {
                    surveys.add(eaux);
                }
            }
        }
        adapter = new ResultsAdapter(getActivity(), surveys);
        listResults = root.findViewById(R.id.listCreatedResults);
        listResults.setAdapter(adapter);

        return root;
    }
}