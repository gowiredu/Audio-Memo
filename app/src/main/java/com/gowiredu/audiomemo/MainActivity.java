package com.gowiredu.audiomemo;

import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private ArrayList<String> lines = new ArrayList<>(); // temporarily store names of files in directory
    private ListView ResultsListView;
    private long tempFileName; // temporary file name
    private String tempString; // temporary string to store name of touched file (to look up for playback or deletion).

    //private Button start, stop, format;
    private MediaRecorder recorder = null;
    final private int opformats[] = {MediaRecorder.OutputFormat.MPEG_4, MediaRecorder.OutputFormat.THREE_GPP};

    final private String fileExtension[] = {".mp3", ".3gpp"};
    private int curFormat = 0;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY); // for ActionBar disappear on scroll
        setContentView(R.layout.activity_main);

        ResultsListView = (ListView) findViewById(R.id.ResultsListView);
        ImageButton roundButton = (ImageButton) findViewById(R.id.fab_button);

        /*
        start = (Button) findViewById(R.id.startbtn);
        stop = (Button) findViewById(R.id.stopbtn);
        format = (Button) findViewById(R.id.formatbtn);

        stop.setEnabled(false);
        format.setEnabled(true);
        */
        listDirectoryFilesStart();



        /*
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording();

                start.setEnabled(false);
                stop.setEnabled(true);
                format.setEnabled(false);

            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();

                start.setEnabled(true);
                stop.setEnabled(false);
                format.setEnabled(true);
                listDirectoryFiles();

            }
        });

        /*
        format.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                formatDialogBox();
            }
        });
        */




        roundButton.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                try {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {

                        vb.vibrate(50); // vibrate when record button is touched
                        startRecording();

                    }

                    else if (event.getAction() == MotionEvent.ACTION_UP)
                    {
                        stopRecording();
                        Log.i("Released", "Stopped");
                        vb.vibrate(50); // vibrate after recording has stopped


                    }

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Please press and hold to record", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });





        ResultsListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String touchedRecording = String.valueOf(parent.getItemAtPosition(position));
                        //Toast.makeText(MainActivity.this, "Playing", Toast.LENGTH_SHORT).show();
                        tempString = touchedRecording;

                        Toast.makeText(getApplicationContext(), "Touched: " + touchedRecording, Toast.LENGTH_SHORT).show();
                        Toast.makeText(getApplicationContext(), "TempStr: " + tempString, Toast.LENGTH_SHORT).show();
                        playAudioFile();

                        // nameOfRecording = touchedRecording; // change String "nameOfRecording" to the touched recording in the ListView
                        // playRecording(); // plays the selected recording
                    }
                }
        );





        ResultsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final String textToDelete = String.valueOf(parent.getItemAtPosition(position));
                Toast.makeText(MainActivity.this, textToDelete, Toast.LENGTH_LONG).show();

                tempString = textToDelete; // make tempString (global variable) the name of the longpressed recording
                deleteAudioDialog(); // shows
                return true;
            }
        });

        /*
        ResultsListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            int mLastFirstVisibleItem = 0;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {   }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (view.getId() == ResultsListView.getId()) {
                    final int currentFirstVisibleItem = ResultsListView.getFirstVisiblePosition();

                    if (currentFirstVisibleItem > mLastFirstVisibleItem) {
                        // getSherlockActivity().getSupportActionBar().hide();
                        getSupportActionBar().hide();
                    } else if (currentFirstVisibleItem < mLastFirstVisibleItem) {
                        // getSherlockActivity().getSupportActionBar().show();
                        getSupportActionBar().show();
                    }

                    mLastFirstVisibleItem = currentFirstVisibleItem;
                }
            }
        });
        */
    }





    // starts the recording
    private void startRecording()
    {
        Toast.makeText(this, "Recording...", Toast.LENGTH_SHORT).show();
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(opformats[curFormat]);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(getFilePath());

        try {
            recorder.prepare();
            recorder.start();
            Log.i("RECORDING", "Recording started");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }





    // stops the recording and frees the MediaRecorder
    private void stopRecording()
    {
        if (recorder != null)
        {
            Toast.makeText(this, "Memo recorded successfully", Toast.LENGTH_SHORT).show();
            recorder.stop();
            recorder.reset();
            recorder.release();

            recorder = null;

            Log.i("MICROPHONE", "Recording stopped");
            listDirectoryFiles();

        }

        /*
        if (m != null)
        {
            m.stop();
            m.reset();
            m.release();
            m = null;
            Log.i("MEDIAPLAYER", "Audio stopped");
        }*/

    }






    // for playing the selected audio file in the ListView
    private void playAudioFile()
    {
        // Runnable for MediaPlayer
        Runnable r = new Runnable() {
            @Override
            public void run() {
                MediaPlayer m = new MediaPlayer();

                try {
                    m.setDataSource(Environment.getExternalStorageDirectory().getPath() + "/MediaRecorderSample" + "/" + tempString);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    m.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                m.start();
                Log.i("PLAY_AUDIO", "Playing Audio");

                // check if MediaPlayer (audio) is done.
                m.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer m) {
                        m.stop();
                        m.release();
                    }
                });
            }
        };
        Thread playerThread = new Thread(r);
        playerThread.run();
    }






    // get the file path
    private String getFilePath()
    {
        tempFileName = System.currentTimeMillis();
        String filePath = Environment.getExternalStorageDirectory().getPath();
        File folder = new File(filePath, "MediaRecorderSample"); // create folder for files

        // if "MediaRecorderSample" folder does not exist, create one...
        if (!folder.exists())
        {
            folder.mkdirs();
        }

        //...and add the audio file.
        return (folder.getAbsolutePath() + "/" + tempFileName + fileExtension[curFormat]);
    }







    // executed at the start of the app. Lists audio files in the app's folder.
    private void listDirectoryFilesStart()
    {
        String filePath = Environment.getExternalStorageDirectory().getPath() + "/MediaRecorderSample";
        Log.d("Files", "Path: " + filePath);

        File f = new File(filePath);
        File file[] = f.listFiles();
        Log.d("Files", "Size: "+ file.length); // current number of files in the folder

        for (int i = 0; i < file.length; i++)
        {
            Log.d("Files", "FileName:" + file[i].getName());
            lines.add(file[i].getName());
        }
        Log.i("ARRAYLIST", lines.toString());

        // populate the ListView now
        try {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lines);
            ResultsListView.setAdapter(adapter);

            // refresh the listView
            refreshListView();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }







    // Updates the ArrayList and refreshes the ListView to reflect the changes.
    private void listDirectoryFiles()
    {
        // convert a "Long" to a "String"
        long number = tempFileName;
        String numberAsString = Long.toString(number);
        lines.add(numberAsString + fileExtension[curFormat]);

        // refresh the ListView
        refreshListView();
    }





    // refreshes the ListView
    private void refreshListView()
    {
        // refresh the listView
        ResultsListView.invalidateViews();
        ResultsListView.refreshDrawableState();
    }






    /*
    // for choosing a recording format (MP3 or 3GPP)
    private void formatDialogBox()
    {
        Log.i("FORMAT", "Format called");
        AlertDialog.Builder build = new AlertDialog.Builder(this);
        String formats[] = {"MP3", "3GPP"};

        build.setTitle("Choose a format");
        build.setSingleChoiceItems(formats, curFormat, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                curFormat = which;
                dialog.dismiss();
            }
        });
        build.show();
    }
    */






    // dialog box asking "Would you like to delete this memo?"
    private void deleteAudioDialog()
    {
        //AlertDialog.Builder build = new AlertDialog.Builder(this);

        android.app.AlertDialog.Builder sendReportBox = new android.app.AlertDialog.Builder(MainActivity.this);
        sendReportBox.setTitle("Delete Memo");
        sendReportBox.setMessage("Would you like to delete this memo?");
        sendReportBox.setCancelable(true);

        sendReportBox.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(MainActivity.this, "Delete selected", Toast.LENGTH_LONG).show();
                        deleteAudioFile();
                        dialog.cancel();
                    }
                });

        sendReportBox.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(MainActivity.this, "Cancel selected", Toast.LENGTH_LONG).show();
                        dialog.cancel();
                    }
                });

        android.app.AlertDialog dialogReportBox = sendReportBox.create();
        dialogReportBox.show();
    }

    private void deleteAudioFile()
    {
        Runnable r = new Runnable() {
            @Override
            public void run() {

                String filePath = Environment.getExternalStorageDirectory().getPath() + "/MediaRecorderSample";
                Log.d("Files", "Path: " + filePath);

                File f = new File(filePath);
                File file[] = f.listFiles();
                Log.d("Files", "Size: " + file.length); // current number of files in the folder


                for (int i = 0; i < file.length; i++) {
                    if (file[i].getName().equals(tempString)) {
                        boolean deleted = file[i].delete();
                        lines.remove(i);
                        refreshListView();
                    }
                }
            }
        };
        Thread deleteAudioFileThread = new Thread(r);
        deleteAudioFileThread.run();
    }






    public void onPause()
    {
        super.onPause();
        Log.i("PAUSE", "onPause() called");
        stopRecording();
    }






    public void onStop()
    {
        super.onStop();
        Log.i("STOP", "onStop() called");
        stopRecording();
    }
}
