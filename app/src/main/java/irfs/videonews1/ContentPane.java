package irfs.videonews1;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.SpannableStringBuilder;
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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.MediaController;
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
    boolean m_isVisible = false;
    int currentCaptionIndex = -1;
    boolean seeking = false;
    boolean timeSlip = false; // make captions not align to video, when seeking jumps to wrong place

    private Handler videoTimerHandler = new Handler();

    ViewGroup rootView;
    Uri videoSource;

    Chapter chapter;
    Button nextCaptionButton, prevCaptionButton;
    LinearLayout pausedLayout;
    Button pauseButton;
    Button resumeButton, replayButton;
    TextView captionView;
    LinearLayout exploreDeeperLayout;
    TextView exploreDeeperHeader;
    List<Button> exploreButtons = new ArrayList<Button>();
    int chapID = 0;
    VideoProgressBar videoProgressBar;

    MainActivity parentActivity;


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
        exploreDeeperHeader = (TextView)rootView.findViewById(R.id.exploreDeeperHeader);

        // play/resume buttons
        pausedLayout = (LinearLayout) rootView.findViewById(R.id.pausedLayout);
        resumeButton = (Button) rootView.findViewById(R.id.resumeButton);
        resumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("is","resuming");
                play();
            }
        });

        replayButton = (Button) rootView.findViewById(R.id.replayButton);
        replayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("is","replaying");
                play();
                mp.pause();
                mp.seekTo(0);
                play();
            }
        });


        chapter = (Chapter) args.getSerializable("chapter");
        //captionView.setText(chapter.captions.get(0).body);
        setCaptionText(chapter.captions.get(0).body);

        // Create Explore Deeper links
        for (int i = 0; i<chapter.links.size();i++) {



            int chap = chapter.links.get(i);
            String storyTitle =   parentActivity.getStory().chapters.get(chap).title;
            addExploreDeeperButton(storyTitle,chap);


            // only show explore deeper if buttons are present

            //if (exploreDeeperHeader != null) {
            //    exploreDeeperHeader.setVisibility(View.VISIBLE);
            //}
        }


        //mSurfaceView = (SurfaceView)rootView.findViewById(R.id.videoSurfaceView);
        mVideoView = (TextureView) rootView.findViewById(R.id.videoView);

        mVideoView.setSurfaceTextureListener(this);
        //holder = mVideoView.getHolder();
        //holder.addCallback(this);

        videoSource = Uri.parse(args.getString("uriString"));

        mp = new MediaPlayer();
        mp.setVolume(0, 0);
        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (m_isVisible) {
                    play();
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
                pauseButton.setVisibility(View.INVISIBLE);
                if (exploreDeeperLayout.getChildCount()>0) {
                    exploreDeeperLayout.setVisibility(View.VISIBLE);
                    exploreDeeperHeader.setVisibility(View.VISIBLE);
        }
                parentActivity.notifyPaused();
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
        if (!parentActivity.portrait) {
            nextCaptionButton = (Button) rootView.findViewById(R.id.nextCaption);
            nextCaptionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   if (currentCaptionIndex < chapter.captions.size()-1) {
                       // go to next caption

                       goToCaption(currentCaptionIndex + 1);
                   }
                }
            });
            prevCaptionButton = (Button) rootView.findViewById(R.id.prevCaption);
            prevCaptionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentCaptionIndex > 0) {
                        // go to previous caption
                        goToCaption(currentCaptionIndex - 1);
                    }
                }
            });
        }

        //set up video progress bar
        if (!parentActivity.portrait) {
            videoProgressBar = (VideoProgressBar) rootView.findViewById(R.id.videoProgressBar);
            int[] positions = new int[chapter.captions.size()];
            for (int i =0;i<positions.length;i++) positions[i] = chapter.captions.get(i).start;
            videoProgressBar.setCaptionPositions(positions);
        }


        return rootView;
    }


    private void addExploreDeeperButton(String storyTitle, int chap) {
        Button b = new Button(getActivity(),null,R.drawable.explore_deeper_button_drawable);
            //Style the button. This all gets a bit nasty...
            b.setText(storyTitle);
            b.setTag(chap);
            b.setAllCaps(false);
            int p = 25;
            b.setPadding(p,p,p,p);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            int m = 20;
            layoutParams.setMargins(m,m,m,m);
            //b.setLayoutParams(layoutParams);
            boolean isPresent = parentActivity.getTimelineModel().contains(chap);
            b.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            b.setEnabled(!isPresent);
            if (isPresent) {
                b.setTextColor(Color.rgb(200, 200, 200));
                b.setBackgroundResource(R.drawable.explore_deeper_inactive_button_drawable);
            } else {
                b.setTextColor(Color.WHITE);
                b.setBackgroundResource(R.drawable.explore_deeper_button_drawable);
            }

            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int chapterToLaunch = (int) v.getTag();
                    Button b = (Button) v;
                    b.setTextColor(Color.rgb(200,200,200));
                    b.setBackgroundResource(R.drawable.explore_deeper_inactive_button_drawable);
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

        if (mp!=null) {
            //currentCaptionIndex = index;

            mp.seekTo(captionTime);
            seeking = true;
            timeSlip=true;
            currentCaptionIndex = index;
            calculatePercent();
            updateCaptionButtons();
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
                play();
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

        exploreDeeperLayout.setVisibility(View.INVISIBLE);
        exploreDeeperHeader.setVisibility(View.INVISIBLE);

        mp.start();
        videoTimerHandler.postDelayed(UpdateCaptions, 100);
        parentActivity.notifyPlaying();

        //if (!parentActivity.portrait) {
        //    exploreDeeperLayout.setVisibility(View.INVISIBLE);
        //}

    }

    public void pause() {

        pausedLayout.setVisibility(View.VISIBLE);
        resumeButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.INVISIBLE);
        if (exploreDeeperLayout.getChildCount()>0) {
            exploreDeeperLayout.setVisibility(View.VISIBLE);
            exploreDeeperHeader.setVisibility(View.VISIBLE);
        }
        /*
        playButton.setText("►");
        playButton.setTag("play");
        playButton.setEnabled(true);
        playButton.setAlpha(1);
        */
        mp.pause();

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
                            setCaptionText(chapter.captions.get(i).body);
                            updateCaptionButtons();
                        }
                        break;
                    }
                }
            }

            if (mp.isPlaying()) {
                videoTimerHandler.postDelayed(this, 100);
            }
        }
    };

    private void calculatePercent() {
        float percent = 100.0f * (float) mp.getCurrentPosition()/ (float )mp.getDuration();
        mUpdatePercentListener.updatePercent(chapID, percent);

        if (!parentActivity.portrait) {
            videoProgressBar = (VideoProgressBar) rootView.findViewById(R.id.videoProgressBar);
            videoProgressBar.setDuration(mp.getDuration());
            videoProgressBar.setPosition(mp.getCurrentPosition());

        }
    }

    private void updateCaptionButtons() {


        if (!parentActivity.portrait) {
            if (currentCaptionIndex > 0) {
                prevCaptionButton.setText("<");
            } else {
                prevCaptionButton.setText("");
            }

            if (currentCaptionIndex < chapter.captions.size() - 1) {
                nextCaptionButton.setText(">");
            } else {
                nextCaptionButton.setText("");
            }

        }
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
        ClickableSpan clickable = new ClickableSpan() {
            @Override
            public void onClick(View v) {
                // Do something
                pause();
                parentActivity.showOverlay(span.getURL());
            }
        };

        stringBuilder.setSpan(clickable,start,end,flags);
        stringBuilder.removeSpan(span);


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isPlaying",mp.isPlaying());
        outState.putInt("position",mp.getCurrentPosition());

    }
    @Override
    public void onDestroyView() {

        videoTimerHandler.removeCallbacksAndMessages(null);
        mp.release();
        super.onDestroyView();
    }
}
