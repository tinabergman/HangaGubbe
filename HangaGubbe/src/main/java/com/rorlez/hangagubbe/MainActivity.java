package com.rorlez.hangagubbe;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

public class MainActivity extends Activity {
    public String current_word = "";
    public TextView txv_word;
    public TextView txv_wins;
    public TextView txv_losses;
    //public TextView txv_lives;
    public TextView txv_game_done;
    public int livesLeft = 8;
    //public GridLayout btnGrid;
    public static final String PREFS_NAME = "MyPreferences";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Laddar in rätt layout när den startar
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity);
        wordRandomizer();
        // Läser in txv_word och sätter texten i txv_word som streck för current_word
        // btnGrid = (GridLayout) findViewById(R.id.buttonGrid);


    }

    public void wordRandomizer() {
        //String word="";
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://vps.silenz.se/words");
                    URLConnection con = url.openConnection();
                    InputStream in = con.getInputStream();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = in.read(buf)) != -1) {
                        baos.write(buf, 0, len);
                    }
                    String body = new String(baos.toByteArray(), "UTF-8");
                    Random ran = new Random();
                    int x = ran.nextInt(body.length() - body.replace("&", "").length()) + 1;
                    System.out.println(x + "momnkey" + (body.length() - body.replace("&", "").length()));

                    final String abc = body.split("&")[x].split("&")[0];
                    current_word = abc;
                    runOnUiThread(new Runnable() {
                        public void run() {

                            txv_word = (TextView) findViewById(R.id.txv_word);
                            //txv_lives = (TextView) findViewById(R.id.txv_lives);
                            txv_word.setText(lineCounter(abc));
                        }
                    });

                } catch (Exception e) {
                    System.out.println("fail");
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    public void startGame(View view) {
        //Nollställer livräknaren
        livesLeft = 8;

        //Byter till själva spelets layout, ifall den skulle vara fel (andra omgången)
        setContentView(R.layout.game_activity);
        wordRandomizer();
        //btnGrid = (GridLayout) findViewById(R.id.buttonGrid);
        //Sätter streck för ordet till txv_word
        txv_word = (TextView) findViewById(R.id.txv_word);
        //txv_lives = (TextView) findViewById(R.id.txv_lives);
        txv_word.setText(lineCounter(current_word));
    }

    public void disableSomething(View view) {
        //Läser in knappen som tryckts och deaktiverar den
        Button b = (Button) view;
        b.setEnabled(false);
        //Läser in data med vinster och förluster
        final SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        //Kollar vilken knapp som tryckts in
        String buttonText = b.getText().toString().toLowerCase();
        //Förbereder för att leta plats på bokstaven
        int location = -1;
        boolean endofword = false;
        String word = txv_word.getText().toString();
        char[] wordArray = word.toCharArray();
        while (endofword == false) {
            if (current_word.contains(buttonText)) {
                location = current_word.indexOf(buttonText, location + 1);
                System.out.println(location);
                //System.out.println(txv_word.getText().charAt(location*2));
                //txv_word.getText().toString().

                if (location >= 0) {
                    //System.out.println(changeCharInPosition((location*2), 'a',word ));
                    wordArray[location * 2] = buttonText.toCharArray()[0];
                }

            } else {
                livesLeft -= 1;
                ImageView image = (ImageView) findViewById(R.id.img_manView);
                //txv_lives.setText(Integer.toString(livesLeft));
                Resources r = getResources();
                int imagefile = r.getIdentifier("horse_" + Integer.toString(livesLeft), "drawable", "com.rorlez.hangagubbe");
                //int imagefile = r.getResources().getIdentifier("testimage", "drawable", "your.package.name");
                image.setImageResource(imagefile);
                if (livesLeft == 0) {
                    //txv_lives.setText("Game Over");
                    int losses = settings.getInt("losses", 0);
                    int wins = settings.getInt("wins", 0);
                    editor.putInt("losses", (losses += 1));
                    editor.commit();

                    setContentView(R.layout.game_done);

                    ImageView img_manViewLost = (ImageView) findViewById(R.id.img_manViewLost);
                    img_manViewLost.setImageResource(R.drawable.horse_lasagna);

                    txv_wins = (TextView) findViewById(R.id.txv_wins);
                    txv_wins.setText("Vinster: " + Integer.toString(wins));
                    txv_losses = (TextView) findViewById(R.id.txv_losses);
                    txv_losses.setText(Html.fromHtml(getString(R.string.losses)) + Integer.toString(losses));
                    txv_game_done = (TextView) findViewById(R.id.txv_game_done);
                    txv_game_done.setText(Html.fromHtml(getString(R.string.wrongword)) + current_word);
                }
            }
            if (location < 0) {
                endofword = true;
            }
            txv_word.setText(new String(wordArray));

            editor.commit();
        }
        word = txv_word.getText().toString();
        if (word.contains("_ ")) {

        } else {
            int wins = settings.getInt("wins", 0);
            editor.putInt("wins", (wins += 1));
            int losses = settings.getInt("losses", 0);
            editor.commit();
            setContentView(R.layout.game_done);
            txv_wins = (TextView) findViewById(R.id.txv_wins);
            txv_wins.setText("Vinster: " + Integer.toString(wins));
            txv_losses = (TextView) findViewById(R.id.txv_losses);
            txv_losses.setText(Html.fromHtml(getString(R.string.losses)) + Integer.toString(losses));
            txv_game_done = (TextView) findViewById(R.id.txv_game_done);
            txv_game_done.setText("Du hittade ordet");

            ImageView img_manViewLost = (ImageView) findViewById(R.id.img_manViewLost);
            img_manViewLost.setImageResource(R.drawable.horse_saved);

        }
    }

    public String lineCounter(String word) {
        String lines = "";
        for (int i = 1; i <= word.length(); i++) {
            lines = lines + "_ ";
            //System.out.println(i);
        }
        return lines;
    }

}