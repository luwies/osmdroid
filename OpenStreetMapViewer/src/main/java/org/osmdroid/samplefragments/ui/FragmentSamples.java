package org.osmdroid.samplefragments.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.osmdroid.ExtraSamplesActivity;
import org.osmdroid.ISampleFactory;
import org.osmdroid.model.BaseActivity;
import org.osmdroid.samplefragments.BaseSampleFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by KjellbergZ on 16.12.2015.
 */
public class FragmentSamples extends ListFragment {

    public final static String TAG="osmfragsample";

    private ISampleFactory sampleFactory=null;
    private List<BaseActivity> additionActivitybasedSamples;

    public static FragmentSamples newInstance(ISampleFactory fac, List<BaseActivity> additionActivitybasedSamples) {
        FragmentSamples x= new FragmentSamples();
        x.sampleFactory=fac;
        x.additionActivitybasedSamples=additionActivitybasedSamples;
        return x;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //the following block of code took me an entire weekend to track down the root cause.
        //if the code block is in onCreate, it will leak. onActivityCreated = no leak.
        //makes no sense, but that's Android for you.

        // Generate a ListView with Sample Maps
        final ArrayList<String> list = new ArrayList<>();
        // Put samples next
        for (int a = 0; a < sampleFactory.count(); a++) {
            final BaseSampleFragment f = sampleFactory.getSample(a);
            list.add(f.getSampleTitle());
        }
        for (int a=0; a<additionActivitybasedSamples.size(); a++){
            list.add(additionActivitybasedSamples.get(a).getActivityTitle());
        }
        //changed from getActivity to Application context per http://www.slideshare.net/AliMuzaffar2/android-preventing-common-memory-leaks
        //prior to that, we had a memory leak that would only show on long running tests. the issue is that the array adapter
        //wasn't being gc'd.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity().getApplicationContext(),
                android.R.layout.simple_list_item_1, list);
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (position < sampleFactory.count()) {
            // Replace Fragment with selected sample
            BaseSampleFragment frag = sampleFactory.getSample(position);
            Log.i(TAG, "loading fragment " + frag.getSampleTitle() + ", " + frag.getClass().getCanonicalName());
            FragmentManager fm = getFragmentManager();
            fm.beginTransaction().replace(org.osmdroid.R.id.samples_container, frag, ExtraSamplesActivity.SAMPLES_FRAGMENT_TAG)
                    .addToBackStack(null).commit();
        } else {
            Activity activity = additionActivitybasedSamples.get(position-sampleFactory.count());
            Intent i = new Intent(getContext(), activity.getClass());
            getActivity().startActivity(i);
        }

    }

    @Override
    public void onResume(){
        super.onResume();

        FragmentManager fm = getFragmentManager();
        fm.popBackStack();
        System.gc();
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
    }
}
