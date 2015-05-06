package irfs.videonews1;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.Fragment;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.List;


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


    String articleName;
    public Story getStory() {
        return story;
    }
    public ArrayList<Integer> getTimelineModel() {
        return timelineModel;
    }

    public final static int HIDDEN = 0;
    public final static int VISIBLE = 1;
    public final static int WAITING = 2;
    int timelineState = VISIBLE;
    final Handler timelineAnimationHandler = new Handler();

    public boolean portrait;
    private Story story;

    String currentOverlay = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        portrait = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       Intent intent = getIntent();
       articleName = intent.getStringExtra("name");

        StoryLoader storyLoader = new StoryLoader(this);
        story = storyLoader.loadStoryFromLocalJSON(articleName);


        //if (portrait) {
        //    TextView titleTextView = (TextView) findViewById(R.id.titleTextView);
        //    titleTextView.setText(story.title);
        //`}

        timelineLayout = (LinearLayout) findViewById(R.id.timelineLayout);
        timelineScrollView = (HorizontalScrollView) findViewById(R.id.timelineScrollView);



        if (savedInstanceState != null) {
            // Resume overlay if there is one
            if (savedInstanceState.getString("currentOverlay") != "") {
                showOverlay(savedInstanceState.getString("currentOverlay"));
            }


                // Reload timeline from model if there is one
        ArrayList<Integer> savedTimelineModel =  savedInstanceState.getIntegerArrayList("timelineModel");
            for (int k = 0; k < savedTimelineModel.size(); k++) {
                addTimelineElement(savedTimelineModel.get(k), -1, false);
            }
            timelineElements.get(0).setActive(true);
            //int position = savedInstanceState.getInt("lastPosition");
            //float scrollLoc = timelineElements.get(position).getX() - 50;
            //timelineScrollView.smoothScrollTo((int) scrollLoc,0);
        } else {
            //populate timeline
            for (int k = 0; k < story.initialChapters.size(); k++) {
                addTimelineElement(story.initialChapters.get(k), -1, false);
            }
            timelineElements.get(0).setActive(true);
        }


        mPager = (ViewPager) findViewById(R.id.content_pager);
        mPagerAdapter = new ContentPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setOnPageChangeListener(pageChangeListener);


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
        outState.putIntegerArrayList("timelineModel",timelineModel);
        outState.putString("currentOverlay",currentOverlay);
        //outState.putInt("lastPosition",lastPosition);
    }

    void goHome() {
        Intent intent = new Intent(this,StoryChooserActivity.class);
        startActivity(intent);
    }

    ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            //Log.d("scroller","page scrolled pos "+position+",offset "+positionOffset + ", px "+positionOffsetPixels);
            // check we moved to a different page

            if (!portrait) {
                if (positionOffset ==0.0) {
                    //hideTimeline();
                } else {
                    if (timelineState == HIDDEN) animateTimelineDown();
                    if (timelineState == WAITING) {
                        animateTimelineDown();
                    }
                    //showTimeline();
                }
            }
            if (lastPosition != position && positionOffset==0.0) {

                //Log.d("pos","last pos was "+lastPosition+", current "+position);
                timelineElements.get(lastPosition).setActive(false);
                timelineElements.get(lastPosition).setPercent(0);
                lastPosition = position;

                timelineElements.get(position).setActive(true);
                timelineScrollView.post(new Runnable() { //Posting delays until the view has been set up.
                    @Override
                    public void run() {

                        float scrollLoc = timelineElements.get(lastPosition).getX() - 50;
                        //Log.d("autoscroll","auto scrolled to " + scrollLoc);
                        timelineScrollView.smoothScrollTo((int) scrollLoc,0);
                    }
                });
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
        //Log.d("addTimelineElement","chapterID "+chapterID+" after "+afterChapterID);

        TimelineElement newEl = new TimelineElement(this,chapterID);
        newEl.setTag(chapterID);
        newEl.setText(story.chapters.get(chapterID).title);
        newEl.setPercent(0);
        newEl.setActive(false);
        newEl.setClickable(true);
        //newEl.setPadding(0,0,4,0);
        //ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        //newEl.setLayoutParams(params);

        newEl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int chapterID = (int) v.getTag();
                //goToPage(page);
                goToChapterID(chapterID);
            }
        });

        if (animate) {
            // scroll to ensure we can see the animation
            float scrollLoc = timelineElements.get(lastPosition).getX() - 50;
            timelineScrollView.smoothScrollTo((int) scrollLoc,0);

            ObjectAnimator anim = ObjectAnimator.ofFloat(newEl,"interp",0f,1f);
            anim.setDuration(2000);
            anim.start();
            if (!portrait) {
                if (timelineState == HIDDEN) { animateTimelineDown(); }
                if (timelineState == WAITING) { animateTimelineDown(); }
            }
        } else {

        }

        timelineLayout.addView(newEl,position);

        // update data model and list of elements
        timelineModel.add(position,chapterID);
        if (mPagerAdapter != null) {
            mPagerAdapter.notifyDataSetChanged();
        }
        timelineElements.add(position,newEl);
        //Log.d("addTimelineElement","timelineModel is now "+timelineModel);


    }



    public void goToPage(int i)
    {
        mPager.setCurrentItem(i);
    }

    public void goToChapterID(int id) {
        int page = timelineModel.indexOf(id);
        if (page >=0) {
            goToPage(page);
        }
    }

    @Override
    public void updatePercent(int chapID, float percent) {
        //int i = mPager.getCurrentItem();

        int i = timelineModel.indexOf(chapID);
        if (i >= 0) {
            timelineElements.get(i).setPercent(percent);
        }

        if (!portrait) {
            if (timelineState == VISIBLE) {
                startTimelineTimeout();
            }
        }
    }

    public void showOverlay(String id) {

        LinearLayout overlayLayout = (LinearLayout) findViewById(R.id.overlayLayout);
        TextView overlayTitleView = (TextView) findViewById(R.id.overlayTitle);
        TextView overlayBodyView = (TextView) findViewById(R.id.overlayBody);
        Button overlayCloseButton = (Button) findViewById(R.id.overlayClose);
        String overlayBodyText = (String)  story.extras.get(id);

        currentOverlay = id;
        overlayTitleView.setText(id);
        overlayBodyView.setText(overlayBodyText);

        overlayCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout overlayLayout = (LinearLayout) findViewById(R.id.overlayLayout);
                overlayLayout.setVisibility(View.INVISIBLE);
                currentOverlay="";
                //resume fragment
                // problem: how do I know which fragment is currently available?
                //ContentPane c = (ContentPane) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.content_pager + ":" + mPager.getCurrentItem());

                List<Fragment> fragments = getSupportFragmentManager().getFragments();
                for (int i = 0; i < fragments.size(); i++) {
                    ContentPane pane = (ContentPane) fragments.get(i);
                    if (pane != null) {
                        if (pane.pausedForOverlay) {
                            pane.pausedForOverlay = false;
                            pane.play();
                        }
                    }
                }
            }
        });

        overlayLayout.setVisibility(View.VISIBLE);

    }

    @Override
    public void onBackPressed(){
        LinearLayout overlayLayout = (LinearLayout) findViewById(R.id.overlayLayout);
        if (overlayLayout.getVisibility() == View.VISIBLE) {
            overlayLayout.setVisibility(View.INVISIBLE);
                //resume fragment

                List<Fragment> fragments = getSupportFragmentManager().getFragments();
                for (int i = 0; i < fragments.size(); i++) {
                    ContentPane pane = (ContentPane) fragments.get(i);
                    if (pane != null) {
                        if (pane.pausedForOverlay) {
                            pane.pausedForOverlay = false;
                            pane.play();
                        }
                    }
                }

        } else if (mPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            goToPage(mPager.getCurrentItem()-1);
        }
    }


    private class ContentPagerAdapter extends FragmentStatePagerAdapter {
        public ContentPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {

            int chapID = timelineModel.get(position);

            String uri = "android.resource://" + getPackageName() + "/" + getResources().getIdentifier(articleName + chapID,"raw",getPackageName());
            ContentPane contentPane = ContentPane.newInstance(position, uri, chapID, story.chapters.get(chapID));

            return contentPane;
        }
        @Override
        public int getCount() {

            return timelineModel.size();
        }

        @Override
        public int getItemPosition(Object object) {
            // We only want items to be refreshed if needed - i.e. they're to the right of the current item
            ContentPane f = (ContentPane) object;
            int chapID = f.getChapID();

            int position = timelineModel.indexOf(chapID);
            if (position > mPager.getCurrentItem()) {
                return PagerAdapter.POSITION_NONE;
            } else {

                return PagerAdapter.POSITION_UNCHANGED;
            }
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
    private void showTimeline() {
        if (timelineState == WAITING) {
            // cancel timer
            timelineAnimationHandler.removeCallbacksAndMessages(null);
        }
        if (timelineState == HIDDEN) {
            animateTimelineDown();
        }
        if (timelineState == VISIBLE) {
            timelineAnimationHandler.removeCallbacksAndMessages(null);
        }
    }
    private void hideTimeline() {

        if (timelineState == VISIBLE) {
            // start timer
            timelineAnimationHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!portrait) {
                        animateTimelineUp();
                    }
                }
            }, 3000);
        }

    }


    void animateTimelineDown() {

        timelineAnimationHandler.removeCallbacksAndMessages(null);
        //final int start = ((ViewGroup.MarginLayoutParams) timelineScrollView.getLayoutParams()).topMargin;
        //final int end = 0; // hack - see -120 below
        final float startAlpha = timelineLayout.getAlpha();
        final float endAlpha = 1f;
            Animation a = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) timelineScrollView.getLayoutParams();
                    //params.topMargin = (int) (start * (1f-interpolatedTime) + end * interpolatedTime ) ;
                    timelineLayout.setAlpha(startAlpha*(1f-interpolatedTime)+endAlpha*interpolatedTime);
                    //timelineScrollView.setLayoutParams(params);
                }
            };
        a.setDuration(300);
        a.setAnimationListener(new Animation.AnimationListener(){
                @Override
                public void onAnimationStart(Animation animation) {
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    timelineState = VISIBLE;
                }
                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            //a.cancel();
            timelineScrollView.startAnimation(a);
    }

    void animateTimelineUp() {

        timelineAnimationHandler.removeCallbacksAndMessages(null);
        //final int start = ((ViewGroup.MarginLayoutParams) timelineScrollView.getLayoutParams()).topMargin;
        //final int end = -140; // hack - see -120 below
        final float startAlpha = timelineLayout.getAlpha();
        final float endAlpha = 0f;
            Animation a = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    //ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) timelineScrollView.getLayoutParams();
                    timelineLayout.setAlpha(startAlpha*(1f-interpolatedTime)+endAlpha*interpolatedTime);
                    //params.topMargin = (int) (start * (1f-interpolatedTime) + end * interpolatedTime ) ;

                    //timelineScrollView.setLayoutParams(params);
                }
            };
            a.setDuration(1000);
            timelineScrollView.startAnimation(a);
            a.setAnimationListener(new Animation.AnimationListener(){
                @Override
                public void onAnimationStart(Animation animation) {
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    timelineState = HIDDEN;
                }
                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
    }

    void startTimelineTimeout() {
        timelineState = WAITING;
        timelineAnimationHandler.postDelayed(new Runnable() {
            @Override
                public void run() {
                    animateTimelineUp();
            }
        }, 3000);
    }
    public void notifyPaused() {
       if (!portrait) {
           if (timelineState == HIDDEN) animateTimelineDown();
           if (timelineState == WAITING) animateTimelineDown();
           //showTimeline();

       }
    }
    public void notifyPlaying() {
        if (!portrait) {
            //hideTimeline();
            if (timelineState == VISIBLE) startTimelineTimeout();
            //if (timelineState == WAITING) animateTimelineUp();
        }
    }
}

