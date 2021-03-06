package irfs.videonews1;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tomh on 18/03/15.
 */
public class ContentPane extends Fragment implements TextureView.SurfaceTextureListener {


    //private VideoView mainVideo;
    //private MediaController mediaControls;


    // media player
    private MediaPlayer mp;
    private SurfaceView mSurfaceView;
    private TextureView mVideoView;
    private SurfaceHolder holder;
    public boolean pausedForOverlay = false;
    boolean m_isVisible = false;
    int currentCaptionIndex = -1;
    boolean seeking = false;
    boolean timeSlip = false; // make captions not align to video, when seeking jumps to wrong place

    int playFrom = 0;

    private Handler videoTimerHandler = new Handler();

    ViewGroup rootView;
    Uri videoSource;

    Chapter chapter;
    TextView skipView;
    Button nextCaptionButton, prevCaptionButton;
    ImageView nextCaptionImage, prevCaptionImage;
    RelativeLayout nextCaptionLayout, prevCaptionLayout;
    LinearLayout pausedLayout;
    Button pauseButton;
    Button resumeButton, replayButton;
    TextView captionView;
    LinearLayout exploreDeeperLayout;
    //TextView exploreDeeperHeader;
    List<Button> exploreButtons = new ArrayList<Button>();
    int chapID = 0;
    VideoProgressBar videoProgressBar;

    MainActivity parentActivity;

    private static int updateInterval = 60;
    private String skipTextDivider = " of ";
    boolean autoPlay = true;
    boolean saveAutoPlay = true;


    public int getChapID() {
        return chapID;
    }

    public interface UpdatePercentListener {
        public void updatePercent(int chapID, float percent);
    }
    UpdatePercentListener mUpdatePercentListener;


    public static ContentPane newInstance(int _position, String _uriString,  int _chapID, Chapter _chapter) {
        ContentPane contentPane = new ContentPane();
        Bundle args = new Bundle();
        args.putInt("position",_position);
        args.putInt("chapID",_chapID);

        args.putString("uriString", _uriString);
        args.putSerializable("chapter", _chapter);
        contentPane.setArguments(args);
        return contentPane;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mUpdatePercentListener = (UpdatePercentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement UpdatePercentListener");
        }
    }
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        chapID = args.getInt("chapID");


    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Bundle args = getArguments();


        rootView = (ViewGroup) inflater.inflate(R.layout.content_pane,container,false);
        parentActivity = (MainActivity) getActivity();

        captionView = (TextView) rootView.findViewById(R.id.captionView);
        captionView.setMovementMethod(LinkMovementMethod.getInstance()); //make links clickable
        exploreDeeperLayout = (LinearLayout) rootView.findViewById(R.id.exploreDeeperLayout);
        //exploreDeeperHeader = (TextView)rootView.findViewById(R.id.exploreDeeperHeader);

