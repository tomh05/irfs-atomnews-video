package irfs.videonews1;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.Fragment;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.VideoView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements ContentPane.UpdatePercentListener {

    private VideoView mainVideo;
    private MediaController mediaControls;
    private ArrayList<TimelineElement> timelineElements = new ArrayList<TimelineElement>();

    // pager widget
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;

    private LinearLayout timelineLayout;
    private HorizontalScrollView timelineScrollView;


    private Story story;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("MainActivity","Loading Story...");
        StoryLoader storyLoader = new StoryLoader(this);
        story = storyLoader.loadStoryFromLocalJSON();
        Log.d("MainActivity","Survived...");

        mPager = (ViewPager) findViewById(R.id.content_pager);
        mPagerAdapter = new ContentPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);


        timelineLayout = (LinearLayout) findViewById(R.id.timelineLayout);
        timelineScrollView = (HorizontalScrollView) findViewById(R.id.timelineScrollView);

        for (int i=0;i<story.chapters.size(); i++) {
            TimelineElement newEl = new TimelineElement(this,i);
            newEl.setTag(i);
            newEl.setText(story.chapters.get(i).title);
            newEl.setPercent(0);
            newEl.setActive(false);
            newEl.setClickable(true);

            newEl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int page = (int) v.getTag();
                    Log.d("buttonClick2","button2  clicked " + page);
                    goToPage(page);

                    //context.goToPage((int) v.getTag());

                }
            });
            //newButton.setText(story.chapters.get(i).title);

            timelineElements.add(newEl);
            timelineLayout.addView(newEl);
        }

        timelineElements.get(0).setActive(true);


    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            //mPager.setCurrentItem(mPager.getCurrentItem()-1,true);
            goToPage(mPager.getCurrentItem()-1);
        }
    }

    public void goToPage(int i)
    {
        int oldEl = mPager.getCurrentItem();
        timelineElements.get(oldEl).setPercent(0);
        timelineElements.get(oldEl).setActive(false);

        mPager.setCurrentItem(i);

        timelineElements.get(i).setActive(true);

        float scrollLoc = timelineElements.get(i).getX() - 50;
        timelineScrollView.smoothScrollTo((int) scrollLoc,0);



    }

    @Override
    public void updatePercent(float percent) {
        int i = mPager.getCurrentItem();

        timelineElements.get(i).setPercent(percent);
    }

    private class ContentPagerAdapter extends FragmentStatePagerAdapter {
        private static final int NUM_PAGES = 7;
        public ContentPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            //Fragment fragment = new ContentPane();
            //Bundle args = new Bundle();
            String uri = "";
            if (position ==0) {
                uri = "android.resource://" + getPackageName() + "/" + R.raw.india1;
            } else if (position ==1) {
                uri = "android.resource://" + getPackageName() + "/" + R.raw.india2;
            } else if (position ==2) {
                uri = "android.resource://" + getPackageName() + "/" + R.raw.india3;
            } else if (position ==3) {
                uri = "android.resource://" + getPackageName() + "/" + R.raw.india4;
            } else if (position ==4) {
                uri = "android.resource://" + getPackageName() + "/" + R.raw.india5;
            } else if (position == 5) {
                uri = "android.resource://" + getPackageName() + "/" + R.raw.india6;
            } else {
                uri = "android.resource://" + getPackageName() + "/" + R.raw.india7;

            }
            /*
            args.putString("uri", uri);
            args.putInt("position",position);
            args.putSerializable("chapter",story.chapters.get(position));
            fragment.setArguments(args);
            fragment.setUserVisibleHint(true);
            */

            ContentPane contentPane = ContentPane.newInstance(position, uri, story.chapters.get(position));

            return contentPane;
        }
        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
