package irfs.videonews1;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.Fragment;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
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
import android.widget.TextView;
import android.widget.VideoView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class MainActivity extends FragmentActivity implements ContentPane.UpdatePercentListener {

    private VideoView mainVideo;
    private MediaController mediaControls;



    private ArrayList<Integer> timelineModel = new ArrayList<Integer>(); // Integer list of chapters currently in timeline
    private ArrayList<TimelineElement> timelineElements = new ArrayList<TimelineElement>(); // List of the timeline "button" elements at top of page

    // pager widget
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    int lastPosition = 0;

    private LinearLayout timelineLayout;
    private HorizontalScrollView timelineScrollView;


    public Story getStory() {
        return story;
    }
    public ArrayList<Integer> getTimelineModel() {
        return timelineModel;
    }


    private Story story;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("MainActivity","Loading Story...");
        StoryLoader storyLoader = new StoryLoader(this);
        story = storyLoader.loadStoryFromLocalJSON();
        Log.d("MainActivity","Survived...");


        timelineLayout = (LinearLayout) findViewById(R.id.timelineLayout);
        timelineScrollView = (HorizontalScrollView) findViewById(R.id.timelineScrollView);

        //populate timeline
        for (int k = 0; k < story.initialChapters.size(); k++) {
            addTimelineElement(story.initialChapters.get(k),-1,false);
        }
        timelineElements.get(0).setActive(true);


        mPager = (ViewPager) findViewById(R.id.content_pager);
        mPagerAdapter = new ContentPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setOnPageChangeListener(pageChangeListener);



    }

    ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            //Log.d("scroller","page scrolled pos "+position+",offset "+positionOffset + ", px "+positionOffsetPixels);
            // check we moved to a different page
            if (lastPosition != position && positionOffset==0.0) {

                timelineElements.get(lastPosition).setActive(false);
                timelineElements.get(lastPosition).setPercent(0);
                lastPosition = position;

                timelineElements.get(position).setActive(true);
                float scrollLoc = timelineElements.get(position).getX() - 50;
                timelineScrollView.smoothScrollTo((int) scrollLoc,0);
            }

        }

        @Override
        public void onPageSelected(int position) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    public void addTimelineElement(int chapterID, int afterChapterID, boolean animate) {
        int position = timelineModel.indexOf(afterChapterID);
        position ++;
        if (position <= 0) position = timelineModel.size();
        Log.d("addTimelineElement","chapterID "+chapterID+" after "+afterChapterID);
        if (animate) {
            //Animation anim = AnimationUtils.loadAnimation(this,R.anim.added_timeline);
            TextView addedView = (TextView) findViewById(R.id.addedTimelineIndicator);
            //ObjectAnimator anim = ObjectAnimator.ofFloat(addedView,"layout_marginRight",-100f,0f);
            ObjectAnimator animIn = ObjectAnimator.ofFloat(addedView,"alpha",0f,1f);
            animIn.setDuration(500);
            ObjectAnimator animOut = ObjectAnimator.ofFloat(addedView,"alpha",1f,0f);
            animOut.setDuration(500);
            //animOut.start();
            AnimatorSet animSet = new AnimatorSet();
            animSet.play(animIn).before(animOut);
            animSet.play(animOut).after(1000);
            animSet.start();
        }

        TimelineElement newEl = new TimelineElement(this,chapterID);
        newEl.setTag(timelineModel.size());
        newEl.setText(story.chapters.get(chapterID).title);
        newEl.setPercent(0);
        newEl.setActive(false);
        newEl.setClickable(true);


        newEl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int page = (int) v.getTag();
                Log.d("buttonClick2","button2  clicked " + page);
                goToPage(page);
            }
        });
        timelineLayout.addView(newEl,position);

        // update data model and list of elements
        timelineModel.add(position,chapterID);
        if (mPagerAdapter != null) {
            mPagerAdapter.notifyDataSetChanged();
        }
        timelineElements.add(position,newEl);
        Log.d("MainActivity","timelineModel is now "+timelineModel);


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
        //int oldEl = mPager.getCurrentItem();
        //timelineElements.get(oldEl).setPercent(0);
        //timelineElements.get(oldEl).setActive(false);

        mPager.setCurrentItem(i);

        //timelineElements.get(i).setActive(true);

        //float scrollLoc = timelineElements.get(i).getX() - 50;
        //timelineScrollView.smoothScrollTo((int) scrollLoc,0);



    }

    @Override
    public void updatePercent(float percent) {
        int i = mPager.getCurrentItem();

        timelineElements.get(i).setPercent(percent);
    }

    private class ContentPagerAdapter extends FragmentStatePagerAdapter {
        public ContentPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {

            int chapID = timelineModel.get(position);
            //Fragment fragment = new ContentPane();
            //Bundle args = new Bundle();
            String uri = "";
            if (chapID ==0) {
                uri = "android.resource://" + getPackageName() + "/" + R.raw.india1;
            } else if (chapID ==1) {
                uri = "android.resource://" + getPackageName() + "/" + R.raw.india2;
            } else if (chapID ==2) {
                uri = "android.resource://" + getPackageName() + "/" + R.raw.india3;
            } else if (chapID ==3) {
                uri = "android.resource://" + getPackageName() + "/" + R.raw.india4;
            } else if (chapID ==4) {
                uri = "android.resource://" + getPackageName() + "/" + R.raw.india5;
            } else if (chapID == 5) {
                uri = "android.resource://" + getPackageName() + "/" + R.raw.india6;
            } else {
                uri = "android.resource://" + getPackageName() + "/" + R.raw.india7;

            }
            /*
            args.putString("uri", uri);
            args.putInt("chapID",chapID);
            args.putSerializable("chapter",story.chapters.get(chapID));
            fragment.setArguments(args);
            fragment.setUserVisibleHint(true);
            */

            ContentPane contentPane = ContentPane.newInstance(position, uri, chapID, story.chapters.get(chapID));

            return contentPane;
        }
        @Override
        public int getCount() {

            return timelineModel.size();
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
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
