package com.example.battleairplanesclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class GameActivity extends Activity {
	List<Integer> myPlane;
	List<Integer> serverPlane;
	List<Integer> pushedButtons;
	private boolean myTurn = true;
	private Integer myScore = 0;
	private Integer serverScore = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
		    Boolean newGame = extras.getBoolean("newGame");
		    if (newGame == true) {
		    	extras.remove("newGame");
		    	myPlane = new ArrayList<Integer>();
		    	while (true) {
		    		Integer newPosition = randInt(1, 36);
		    		if (myPlane.contains(newPosition) == false)
		    			myPlane.add(newPosition);
		    		if (myPlane.size() == 6)
		    			break;
		    	}
		    	for (int i = 0; i < 6; i++) {
		    		Integer buttonId = myPlane.get(i);
		    		int id = getResources().getIdentifier("but".concat(buttonId.toString()), "id", getPackageName());
		    		Button but = (Button)findViewById(id);
		    		but.setBackgroundColor(Color.GREEN);
		    		but.setText("||");
		    	}
		    	TextView mScore = (TextView)findViewById(R.id.scor_juc);
		    	mScore.setText("Your Score: 0");
		    	
		    	TextView oScore = (TextView)findViewById(R.id.scor_opp);
		    	oScore.setText("Oponnent Score: 0");
		    	
		    	enableAllButtons();
		    	pushedButtons = new ArrayList<Integer>();
		    }
		}
		
		new Game().execute(null, null, null);
		
	}
	
	private class Game extends AsyncTask<Void, Void, Void> {
		private final static String TAG = "ServerThread";
		
		@Override
		protected Void doInBackground(Void... params) {
			Log.e(TAG, "Game doInBackground");
			Socket socket = MainActivity.SocketConnection.getSocket();
			serverPlane = new ArrayList<Integer>();
			try {
				PrintStream writer = new PrintStream(socket.getOutputStream());
				for (int i = 0; i < 6; i++) {
					writer.println(myPlane.get(i));
					Log.i("Game: client sending position: ", myPlane.get(i).toString());
				}
				
				BufferedReader responseReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String line;
				for (int i = 0; i < 6; i++) {
					line = responseReader.readLine();
					Log.i("server received position: ", line);
					serverPlane.add(Integer.parseInt(line));
				}
				
			} catch (IOException e) {
				Log.e("writing to socket", "error while sending/receiving plane position");
				return null;
			}
			
			return null;
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game, menu);
		return true;
	}
	
	private void disableButtons() {
		for (Integer i = 1; i < 37; i++) {
			int id = getResources().getIdentifier("but".concat(i.toString()), "id", getPackageName());
    		Button but = (Button)findViewById(id);
    		but.setEnabled(false);
		}
	}
	
	private void enableAllButtons() {
		for (Integer i = 1; i < 37; i++) {
			int id = getResources().getIdentifier("but".concat(i.toString()), "id", getPackageName());
    		Button but = (Button)findViewById(id);
    		but.setEnabled(true);
		}
	}
	
	private void enableSomeButtons() {
		for (Integer i = 1; i < 37; i++) {
			int id = getResources().getIdentifier("but".concat(i.toString()), "id", getPackageName());
    		Button but = (Button)findViewById(id);
    		if(!pushedButtons.contains(id))
    			but.setEnabled(true);
    		else
    			but.setEnabled(false);
		}
	}
	
	public void hit(View view) {
		String idAsString = view.getResources().getResourceName(view.getId());
		String idBut = idAsString.substring(idAsString.length() - 2);
		if (idBut.getBytes()[0] == 't')
			idBut = idBut.substring(1);
		Log.i("apasat: ", idBut);
		
		int id = getResources().getIdentifier("but".concat(idBut), "id", getPackageName());
		Button but = (Button)findViewById(id);
		pushedButtons.add(id);
		but.setEnabled(false);
		
		if (serverPlane.contains(Integer.parseInt(idBut)) == true) {
			if (myPlane.contains(Integer.parseInt(idBut))) 
				but.setText("|H|");
			else
				but.setText("H");
			but.setBackgroundColor(Color.GREEN);
			TextView mScore = (TextView)findViewById(R.id.scor_juc);
			myScore++;
	    	mScore.setText("Your Score: " + myScore);
	    	
			new Hit().execute("H".concat(idBut), null, null);
		}
		else {
			if (myPlane.contains(Integer.parseInt(idBut))) 
				but.setText("|M|");
			else
				but.setText("M");
    		but.setBackgroundColor(Color.RED);
    		new Hit().execute("M".concat(idBut), null, null);
    		disableButtons();
    		TextView turn = (TextView)findViewById(R.id.TextTeam);
	    	turn.setText("Oponnent turn");
    		new Receive().execute(null, null, null);
		}
	}
	
	private class Receive extends AsyncTask<Void, String, Void> {
		private final static String TAG = "ServerThread";
		
		@Override
		protected Void doInBackground(Void... params) {
			Log.e(TAG, "Receive doInBackground");
			Socket socket = MainActivity.SocketConnection.getSocket();
			try {
				BufferedReader responseReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String line;
					
				while (true) {
					line = responseReader.readLine();
					if (line.getBytes()[0] == 'M') {
						publishProgress("M");
						break;
					}
					else {
						String id = line.substring(1);
						Log.i("server received command: ", line);
						publishProgress(id);
					}
				}
				
			} catch (IOException e) {
				Log.e("writing to socket", "error receiving Hit");
				return null;
			}
			
			return null;
		}
		
		protected void onProgressUpdate(String... progress) {
			String action = progress[0];
			if (action.equals("M")) {
				enableSomeButtons();
				TextView turn = (TextView)findViewById(R.id.TextTeam);
		    	turn.setText("Your turn");
			}
			else {
				int id = getResources().getIdentifier("but".concat(progress[0]), "id", getPackageName());
				Button but = (Button)findViewById(id);
				but.setBackgroundColor(Color.RED);
				TextView oScore = (TextView)findViewById(R.id.scor_opp);
				serverScore++;
		    	oScore.setText("Oponnent Score: " + serverScore);
			}
		}
	}
	private class Hit extends AsyncTask<String, Void, Void> {
		private final static String TAG = "ServerThread";
		
		@Override
		protected Void doInBackground(String... params) {
			Log.e(TAG, "Hit doInBackground");
			Socket socket = MainActivity.SocketConnection.getSocket();
			try {
				PrintStream writer = new PrintStream(socket.getOutputStream());
				writer.println(params[0]);
				Log.i("Hit: client sending position: ", params[0]);
				
			} catch (IOException e) {
				Log.e("writing to socket", "error while sending hit");
				return null;
			}
			
			return null;
		}
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_game, container,
					false);
			return rootView;
		}
	}
	
	public static int randInt(int min, int max) {
	    Random rand = new Random();
	    int randomNum = rand.nextInt((max - min) + 1) + min;
	    return randomNum;
	}

}