        // play/resume buttons
        pausedLayout = (LinearLayout) rootView.findViewById(R.id.pausedLayout);
        resumeButton = (Button) rootView.findViewById(R.id.resumeButton);
        resumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play();
            }
        });

        replayButton = (Button) rootView.findViewById(R.id.replayButton);
        replayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play();
                mp.pause();
                mp.seekTo(0);
                play();
            }
        });


        chapter = (Chapter) args.getSerializable("chapter");
        setCaptionText(chapter.captions.get(0).body);

        // Create Explore Deeper links
        for (int i = 0; i<chapter.links.size();i++) {
            int chap = chapter.links.get(i);
            String storyTitle =   parentActivity.getStory().chapters.get(chap).title;
            addExploreDeeperButton(storyTitle,chap);

        }


        //mSurfaceView = (SurfaceView)rootView.findViewById(R.id.videoSurfaceView);
        mVideoView = (TextureView) rootView.findViewById(R.id.videoView);

        mVideoView.setSurfaceTextureListener(this);
        //holder = mVideoView.getHolder();
        //holder.addCallback(this);

        videoSource = Uri.parse(args.getString("uriString"));

        if (savedInstanceState != null) {
            Log.d("fragment","recovered position: "+savedInstanceState.getInt("position"));
            playFrom = savedInstanceState.getInt("position");

            autoPlay = savedInstanceState.getBoolean("isPlaying");
            Log.d("fragment","recovered autoplay:" + autoPlay);
        }



        mp = new MediaPlayer();
        mp.setVolume(0, 0);
        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (m_isVisible) {
                    mp.seekTo(playFrom);

                    if (autoPlay) {
                        Log.d("res","I am going to play");
                        play();
                    } else {
                        Log.d("res","I am going to pause");
                        pause();
                    }
                    if (chapID==0) {
                        //pause(); // don't autoplay first video, but still load video
                        //replayButton.setVisibility(View.GONE);
                    }
                }

            }
        });


        mp.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {
                Log.d("MPlayer", "I just got asked to seek to " + mp.getCurrentPosition());
                seeking=false;

            }
        });

        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                pausedLayout.setVisibility(View.VISIBLE);
                resumeButton.setVisibility(View.GONE);
                replayButton.setVisibility(View.VISIBLE);
                pauseButton.setVisibility(View.INVISIBLE);
                //if (exploreDeeperLayout.getChildCount()>0) {
                //    exploreDeeperLayout.setVisibility(View.VISIBLE);
                //exploreDeeperHeader.setVisibility(View.VISIBLE);
                //}
                saveAutoPlay = false;
                parentActivity.notifyPaused();
                mUpdatePercentListener.updatePercent(chapID, 100f);

            }
        });

        try {
            mp.setDataSource(getActivity(), videoSource);
            mp.prepareAsync();
        } catch ( IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e ) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        pauseButton = (Button) rootView.findViewById(R.id.pauseButton);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("is","pause button pressed");
                if (mp.isPlaying()) {
                    pause();
                }
            }
        });

        //set up caption skip buttons
        //if (!parentActivity.portrait) {

        if (parentActivity.portrait) {
            skipView = (TextView) rootView.findViewById(R.id.skipLabel);
            skipView.setText("1" + skipTextDivider + chapter.captions.size());
        }

        //nextCaptionButton = (Button) rootView.findViewById(R.id.nextCaption);
        nextCaptionImage = (ImageView) rootView.findViewById(R.id.nextCaption);
        nextCaptionLayout = (RelativeLayout) rootView.findViewById(R.id.nextCaptionLayout);
        nextCaptionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentCaptionIndex < chapter.captions.size()-1) {
                    // go to next caption

                    goToCaption(currentCaptionIndex + 1);
                }
            }
        });
        //prevCaptionButton = (Button) rootView.findViewById(R.id.prevCaption);
        prevCaptionImage = (ImageView) rootView.findViewById(R.id.prevCaption);
        prevCaptionLayout = (RelativeLayout) rootView.findViewById(R.id.prevCaptionLayout);
        prevCaptionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentCaptionIndex > 0) {
                    // go to previous caption
                    goToCaption(currentCaptionIndex - 1);
                }
            }
        });
        //}

        //set up video progress bar
        //if (!parentActivity.portrait) {
        videoProgressBar = (VideoProgressBar) rootView.findViewById(R.id.videoProgressBar);
        int[] positions = new int[chapter.captions.size()];
        for (int i =0;i<positions.length;i++) positions[i] = chapter.captions.get(i).start;
        videoProgressBar.setCaptionPositions(positions);
        //}


        return rootView;
    }


    private void addExploreDeeperButton(String storyTitle, int chap) {
        Button b = new Button(getActivity());
        //Style the button. This all gets a bit nasty...
        b.setText(storyTitle);
        b.setTag(chap);
        b.setAllCaps(false);
        b.setTextSize(20);
        b.setTypeface(null, Typeface.NORMAL);
        //b.setBackgroundColor(Color.argb(40,0,0,0));
        //int p = 25;
        //b.setPadding(p,p,p,p);

        int defaultHeight, m;
        RelativeLayout contentPaneLayout = (RelativeLayout) rootView.findViewById(R.id.content_pane);
        Log.d("contentpane","tag is "+ contentPaneLayout.getTag());
        if (contentPaneLayout.getTag().equals("large")) {
            Log.d("contentpane","large size");
            defaultHeight= (int) (44 * getResources().getDisplayMetrics().scaledDensity); // was 48
            m = 20;
            b.setTextSize(20);
        } else {
            Log.d("contentpane","normal size");
            defaultHeight= (int) (39 * getResources().getDisplayMetrics().scaledDensity); // was 48
            m = 18;
            b.setTextSize(18);
        }

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, defaultHeight);
        layoutParams.setMargins(m,m,m,m);
        //b.setLayoutParams(layoutParams);
        //b.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        boolean isPresent = parentActivity.getTimelineModel().contains(chap);

        b.setEnabled(!isPresent);
        if (isPresent) {
            b.setTextColor(Color.rgb(40, 40, 40));
            b.setBackgroundResource(R.mipmap.btn_deeper_inactive);
        } else {
            b.setTextColor(Color.WHITE);
            b.setBackgroundResource(R.mipmap.btn_deeper_active);
        }

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int chapterToLaunch = (int) v.getTag();
                Button b = (Button) v;
                b.setTextColor(Color.rgb(40, 40, 40));
                b.setBackgroundResource(R.mipmap.btn_deeper_inactive);
                parentActivity.addTimelineElement(chapterToLaunch,chapID,true);
                v.setEnabled(false);
            }
        });
        exploreButtons.add(b);
        exploreDeeperLayout.addView(b,layoutParams);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {

        updateSurfaceTextureSize();
        Surface surface = new Surface(surfaceTexture);
        mp.setSurface(surface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return true;
    }


    private void updateSurfaceTextureSize() {
        if (parentActivity.portrait) {

            float newAspect = 1f;
            //mVideoView.getLayoutParams().height = (int) (mVideoView.getWidth() * squariness);
            int mainDimension = 1080;
            float width = mVideoView.getWidth();
            //mVideoView.getLayoutParams().width = mainDimension;
            //mVideoView.getLayoutParams().height = (int) (mainDimension/newAspect);
            //mVideoView.getLayoutParams().height = (int) (width/newAspect);
            Matrix matrix = new Matrix();
            float scaleX = (16f/9f) / newAspect;
            float scaleY = 1f;
            float pivotX = (float) width/2f;
            float pivotY = (width / newAspect) /2f;
            matrix.setScale(scaleX,scaleY,pivotX,pivotY);
            //mVideoView.setMinimumHeight((int) (mVideoView.getWidth()*squariness));
            mVideoView.setTransform(matrix);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mVideoView.getLayoutParams();
            params.height = (int) (width/newAspect);
            mVideoView.setLayoutParams(params);
            //mVideoView.setLayoutParams(new FrameLayout.LayoutParams(viewWidth,viewHeight));
        }

    }

    private void goToCaption(int index) {
        //stop stupid values
        if (index ==currentCaptionIndex || index > chapter.captions.size() - 1 || index < 0) {
            return;
        }
        Log.d("caption","going from index " + currentCaptionIndex+ " to "+ index);
        int captionTime = chapter.captions.get(index).start;


        resumeButton.setVisibility(View.VISIBLE);

        if (mp!=null) {
            //currentCaptionIndex = index;


            Log.d("seeker","seeking to "+captionTime);
            mp.seekTo(captionTime);
            seeking = true;
            timeSlip=true;
            currentCaptionIndex = index;
            calculatePercent();
            updateCaptionButtons();


            if (parentActivity.portrait) {
                skipView.setText((index + 1) + skipTextDivider + chapter.captions.size());
            }
            setCaptionText(chapter.captions.get(index).body);
            //videoTimerHandler.removeCallbacksAndMessages(null);
            //videoTimerHandler.post(UpdateCaptions);

        }

    }
    private void captionTimeout() {

    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {

        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && ! m_isVisible) {
            // it transitioned invisible -> visible


            if (mp != null) {
                /*if (autoPlay) {
                    play();
                } else {
                    pause();
                }*/
                Log.d("uservisible","became visible, autoplay " + autoPlay);
                play();
                //if(!autoPlay) pause();

            }

        } else if (!isVisibleToUser && m_isVisible) {
            // it transitioned visible -> invisible

            try {
                if (mp.isPlaying()) {

                    mp.pause();
                    mp.seekTo(0);
                }
            } catch (Exception e) {

            }
        }


        m_isVisible = isVisibleToUser;

    }

    /*
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {


    }
    @Override
    public void surfaceCreated(SurfaceHolder holder ) {
        mp.setDisplay(holder);
        //play();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
    */

    public void play() {
        if (chapter.hasAudio) {
            mp.setVolume(1, 1);
        } else {
            mp.setVolume(0, 0);
        }

        //playButton.setAlpha(0);
        //playButton.setEnabled(false);

        pausedLayout.setVisibility(View.INVISIBLE);
        pauseButton.setVisibility(View.VISIBLE);

        //exploreDeeperLayout.setVisibility(View.INVISIBLE);
        //exploreDeeperHeader.setVisibility(View.INVISIBLE);

        if (!mp.isPlaying()) {
            mp.start();
        }

        videoTimerHandler.postDelayed(UpdateCaptions, updateInterval);
        parentActivity.notifyPlaying();

        saveAutoPlay = true;

        //if (!parentActivity.portrait) {
        //    exploreDeeperLayout.setVisibility(View.INVISIBLE);
        //}

    }

    public void pause() {

        pausedLayout.setVisibility(View.VISIBLE);
        resumeButton.setVisibility(View.VISIBLE);
        replayButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.INVISIBLE);
        //if (exploreDeeperLayout.getChildCount()>0) {
        //    exploreDeeperLayout.setVisibility(View.VISIBLE);
        //exploreDeeperHeader.setVisibility(View.VISIBLE);
        //}
        /*
        playButton.setText("►");
        playButton.setTag("play");
        playButton.setEnabled(true);
        playButton.setAlpha(1);
        */
        if (mp.isPlaying()) {
            mp.pause();
        }

        saveAutoPlay = false;
        parentActivity.notifyPaused();
        //if (!parentActivity.portrait) {
        //    exploreDeeperLayout.setVisibility(View.VISIBLE);
        //}
    }

    private Runnable UpdateCaptions = new Runnable() {
        @Override
        public void run() {

            calculatePercent();

            int pos = mp.getCurrentPosition();
            //Log.d("caption","updatecaptions, pos is "+pos);

            // don't update the captions if we're still seeking and haven't got to the right point
            // in the video. (This is a workaround because the video usually seeks a little before
            // where you tell it to seek to, making the wrong caption appear)
            if (timeSlip && !seeking && pos >= chapter.captions.get(currentCaptionIndex).start ) {
                timeSlip = false;
            }

            if (!timeSlip) {

                int i;
                for (i = 0; i < chapter.captions.size(); i++) {
                    if (pos >= chapter.captions.get(i).start && pos < chapter.captions.get(i).end) {
                        if (i != currentCaptionIndex) {
                            //Log.d("caption", "I picked index " + i + ", as pos inbetween " + chapter.captions.get(i).start + " and " + chapter.captions.get(i).end);
                            currentCaptionIndex = i;


                            if (parentActivity.portrait) {
                                skipView.setText((i + 1) + skipTextDivider + chapter.captions.size());
                            }
                            setCaptionText(chapter.captions.get(i).body);
                            updateCaptionButtons();
                        }
                        break;
                    }
                }
            }

            if (mp.isPlaying()) {
                videoTimerHandler.postDelayed(this, updateInterval);
            }
        }
    };

    private void calculatePercent() {
        float percent = 100.0f * (float) mp.getCurrentPosition()/ (float )mp.getDuration();
        mUpdatePercentListener.updatePercent(chapID, percent);

        videoProgressBar = (VideoProgressBar) rootView.findViewById(R.id.videoProgressBar);
        videoProgressBar.setDuration(mp.getDuration());
        videoProgressBar.setPosition(mp.getCurrentPosition());

    }

    private void updateCaptionButtons() {


        //if (!parentActivity.portrait) {
        if (currentCaptionIndex > 0) {
            prevCaptionImage.setBackgroundResource(R.mipmap.skip_left_active);
        } else {
            prevCaptionImage.setBackgroundResource(R.mipmap.skip_left_inactive);
        }

        if (currentCaptionIndex < chapter.captions.size() - 1) {
            nextCaptionImage.setBackgroundResource(R.mipmap.skip_right_active);
        } else {
            nextCaptionImage.setBackgroundResource(R.mipmap.skip_right_inactive);
        }

        //}
    }

    // creates hyperlinks in text
    private void setCaptionText(String body) {
        CharSequence sequence = Html.fromHtml(body);
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        URLSpan[] urls = strBuilder.getSpans(0,sequence.length(), URLSpan.class);
        for (URLSpan span:urls) {
            makeLinkClickable(strBuilder,span);
        }
        captionView.setText(strBuilder);
    }

    private void makeLinkClickable(SpannableStringBuilder stringBuilder, final URLSpan span) {
        int start = stringBuilder.getSpanStart(span);
        int end = stringBuilder.getSpanEnd(span);
        int flags = stringBuilder.getSpanFlags(span);
        ClickableSpanNoUnderline clickable = new ClickableSpanNoUnderline() {
            @Override
            public void onClick(View v) {
                // Do something
                if (mp.isPlaying()) {
                    pausedForOverlay = true;
                    pause();
                }
                parentActivity.showOverlay(span.getURL());
            }
        };

        stringBuilder.setSpan(clickable,start,end,flags);
        stringBuilder.removeSpan(span);


    }

    private class ClickableSpanNoUnderline extends ClickableSpan {
        public ClickableSpanNoUnderline() {
            super();
        }
        @Override
        public void onClick(View widget) {
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
        }

    }

    @Override
    public void onPause() {
        Log.d("fragment","pausing");
        if (mp.isPlaying()) {
            pause();
            saveAutoPlay = true;
        }
        super.onPause();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d("fragment","saving with autoplay state: " + saveAutoPlay);

        outState.putBoolean("isPlaying",saveAutoPlay);
        outState.putInt("position", mp.getCurrentPosition());

        super.onSaveInstanceState(outState);

    }


    @Override
    public void onDestroyView() {

        videoTimerHandler.removeCallbacksAndMessages(null);
        mp.release();
        super.onDestroyView();
    }
}
