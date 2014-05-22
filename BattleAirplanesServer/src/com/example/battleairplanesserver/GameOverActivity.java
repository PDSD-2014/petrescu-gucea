package com.example.battleairplanesserver;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class GameOverActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game_over);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
		    Boolean win = extras.getBoolean("win");
		    extras.remove("win");
		    ImageView winImage = (ImageView) findViewById(R.id.win);
		    if (win == true) {
		    	 winImage.setImageDrawable(getResources().getDrawable(R.drawable.win));
		    }
		    else
		    	winImage.setImageDrawable(getResources().getDrawable(R.drawable.game_over));
		}
	}
	
	public void replay(View view) {
		Log.i("Buton", "Replay");
		new Replay().execute(null, null, null);
	}
	
	private class Replay extends AsyncTask<Void, Void, Void> {
		private final static String TAG = "ServerThread";
		
		@Override
		protected Void doInBackground(Void... params) {
			Log.i(TAG, "Replay");
			Socket socket = MainActivity.SocketConnection.getSocket();
			try {
				PrintStream writer = new PrintStream(socket.getOutputStream());
				writer.println("replay");
				Intent intent = new Intent(GameOverActivity.this, GameActivity.class);
				intent.putExtra("newGame", true);
				startActivity(intent);
			} catch (IOException e) {
				Log.e("writing to socket", "error while sending replay");
				return null;
			}
			
			return null;
		}
		
	}
	
	public void quit(View view) {
		Log.i("Buton", "Quit");
		new QuitGame().execute(null, null, null);
	}
	
	private class QuitGame extends AsyncTask<Void, Void, Void> {
		private final static String TAG = "ServerThread";
		
		@Override
		protected Void doInBackground(Void... params) {
			Log.i(TAG, "Quit");
			Socket socket = MainActivity.SocketConnection.getSocket();
			try {
				PrintStream writer = new PrintStream(socket.getOutputStream());
				writer.println("quit");
				socket.close();
				Intent intent = new Intent(GameOverActivity.this, MainActivity.class);
				startActivity(intent);
			} catch (IOException e) {
				Log.e("writing to socket", "error while sending quit");
				return null;
			}
			
			return null;
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game_over, menu);
		return true;
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
			View rootView = inflater.inflate(R.layout.fragment_game_over,
					container, false);
			return rootView;
		}
	}

}
